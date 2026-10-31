package com.brightloop.subscription.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.DiscountEngine.Application;
import com.brightloop.subscription.billing.DiscountEngine.LineGross;
import com.brightloop.subscription.billing.GstCalculator.GstBreakdown;
import com.brightloop.subscription.billing.dto.CompanyInfo;
import com.brightloop.subscription.billing.dto.InvoicePrintResponse;
import com.brightloop.subscription.billing.dto.InvoiceResponse;
import com.brightloop.subscription.billing.dto.InvoiceSummary;
import com.brightloop.subscription.catalog.tax.Tax;
import com.brightloop.subscription.catalog.tax.TaxService;
import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.common.MailService;
import com.brightloop.subscription.common.PageResponse;
import com.brightloop.subscription.common.sequence.SequenceService;
import com.brightloop.subscription.config.AppProperties;
import com.brightloop.subscription.currency.CurrencyService;
import com.brightloop.subscription.notification.NotificationService;
import com.brightloop.subscription.payment.PaymentRepository;
import com.brightloop.subscription.security.CurrentUserService;
import com.brightloop.subscription.subscription.BillingPeriods;
import com.brightloop.subscription.subscription.Subscription;
import com.brightloop.subscription.subscription.SubscriptionLine;
import com.brightloop.subscription.subscription.SubscriptionService;
import com.brightloop.subscription.user.Role;
import com.brightloop.subscription.user.User;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;
    private final DiscountEngine discountEngine;
    private final GstCalculator gstCalculator;
    private final CurrencyService currencyService;
    private final TaxService taxService;
    private final SequenceService sequenceService;
    private final CurrentUserService currentUserService;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final AppProperties properties;

    public InvoiceService(InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            SubscriptionService subscriptionService,
            DiscountEngine discountEngine,
            GstCalculator gstCalculator,
            CurrencyService currencyService,
            TaxService taxService,
            SequenceService sequenceService,
            CurrentUserService currentUserService,
            MailService mailService,
            NotificationService notificationService,
            AppProperties properties) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.subscriptionService = subscriptionService;
        this.discountEngine = discountEngine;
        this.gstCalculator = gstCalculator;
        this.currencyService = currencyService;
        this.taxService = taxService;
        this.sequenceService = sequenceService;
        this.currentUserService = currentUserService;
        this.mailService = mailService;
        this.notificationService = notificationService;
        this.properties = properties;
    }

    @Transactional
    public Invoice generateForSubscription(Subscription subscription, LocalDate periodStart, boolean confirm) {
        return invoiceRepository.findBySubscriptionIdAndBillingPeriodStart(subscription.getId(), periodStart)
                .orElseGet(() -> buildInvoice(subscription, periodStart, confirm));
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceSummary> list(InvoiceStatus status, Long customerId, LocalDate from, LocalDate to,
            String currency, Pageable pageable) {
        User current = currentUserService.get();
        Long effectiveCustomer = current.getRole() == Role.CUSTOMER ? current.getId() : customerId;
        String effectiveCurrency = currency == null ? null : CurrencyService.normalize(currency);
        Page<Invoice> page = invoiceRepository.search(status, effectiveCustomer, from, to, effectiveCurrency, pageable);
        return PageResponse.from(page, InvoiceSummary::from);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        return toResponse(loadAuthorized(id));
    }

    @Transactional(readOnly = true)
    public InvoicePrintResponse print(Long id) {
        Invoice invoice = loadAuthorized(id);
        CompanyInfo company = new CompanyInfo(properties.company().name(), properties.company().state(),
                properties.company().gstin());
        return new InvoicePrintResponse(company, toResponse(invoice));
    }

    @Transactional
    public InvoiceResponse confirm(Long id) {
        Invoice invoice = loadAuthorized(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw ApiException.badRequest("Only draft invoices can be confirmed");
        }
        invoice.setStatus(InvoiceStatus.CONFIRMED);
        notificationService.notify(invoice.getCustomer(), "INVOICE_CONFIRMED",
                "Invoice " + invoice.getNumber() + " confirmed",
                "Invoice " + invoice.getNumber() + " for " + invoice.getCurrency() + " " + invoice.getTotal()
                        + " is due on " + invoice.getDueDate() + ".",
                "INVOICE", invoice.getId());
        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse cancel(Long id) {
        Invoice invoice = loadAuthorized(id);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw ApiException.forbidden("Paid invoices cannot be cancelled");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw ApiException.badRequest("Invoice is already cancelled");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return toResponse(invoice);
    }

    @Transactional
    public void send(Long id) {
        Invoice invoice = loadAuthorized(id);
        mailService.send(invoice.getCustomer().getEmail(),
                "Invoice " + invoice.getNumber() + " from " + properties.company().name(),
                "Hello " + invoice.getCustomer().getFullName() + ",\n\n"
                        + "Your invoice " + invoice.getNumber() + " for " + invoice.getCurrency() + " "
                        + invoice.getTotal() + " is due on " + invoice.getDueDate() + ".\n\nThank you.");
        notificationService.notify(invoice.getCustomer(), "INVOICE_SENT",
                "Invoice " + invoice.getNumber() + " sent",
                "A copy of invoice " + invoice.getNumber() + " was sent to " + invoice.getCustomer().getEmail() + ".",
                "INVOICE", invoice.getId());
    }

    @Transactional(readOnly = true)
    public Invoice findEntity(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> ApiException.notFound("Invoice"));
    }

    @Transactional(readOnly = true)
    public Invoice getAuthorizedInvoice(Long id) {
        return loadAuthorized(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal paidAmount(Invoice invoice) {
        return paymentRepository.sumByInvoiceId(invoice.getId());
    }

    public boolean isOverdue(Invoice invoice) {
        return invoice.getStatus() != InvoiceStatus.PAID
                && invoice.getStatus() != InvoiceStatus.CANCELLED
                && invoice.getDueDate().isBefore(LocalDate.now());
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.from(invoice, paymentRepository.sumByInvoiceId(invoice.getId()), isOverdue(invoice));
    }

    private Invoice buildInvoice(Subscription subscription, LocalDate periodStart, boolean confirm) {
        BigDecimal rate = currencyService.rateToInr(subscription.getCurrency());
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(subscription.getPaymentTermsDays());
        LocalDate periodEnd = BillingPeriods.add(periodStart, subscription.getPlan().getBillingPeriod());
        Tax defaultTax = taxService.resolveDefault();

        List<LineData> data = new ArrayList<>();
        for (SubscriptionLine line : subscription.getLines()) {
            BigDecimal unitPrice = convert(line.getUnitPrice(), rate);
            BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(line.getQty()));
            BigDecimal taxPercent = line.getTax() != null ? line.getTax().getPercent()
                    : defaultTax != null ? defaultTax.getPercent() : BigDecimal.ZERO;
            data.add(new LineData(line, unitPrice, gross, taxPercent));
        }

        BigDecimal totalGross = data.stream().map(LineData::gross).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<LineGross> grosses = data.stream()
                .map(d -> new LineGross(d.line().getProduct().getId(), d.gross(), d.line().getQty()))
                .toList();
        List<Application> applications = discountEngine.evaluate(grosses, issueDate, rate);
        BigDecimal totalDiscount = applications.stream().map(Application::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice invoice = Invoice.builder()
                .number(sequenceService.nextNumber("invoice", "INV"))
                .subscription(subscription)
                .customer(subscription.getCustomer())
                .status(confirm ? InvoiceStatus.CONFIRMED : InvoiceStatus.DRAFT)
                .billingPeriodStart(periodStart)
                .billingPeriodEnd(periodEnd)
                .currency(subscription.getCurrency())
                .exchangeRate(rate)
                .issueDate(issueDate)
                .dueDate(dueDate)
                .retryCount(0)
                .companyGstin(properties.company().gstin())
                .customerGstin(subscription.getCustomer().getGstin())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal allocatedSoFar = BigDecimal.ZERO;
        for (int i = 0; i < data.size(); i++) {
            LineData line = data.get(i);
            BigDecimal allocated;
            if (i == data.size() - 1) {
                allocated = totalDiscount.subtract(allocatedSoFar);
            } else {
                allocated = totalGross.signum() == 0 ? BigDecimal.ZERO
                        : totalDiscount.multiply(line.gross()).divide(totalGross, 2, RoundingMode.HALF_UP);
            }
            allocatedSoFar = allocatedSoFar.add(allocated);
            BigDecimal lineNet = line.gross().subtract(allocated).max(BigDecimal.ZERO);
            GstBreakdown gst = gstCalculator.calculate(lineNet, line.taxPercent(), subscription.getCurrency(),
                    subscription.getCustomer());
            BigDecimal lineTotal = lineNet.add(gst.total());

            invoice.addLine(InvoiceLine.builder()
                    .product(line.line().getProduct())
                    .variant(line.line().getVariant())
                    .description(description(line.line()))
                    .hsnSacCode(line.line().getHsnSacCode())
                    .qty(line.line().getQty())
                    .unitPrice(line.unitPrice())
                    .taxPercent(line.taxPercent())
                    .discountAmount(allocated)
                    .lineNet(lineNet)
                    .cgstAmount(gst.cgst())
                    .sgstAmount(gst.sgst())
                    .igstAmount(gst.igst())
                    .total(lineTotal)
                    .build());

            subtotal = subtotal.add(lineNet);
            taxTotal = taxTotal.add(gst.total());
            invoice.setPlaceOfSupply(gst.placeOfSupply());
        }

        invoice.setSubtotal(subtotal);
        invoice.setTaxTotal(taxTotal);
        invoice.setDiscountTotal(totalDiscount);
        invoice.setTotal(subtotal.add(taxTotal));
        invoice.setTotalInBase(invoice.getTotal().multiply(rate).setScale(2, RoundingMode.HALF_UP));

        applications.forEach(application ->
                application.discount().setUsedCount(application.discount().getUsedCount() + 1));

        Invoice saved = invoiceRepository.save(invoice);
        notificationService.notify(saved.getCustomer(), "INVOICE_CREATED",
                "Invoice " + saved.getNumber() + " generated",
                "Invoice " + saved.getNumber() + " for " + saved.getCurrency() + " " + saved.getTotal()
                        + " has been generated.",
                "INVOICE", saved.getId());
        return saved;
    }

    private Invoice loadAuthorized(Long id) {
        Invoice invoice = findEntity(id);
        User current = currentUserService.get();
        if (current.getRole() == Role.CUSTOMER && !invoice.getCustomer().getId().equals(current.getId())) {
            throw ApiException.notFound("Invoice");
        }
        return invoice;
    }

    private String description(SubscriptionLine line) {
        if (line.getVariant() == null) {
            return line.getProduct().getName();
        }
        return line.getProduct().getName() + " (" + line.getVariant().getAttribute() + ": "
                + line.getVariant().getValue() + ")";
    }

    private BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return amount.divide(rate, 2, RoundingMode.HALF_UP);
    }

    private record LineData(SubscriptionLine line, BigDecimal unitPrice, BigDecimal gross, BigDecimal taxPercent) {
    }
}
