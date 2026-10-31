package com.brightloop.subscription.profit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.Invoice;
import com.brightloop.subscription.billing.InvoiceLine;
import com.brightloop.subscription.billing.InvoiceRepository;
import com.brightloop.subscription.catalog.plan.BillingPeriod;
import com.brightloop.subscription.payment.PaymentRepository;
import com.brightloop.subscription.profit.dto.ProfitGroupRow;
import com.brightloop.subscription.profit.dto.ProfitResponse;
import com.brightloop.subscription.profit.dto.ProfitSubscriptionRow;
import com.brightloop.subscription.profit.dto.ProfitSummary;
import com.brightloop.subscription.subscription.Subscription;
import com.brightloop.subscription.subscription.SubscriptionRepository;

@Service
public class ProfitService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    public ProfitService(InvoiceRepository invoiceRepository,
            SubscriptionRepository subscriptionRepository,
            PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public ProfitResponse report(LocalDate from, LocalDate to, Long planId) {
        List<Invoice> invoices = invoiceRepository.findPaidForProfit(from, to, planId);

        Map<Long, SubscriptionAccumulator> subscriptionAcc = new HashMap<>();
        Map<Long, ProfitAccumulator> planAcc = new HashMap<>();
        Map<Long, ProfitAccumulator> productAcc = new HashMap<>();
        Map<YearMonth, ProfitAccumulator> monthAcc = new TreeMap<>();

        BigDecimal gstCollected = invoiceRepository.sumTaxInBase(planId, from, to);
        for (Invoice invoice : invoices) {
            BigDecimal revenue = invoice.getSubtotal().multiply(invoice.getExchangeRate());
            BigDecimal cost = invoice.getLines().stream()
                    .map(this::lineCost)
                    .reduce(ZERO, BigDecimal::add);

            SubscriptionAccumulator subAcc = subscriptionAcc.computeIfAbsent(
                    invoice.getSubscription().getId(), key -> new SubscriptionAccumulator(invoice.getSubscription()));
            subAcc.revenue = subAcc.revenue.add(revenue);
            subAcc.cost = subAcc.cost.add(cost);

            Long planKey = invoice.getSubscription().getPlan().getId();
            ProfitAccumulator plan = planAcc.computeIfAbsent(planKey,
                    key -> new ProfitAccumulator(planKey, invoice.getSubscription().getPlan().getName()));
            plan.add(revenue, cost);

            for (InvoiceLine line : invoice.getLines()) {
                Long productKey = line.getProduct().getId();
                ProfitAccumulator product = productAcc.computeIfAbsent(productKey,
                        key -> new ProfitAccumulator(productKey, line.getProduct().getName()));
                product.add(line.getLineNet().multiply(invoice.getExchangeRate()), lineCost(line));
            }

            YearMonth month = YearMonth.from(invoice.getIssueDate());
            ProfitAccumulator monthRow = monthAcc.computeIfAbsent(month,
                    key -> new ProfitAccumulator(null, key.toString()));
            monthRow.add(revenue, cost);
        }

        List<ProfitSubscriptionRow> subscriptionRows = new ArrayList<>();
        for (SubscriptionAccumulator acc : subscriptionAcc.values()) {
            subscriptionRows.add(new ProfitSubscriptionRow(
                    acc.subscription.getId(),
                    acc.subscription.getNumber(),
                    acc.subscription.getCustomer().getFullName(),
                    acc.subscription.getPlan().getName(),
                    acc.subscription.getStatus().name(),
                    scale(acc.revenue),
                    scale(acc.cost),
                    scale(acc.revenue.subtract(acc.cost)),
                    marginPercent(acc.revenue, acc.revenue.subtract(acc.cost))));
        }
        subscriptionRows.sort(Comparator.comparing(ProfitSubscriptionRow::profitInBase).reversed());

        List<ProfitSubscriptionRow> topProfitable = subscriptionRows.stream().limit(5).toList();
        List<ProfitSubscriptionRow> lossMaking = subscriptionRows.stream()
                .sorted(Comparator.comparing(ProfitSubscriptionRow::profitInBase))
                .limit(5)
                .toList();

        List<ProfitGroupRow> plans = planAcc.values().stream()
                .map(ProfitAccumulator::toRow)
                .sorted(Comparator.comparing(ProfitGroupRow::revenueInBase).reversed())
                .toList();
        List<ProfitGroupRow> products = productAcc.values().stream()
                .map(ProfitAccumulator::toRow)
                .sorted(Comparator.comparing(ProfitGroupRow::revenueInBase).reversed())
                .toList();
        List<ProfitGroupRow> monthly = monthAcc.values().stream()
                .map(ProfitAccumulator::toRow)
                .toList();

        BigDecimal totalRevenue = subscriptionRows.stream()
                .map(ProfitSubscriptionRow::revenueInBase)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalCost = subscriptionRows.stream()
                .map(ProfitSubscriptionRow::costInBase)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal grossProfit = totalRevenue.subtract(totalCost);

        BigDecimal mrr = subscriptionRepository.findActiveForMrr(planId).stream()
                .map(this::monthlyRecurringValue)
                .reduce(ZERO, BigDecimal::add);

        List<Invoice> outstandingInvoices = invoiceRepository.findOutstanding(null, null, planId, from, to, null);
        Map<Long, BigDecimal> paidByInvoice = paidByInvoice(outstandingInvoices);
        BigDecimal outstanding = ZERO;
        for (Invoice invoice : outstandingInvoices) {
            BigDecimal paid = paidByInvoice.getOrDefault(invoice.getId(), ZERO);
            BigDecimal remaining = invoice.getTotal().subtract(paid).max(ZERO);
            outstanding = outstanding.add(remaining.multiply(invoice.getExchangeRate()));
        }

        ProfitSummary summary = new ProfitSummary(
                scale(totalRevenue),
                scale(totalCost),
                scale(grossProfit),
                marginPercent(totalRevenue, grossProfit),
                scale(mrr),
                scale(outstanding),
                scale(gstCollected));

        return new ProfitResponse(summary, subscriptionRows, topProfitable, lossMaking, plans, products, monthly);
    }

    private BigDecimal lineCost(InvoiceLine line) {
        return line.getProduct().getCostPrice().multiply(BigDecimal.valueOf(line.getQty()));
    }

    private BigDecimal monthlyRecurringValue(Subscription subscription) {
        BigDecimal amount = subscription.getLines().stream()
                .map(line -> line.getAmount())
                .reduce(ZERO, BigDecimal::add);
        return amount.multiply(factor(subscription.getPlan().getBillingPeriod()));
    }

    private BigDecimal factor(BillingPeriod period) {
        return switch (period) {
            case DAILY -> BigDecimal.valueOf(30);
            case WEEKLY -> new BigDecimal("4.33");
            case MONTHLY -> BigDecimal.ONE;
            case YEARLY -> BigDecimal.ONE.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        };
    }

    private Map<Long, BigDecimal> paidByInvoice(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return Map.of();
        }
        Map<Long, BigDecimal> paid = new HashMap<>();
        for (Object[] row : paymentRepository.sumByInvoiceIds(invoices.stream().map(Invoice::getId).toList())) {
            paid.put((Long) row[0], (BigDecimal) row[1]);
        }
        return paid;
    }

    private BigDecimal marginPercent(BigDecimal revenue, BigDecimal profit) {
        if (revenue.signum() == 0) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return profit.multiply(HUNDRED).divide(revenue, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static final class SubscriptionAccumulator {
        private final Subscription subscription;
        private BigDecimal revenue = ZERO;
        private BigDecimal cost = ZERO;

        private SubscriptionAccumulator(Subscription subscription) {
            this.subscription = subscription;
        }
    }

    private static final class ProfitAccumulator {
        private final Long id;
        private final String name;
        private BigDecimal revenue = ZERO;
        private BigDecimal cost = ZERO;

        private ProfitAccumulator(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        private void add(BigDecimal revenue, BigDecimal cost) {
            this.revenue = this.revenue.add(revenue);
            this.cost = this.cost.add(cost);
        }

        private ProfitGroupRow toRow() {
            BigDecimal profit = revenue.subtract(cost);
            BigDecimal margin = revenue.signum() == 0
                    ? ZERO.setScale(2, RoundingMode.HALF_UP)
                    : profit.multiply(HUNDRED).divide(revenue, 2, RoundingMode.HALF_UP);
            return new ProfitGroupRow(id, name, revenue.setScale(2, RoundingMode.HALF_UP),
                    cost.setScale(2, RoundingMode.HALF_UP), profit.setScale(2, RoundingMode.HALF_UP), margin);
        }
    }
}
