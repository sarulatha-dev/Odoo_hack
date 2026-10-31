package com.brightloop.subscription.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.Invoice;
import com.brightloop.subscription.billing.InvoiceLine;
import com.brightloop.subscription.billing.InvoiceRepository;
import com.brightloop.subscription.billing.InvoiceStatus;
import com.brightloop.subscription.catalog.plan.Plan;
import com.brightloop.subscription.catalog.plan.PlanRepository;
import com.brightloop.subscription.catalog.plan.PlanTier;
import com.brightloop.subscription.catalog.product.Product;
import com.brightloop.subscription.catalog.product.ProductRepository;
import com.brightloop.subscription.catalog.tax.Tax;
import com.brightloop.subscription.catalog.tax.TaxRepository;
import com.brightloop.subscription.common.sequence.DocumentSequence;
import com.brightloop.subscription.common.sequence.DocumentSequenceRepository;
import com.brightloop.subscription.currency.CurrencyRate;
import com.brightloop.subscription.currency.CurrencyRateRepository;
import com.brightloop.subscription.payment.Payment;
import com.brightloop.subscription.payment.PaymentMethod;
import com.brightloop.subscription.payment.PaymentRepository;
import com.brightloop.subscription.subscription.Subscription;
import com.brightloop.subscription.subscription.SubscriptionLine;
import com.brightloop.subscription.subscription.SubscriptionRepository;
import com.brightloop.subscription.subscription.SubscriptionStatus;
import com.brightloop.subscription.user.IndianState;
import com.brightloop.subscription.user.Role;
import com.brightloop.subscription.user.User;
import com.brightloop.subscription.user.UserRepository;

@Component
@Order(2)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String COMPANY_GSTIN = "29ABCDE1234F1Z5";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final ProductRepository productRepository;
    private final TaxRepository taxRepository;
    private final CurrencyRateRepository currencyRateRepository;
    private final DocumentSequenceRepository sequenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final int targetSubscriptions;
    private final int targetCustomers;

    public DemoDataSeeder(SubscriptionRepository subscriptionRepository, InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository, UserRepository userRepository, PlanRepository planRepository,
            ProductRepository productRepository, TaxRepository taxRepository,
            CurrencyRateRepository currencyRateRepository, DocumentSequenceRepository sequenceRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.demo.enabled:true}") boolean enabled,
            @Value("${app.seed.demo.subscriptions:2000}") int targetSubscriptions,
            @Value("${app.seed.demo.customers:250}") int targetCustomers) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.productRepository = productRepository;
        this.taxRepository = taxRepository;
        this.currencyRateRepository = currencyRateRepository;
        this.sequenceRepository = sequenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.targetSubscriptions = targetSubscriptions;
        this.targetCustomers = targetCustomers;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        long existing = subscriptionRepository.count();
        if (existing >= targetSubscriptions) {
            log.info("Demo data already present: subscriptions={}, invoices={}", existing,
                    invoiceRepository.count());
            return;
        }

        List<Plan> plans = planRepository.findAll();
        List<Product> products = productRepository.findAll();
        if (plans.isEmpty() || products.isEmpty()) {
            log.warn("Skipping demo data: seed the catalog first");
            return;
        }
        Tax tax = taxRepository.findFirstByDefaultTaxTrue().orElse(null);
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("INR", BigDecimal.ONE);
        for (CurrencyRate rate : currencyRateRepository.findAll()) {
            rates.put(rate.getCurrency(), rate.getRateToInr());
        }

        Random random = new Random(20261031L);
        List<User> customers = ensureCustomers(random);
        DocumentSequence subscriptionSequence = sequenceRepository.findById("subscription")
                .orElseGet(() -> new DocumentSequence("subscription", 1L));
        DocumentSequence invoiceSequence = sequenceRepository.findById("invoice")
                .orElseGet(() -> new DocumentSequence("invoice", 1L));
        SequenceCursor cursor = new SequenceCursor(subscriptionSequence.getNextValue(),
                invoiceSequence.getNextValue());
        int toCreate = (int) (targetSubscriptions - existing);
        long started = System.currentTimeMillis();
        for (int i = 0; i < toCreate; i++) {
            createSubscription(random, customers, plans, products, tax, rates, cursor);
            if ((i + 1) % 250 == 0) {
                log.info("Demo seed progress: {}/{} subscriptions", i + 1, toCreate);
            }
        }
        subscriptionSequence.setNextValue(cursor.subscription);
        invoiceSequence.setNextValue(cursor.invoice);
        sequenceRepository.save(subscriptionSequence);
        sequenceRepository.save(invoiceSequence);
        log.info("Demo data seeded: {} subscriptions and {} invoices in {} ms (customers={})", toCreate,
                invoiceRepository.count(), System.currentTimeMillis() - started, customers.size());
    }

    private List<User> ensureCustomers(Random random) {
        List<User> customers = new ArrayList<>(userRepository.findByRoleOrderByFullNameAsc(Role.CUSTOMER));
        String passwordHash = passwordEncoder.encode("Customer@12345");
        IndianState[] states = IndianState.values();
        for (int i = 1; i <= targetCustomers; i++) {
            String email = String.format("customer%03d@brightloop.test", i);
            if (userRepository.existsByEmailIgnoreCase(email)) {
                continue;
            }
            IndianState state = states[i % states.length];
            String currency = i % 7 == 0 ? "USD" : i % 11 == 0 ? "EUR" : "INR";
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordHash)
                    .fullName(String.format("Demo Customer %03d", i))
                    .role(Role.CUSTOMER)
                    .state(state)
                    .gstin(state == IndianState.KARNATAKA ? "29AAAAA" + String.format("%04d", i) + "A1Z5" : null)
                    .preferredCurrency(currency)
                    .autopayEnabled(i % 6 == 0)
                    .autopayMethod(i % 6 == 0 ? "CARD" : null)
                    .autopayToken(i % 6 == 0 ? "tok_demo_" + i : null)
                    .gatewayCustomerId(i % 6 == 0 ? "cus_demo_" + i : null)
                    .monthlyBudget(i % 9 == 0 ? BigDecimal.valueOf(500 + random.nextInt(1500)) : null)
                    .active(true)
                    .build();
            customers.add(userRepository.save(user));
        }
        return customers;
    }

    private void createSubscription(Random random, List<User> customers, List<Plan> plans,
            List<Product> products, Tax tax, Map<String, BigDecimal> rates, SequenceCursor cursor) {
        User customer = customers.get(random.nextInt(customers.size()));
        Plan plan = pickPlan(random, plans);
        SubscriptionStatus status = pickStatus(random);
        String currency = pickCurrency(random, rates.keySet());
        BigDecimal rate = rates.getOrDefault(currency, BigDecimal.ONE);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(30 + random.nextInt(540));
        int paymentTerms = new int[] { 7, 15, 30 }[random.nextInt(3)];

        Subscription subscription = Subscription.builder()
                .number(String.format("SUB-%04d", cursor.subscription++))
                .customer(customer)
                .plan(plan)
                .startDate(start)
                .expiryDate(status == SubscriptionStatus.CLOSED ? start.plusMonths(6 + random.nextInt(12)) : null)
                .paymentTermsDays(paymentTerms)
                .status(status)
                .nextBillingDate(status == SubscriptionStatus.ACTIVE ? today.plusDays(random.nextInt(30)) : null)
                .currency(currency)
                .build();

        BigDecimal remaining = plan.getPrice();
        int lineCount = plan.getTier() == PlanTier.HIGH && random.nextBoolean() ? 2 : 1;
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            Product product = products.get(random.nextInt(products.size()));
            BigDecimal unitPrice;
            if (lineIndex == lineCount - 1) {
                unitPrice = remaining;
            } else {
                unitPrice = plan.getPrice().multiply(new BigDecimal("0.6")).setScale(2, RoundingMode.HALF_UP);
                remaining = remaining.subtract(unitPrice);
            }
            subscription.addLine(SubscriptionLine.builder()
                    .product(product)
                    .qty(1)
                    .unitPrice(unitPrice)
                    .tax(tax)
                    .amount(unitPrice)
                    .hsnSacCode(product.getHsnSacCode())
                    .cgstAmount(BigDecimal.ZERO)
                    .sgstAmount(BigDecimal.ZERO)
                    .igstAmount(BigDecimal.ZERO)
                    .build());
        }
        subscriptionRepository.save(subscription);

        if (status == SubscriptionStatus.CONFIRMED || status == SubscriptionStatus.QUOTATION
                || status == SubscriptionStatus.DRAFT) {
            return;
        }

        int months = (int) Math.min(6, ChronoUnit.MONTHS.between(YearMonth.from(start), YearMonth.now()) + 1);
        months = Math.max(1, months);
        for (int offset = 0; offset < months; offset++) {
            LocalDate periodStart = start.plusMonths(offset);
            if (periodStart.isAfter(today)) {
                break;
            }
            boolean latest = offset == months - 1;
            InvoiceStatus invoiceStatus = pickInvoiceStatus(random, status, latest);
            LocalDate issueDate = periodStart;
            if (latest && invoiceStatus == InvoiceStatus.CONFIRMED) {
                issueDate = today.minusDays(40 + random.nextInt(20));
            }
            createInvoice(random, subscription, periodStart, issueDate, paymentTerms, invoiceStatus, rate, cursor);
        }
    }

    private Plan pickPlan(Random random, List<Plan> plans) {
        PlanTier tier = random.nextInt(100) < 50 ? PlanTier.LOW
                : random.nextInt(100) < 60 ? PlanTier.MEDIUM : PlanTier.HIGH;
        List<Plan> matching = plans.stream().filter(plan -> plan.getTier() == tier).toList();
        List<Plan> pool = matching.isEmpty() ? plans : matching;
        return pool.get(random.nextInt(pool.size()));
    }

    private SubscriptionStatus pickStatus(Random random) {
        int roll = random.nextInt(100);
        if (roll < 72) {
            return SubscriptionStatus.ACTIVE;
        }
        if (roll < 76) {
            return SubscriptionStatus.PAUSED;
        }
        if (roll < 94) {
            return SubscriptionStatus.CLOSED;
        }
        if (roll < 97) {
            return SubscriptionStatus.CONFIRMED;
        }
        return SubscriptionStatus.QUOTATION;
    }

    private InvoiceStatus pickInvoiceStatus(Random random, SubscriptionStatus subscriptionStatus, boolean latest) {
        int roll = random.nextInt(100);
        if (subscriptionStatus == SubscriptionStatus.CLOSED) {
            return roll < 95 ? InvoiceStatus.PAID : InvoiceStatus.CANCELLED;
        }
        if (subscriptionStatus == SubscriptionStatus.PAUSED) {
            return latest && roll < 40 ? InvoiceStatus.CONFIRMED : InvoiceStatus.PAID;
        }
        if (latest) {
            if (roll < 75) {
                return InvoiceStatus.PAID;
            }
            if (roll < 95) {
                return InvoiceStatus.CONFIRMED;
            }
            return InvoiceStatus.DRAFT;
        }
        return roll < 95 ? InvoiceStatus.PAID : InvoiceStatus.CANCELLED;
    }

    private String pickCurrency(Random random, java.util.Set<String> currencies) {
        if (random.nextInt(100) < 85) {
            return "INR";
        }
        List<String> foreign = currencies.stream().filter(code -> !"INR".equals(code)).toList();
        return foreign.isEmpty() ? "INR" : foreign.get(random.nextInt(foreign.size()));
    }

    private void createInvoice(Random random, Subscription subscription, LocalDate periodStart, LocalDate issueDate,
            int paymentTerms, InvoiceStatus status, BigDecimal rate, SequenceCursor cursor) {
        Invoice invoice = Invoice.builder()
                .number(String.format("INV-%04d", cursor.invoice++))
                .subscription(subscription)
                .customer(subscription.getCustomer())
                .status(status)
                .billingPeriodStart(periodStart)
                .billingPeriodEnd(periodStart.plusMonths(1).minusDays(1))
                .currency(subscription.getCurrency())
                .exchangeRate(rate)
                .issueDate(issueDate)
                .dueDate(issueDate.plusDays(paymentTerms))
                .retryCount(0)
                .companyGstin(COMPANY_GSTIN)
                .customerGstin(subscription.getCustomer().getGstin())
                .placeOfSupply(subscription.getCustomer().getState() == null
                        ? "North"
                        : subscription.getCustomer().getState().label())
                .discountTotal(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (SubscriptionLine line : subscription.getLines()) {
            BigDecimal unitPrice = line.getUnitPrice().divide(rate, 2, RoundingMode.HALF_UP);
            BigDecimal lineNet = unitPrice.multiply(BigDecimal.valueOf(line.getQty()));
            BigDecimal taxPercent = "INR".equals(subscription.getCurrency()) && line.getTax() != null
                    ? line.getTax().getPercent()
                    : BigDecimal.ZERO;
            BigDecimal taxAmount = lineNet.multiply(taxPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;
            if (taxAmount.signum() > 0) {
                if (subscription.getCustomer().getState() == IndianState.KARNATAKA) {
                    cgst = taxAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                    sgst = taxAmount.subtract(cgst);
                } else {
                    igst = taxAmount;
                }
            }
            BigDecimal lineTotal = lineNet.add(taxAmount);
            invoice.addLine(InvoiceLine.builder()
                    .product(line.getProduct())
                    .variant(line.getVariant())
                    .description(line.getProduct().getName())
                    .hsnSacCode(line.getHsnSacCode())
                    .qty(line.getQty())
                    .unitPrice(unitPrice)
                    .taxPercent(taxPercent)
                    .discountAmount(BigDecimal.ZERO)
                    .lineNet(lineNet)
                    .cgstAmount(cgst)
                    .sgstAmount(sgst)
                    .igstAmount(igst)
                    .total(lineTotal)
                    .build());
            subtotal = subtotal.add(lineNet);
            taxTotal = taxTotal.add(taxAmount);
            total = total.add(lineTotal);
        }

        invoice.setSubtotal(subtotal);
        invoice.setTaxTotal(taxTotal);
        invoice.setTotal(total);
        invoice.setTotalInBase(total.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        invoiceRepository.save(invoice);

        if (status == InvoiceStatus.PAID) {
            PaymentMethod method = PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)];
            paymentRepository.save(Payment.builder()
                    .invoice(invoice)
                    .method(method)
                    .amount(total)
                    .paidOn(issueDate.plusDays(random.nextInt(4)))
                    .note("Seeded demo payment")
                    .gatewayRef(method == PaymentMethod.GATEWAY ? "pay_demo_" + invoice.getNumber() : null)
                    .build());
        }
    }

    private static final class SequenceCursor {
        private long subscription;
        private long invoice;

        private SequenceCursor(long subscription, long invoice) {
            this.subscription = subscription;
            this.invoice = invoice;
        }
    }
}
