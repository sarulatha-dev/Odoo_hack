package com.brightloop.subscription.expense;

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

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.auth.dto.UserSummary;
import com.brightloop.subscription.billing.Invoice;
import com.brightloop.subscription.billing.InvoiceRepository;
import com.brightloop.subscription.billing.InvoiceStatus;
import com.brightloop.subscription.currency.CurrencyService;
import com.brightloop.subscription.expense.dto.BudgetRequest;
import com.brightloop.subscription.expense.dto.CurrencySpendResponse;
import com.brightloop.subscription.expense.dto.ExpenseSummaryResponse;
import com.brightloop.subscription.expense.dto.MonthlySpendResponse;
import com.brightloop.subscription.expense.dto.SubscriptionSpendResponse;
import com.brightloop.subscription.expense.dto.UpcomingRenewalResponse;
import com.brightloop.subscription.security.CurrentUserService;
import com.brightloop.subscription.subscription.Subscription;
import com.brightloop.subscription.subscription.SubscriptionRepository;
import com.brightloop.subscription.subscription.SubscriptionStatus;
import com.brightloop.subscription.user.User;
import com.brightloop.subscription.user.UserRepository;

@Service
public class ExpenseService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CurrencyService currencyService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public ExpenseService(InvoiceRepository invoiceRepository,
            SubscriptionRepository subscriptionRepository,
            CurrencyService currencyService,
            CurrentUserService currentUserService,
            UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.currencyService = currencyService;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse summary() {
        User user = currentUserService.get();
        String display = CurrencyService.normalize(user.getPreferredCurrency());
        BigDecimal displayRate = currencyService.rateToInr(display);
        LocalDate today = LocalDate.now();

        List<Invoice> invoices = invoiceRepository
                .search(null, user.getId(), null, null, null, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.CANCELLED)
                .toList();

        BigDecimal totalInBase = ZERO;
        Map<YearMonth, BigDecimal> monthly = new TreeMap<>();
        Map<Long, BigDecimal> bySubscriptionBase = new HashMap<>();
        Map<Long, Invoice> subscriptionInvoices = new HashMap<>();
        Map<String, BigDecimal[]> byCurrency = new HashMap<>();

        for (Invoice invoice : invoices) {
            BigDecimal base = invoice.getTotalInBase();
            totalInBase = totalInBase.add(base);
            monthly.merge(YearMonth.from(invoice.getIssueDate()), base, BigDecimal::add);
            bySubscriptionBase.merge(invoice.getSubscription().getId(), base, BigDecimal::add);
            subscriptionInvoices.putIfAbsent(invoice.getSubscription().getId(), invoice);

            BigDecimal[] currency = byCurrency.computeIfAbsent(invoice.getCurrency(),
                    key -> new BigDecimal[] { ZERO, ZERO });
            currency[0] = currency[0].add(invoice.getTotal());
            currency[1] = currency[1].add(base);
        }

        List<MonthlySpendResponse> monthlySpend = monthly.entrySet().stream()
                .map(entry -> new MonthlySpendResponse(
                        entry.getKey().toString(),
                        entry.getValue().setScale(2, RoundingMode.HALF_UP),
                        toDisplay(entry.getValue(), displayRate)))
                .toList();

        List<SubscriptionSpendResponse> bySubscription = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : bySubscriptionBase.entrySet()) {
            Invoice invoice = subscriptionInvoices.get(entry.getKey());
            bySubscription.add(new SubscriptionSpendResponse(
                    entry.getKey(),
                    invoice.getSubscription().getNumber(),
                    invoice.getSubscription().getPlan().getName(),
                    invoice.getSubscription().getCurrency(),
                    entry.getValue().setScale(2, RoundingMode.HALF_UP),
                    toDisplay(entry.getValue(), displayRate)));
        }
        bySubscription.sort(Comparator.comparing(SubscriptionSpendResponse::amountInBase).reversed());

        List<UpcomingRenewalResponse> upcoming = new ArrayList<>();
        LocalDate horizon = today.plusDays(30);
        for (Subscription subscription : subscriptionRepository
                .findByCustomerId(user.getId(), Pageable.unpaged()).getContent()) {
            if (subscription.getStatus() != SubscriptionStatus.ACTIVE
                    || subscription.getNextBillingDate() == null
                    || subscription.getNextBillingDate().isBefore(today)
                    || subscription.getNextBillingDate().isAfter(horizon)) {
                continue;
            }
            BigDecimal amountBase = subscription.getLines().stream()
                    .map(line -> line.getAmount())
                    .reduce(ZERO, BigDecimal::add);
            upcoming.add(new UpcomingRenewalResponse(
                    subscription.getId(),
                    subscription.getNumber(),
                    subscription.getPlan().getName(),
                    subscription.getNextBillingDate(),
                    subscription.getCurrency(),
                    amountBase.setScale(2, RoundingMode.HALF_UP),
                    toDisplay(amountBase, displayRate)));
        }
        upcoming.sort(Comparator.comparing(UpcomingRenewalResponse::nextBillingDate));

        List<CurrencySpendResponse> currencySpend = byCurrency.entrySet().stream()
                .map(entry -> new CurrencySpendResponse(
                        entry.getKey(),
                        entry.getValue()[0].setScale(2, RoundingMode.HALF_UP),
                        entry.getValue()[1].setScale(2, RoundingMode.HALF_UP),
                        toDisplay(entry.getValue()[1], displayRate)))
                .sorted(Comparator.comparing(CurrencySpendResponse::amountInBase).reversed())
                .toList();

        BigDecimal currentMonthBase = monthly.getOrDefault(YearMonth.now(), ZERO);
        BigDecimal currentMonthDisplay = toDisplay(currentMonthBase, displayRate);
        BigDecimal budget = user.getMonthlyBudget();
        boolean budgetWarning = budget != null && currentMonthDisplay.compareTo(budget) > 0;

        return new ExpenseSummaryResponse(
                display,
                displayRate,
                totalInBase.setScale(2, RoundingMode.HALF_UP),
                toDisplay(totalInBase, displayRate),
                currentMonthBase.setScale(2, RoundingMode.HALF_UP),
                currentMonthDisplay,
                budget,
                budgetWarning,
                monthlySpend,
                bySubscription,
                upcoming,
                currencySpend);
    }

    @Transactional
    public UserSummary setBudget(BudgetRequest request) {
        User user = currentUserService.get();
        user.setMonthlyBudget(request.monthlyBudget());
        return UserSummary.from(userRepository.save(user));
    }

    private BigDecimal toDisplay(BigDecimal amountInBase, BigDecimal rate) {
        return amountInBase.divide(rate, 2, RoundingMode.HALF_UP);
    }
}
