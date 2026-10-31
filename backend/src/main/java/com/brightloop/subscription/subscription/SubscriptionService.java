package com.brightloop.subscription.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.catalog.plan.Plan;
import com.brightloop.subscription.catalog.plan.PlanService;
import com.brightloop.subscription.catalog.product.Product;
import com.brightloop.subscription.catalog.product.ProductService;
import com.brightloop.subscription.catalog.product.ProductVariant;
import com.brightloop.subscription.catalog.product.ProductVariantRepository;
import com.brightloop.subscription.catalog.tax.Tax;
import com.brightloop.subscription.catalog.tax.TaxService;
import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.common.PageResponse;
import com.brightloop.subscription.common.sequence.SequenceService;
import com.brightloop.subscription.notification.NotificationService;
import com.brightloop.subscription.quotation.QuotationTemplate;
import com.brightloop.subscription.quotation.QuotationTemplateService;
import com.brightloop.subscription.quotation.TemplateLine;
import com.brightloop.subscription.security.CurrentUserService;
import com.brightloop.subscription.subscription.dto.SubscriptionFromTemplateRequest;
import com.brightloop.subscription.subscription.dto.SubscriptionLineRequest;
import com.brightloop.subscription.subscription.dto.SubscriptionRequest;
import com.brightloop.subscription.subscription.dto.SubscriptionResponse;
import com.brightloop.subscription.subscription.dto.SubscriptionSummary;
import com.brightloop.subscription.user.Role;
import com.brightloop.subscription.user.User;
import com.brightloop.subscription.user.UserRepository;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanService planService;
    private final ProductService productService;
    private final ProductVariantRepository variantRepository;
    private final TaxService taxService;
    private final QuotationTemplateService templateService;
    private final SequenceService sequenceService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            PlanService planService,
            ProductService productService,
            ProductVariantRepository variantRepository,
            TaxService taxService,
            QuotationTemplateService templateService,
            SequenceService sequenceService,
            CurrentUserService currentUserService,
            NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.planService = planService;
        this.productService = productService;
        this.variantRepository = variantRepository;
        this.taxService = taxService;
        this.templateService = templateService;
        this.sequenceService = sequenceService;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SubscriptionSummary> list(SubscriptionStatus status, Long customerId, Pageable pageable) {
        User current = currentUserService.get();
        Page<Subscription> page;
        if (current.getRole() == Role.CUSTOMER) {
            page = status == null
                    ? subscriptionRepository.findByCustomerId(current.getId(), pageable)
                    : subscriptionRepository.findByCustomerIdAndStatus(current.getId(), status, pageable);
        } else if (customerId != null) {
            page = status == null
                    ? subscriptionRepository.findByCustomerId(customerId, pageable)
                    : subscriptionRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else {
            page = status == null
                    ? subscriptionRepository.findAll(pageable)
                    : subscriptionRepository.findByStatus(status, pageable);
        }
        return PageResponse.from(page, SubscriptionSummary::from);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse get(Long id) {
        return SubscriptionResponse.from(loadAuthorized(id));
    }

    @Transactional
    public SubscriptionResponse create(SubscriptionRequest request) {
        User customer = resolveCustomer(request.customerId());
        Plan plan = planService.find(request.planId());
        assertPlanAvailable(plan, request.startDate());

        Subscription subscription = baseSubscription(customer, plan, request.startDate(), request.expiryDate(),
                request.paymentTermsDays(), request.currency());
        for (SubscriptionLineRequest line : request.lines()) {
            subscription.addLine(buildLine(line));
        }
        Subscription saved = subscriptionRepository.save(subscription);
        notifyCreated(saved);
        return SubscriptionResponse.from(saved);
    }

    @Transactional
    public SubscriptionResponse createFromTemplate(SubscriptionFromTemplateRequest request) {
        QuotationTemplate template = templateService.find(request.templateId());
        User customer = resolveCustomer(request.customerId());
        Plan plan = template.getPlan();
        assertPlanAvailable(plan, request.startDate());

        Subscription subscription = baseSubscription(customer, plan, request.startDate(), request.expiryDate(),
                request.paymentTermsDays(), request.currency());
        for (TemplateLine templateLine : template.getLines()) {
            subscription.addLine(SubscriptionLine.builder()
                    .product(templateLine.getProduct())
                    .variant(templateLine.getVariant())
                    .qty(templateLine.getQty())
                    .unitPrice(templateLine.getUnitPrice())
                    .amount(templateLine.getUnitPrice().multiply(BigDecimal.valueOf(templateLine.getQty())))
                    .hsnSacCode(resolveHsn(templateLine.getProduct(), null))
                    .cgstAmount(BigDecimal.ZERO)
                    .sgstAmount(BigDecimal.ZERO)
                    .igstAmount(BigDecimal.ZERO)
                    .build());
        }
        Subscription saved = subscriptionRepository.save(subscription);
        notifyCreated(saved);
        return SubscriptionResponse.from(saved);
    }

    @Transactional
    public SubscriptionResponse performAction(Long id, String action) {
        Subscription subscription = loadAuthorized(id);
        Plan plan = subscription.getPlan();
        String normalized = action == null ? "" : action.trim().toLowerCase();
        switch (normalized) {
            case "quote" -> {
                requireStatus(subscription, SubscriptionStatus.DRAFT);
                subscription.setStatus(SubscriptionStatus.QUOTATION);
            }
            case "confirm" -> {
                requireStatus(subscription, SubscriptionStatus.QUOTATION);
                subscription.setStatus(SubscriptionStatus.CONFIRMED);
            }
            case "activate" -> {
                requireStatus(subscription, SubscriptionStatus.CONFIRMED);
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscription.setNextBillingDate(subscription.getStartDate());
                notificationService.notify(subscription.getCustomer(), "SUBSCRIPTION_ACTIVE",
                        "Subscription " + subscription.getNumber() + " is active",
                        "Your " + plan.getName() + " subscription is now active. First billing is on "
                                + subscription.getNextBillingDate() + ".",
                        "SUBSCRIPTION", subscription.getId());
            }
            case "close" -> {
                if (!plan.isClosable()) {
                    throw ApiException.forbidden("This plan does not allow early closing");
                }
                if (subscription.getStatus() == SubscriptionStatus.CLOSED) {
                    throw ApiException.badRequest("Subscription is already closed");
                }
                subscription.setStatus(SubscriptionStatus.CLOSED);
                notificationService.notify(subscription.getCustomer(), "SUBSCRIPTION_CLOSED",
                        "Subscription " + subscription.getNumber() + " closed",
                        "Your " + plan.getName() + " subscription has been closed.",
                        "SUBSCRIPTION", subscription.getId());
            }
            case "pause" -> {
                if (!plan.isPausable()) {
                    throw ApiException.forbidden("This plan does not allow pausing");
                }
                requireStatus(subscription, SubscriptionStatus.ACTIVE);
                subscription.setStatus(SubscriptionStatus.PAUSED);
            }
            case "resume" -> {
                requireStatus(subscription, SubscriptionStatus.PAUSED);
                subscription.setStatus(SubscriptionStatus.ACTIVE);
            }
            case "renew" -> {
                if (!plan.isRenewable()) {
                    throw ApiException.forbidden("This plan does not allow renewal");
                }
                requireStatus(subscription, SubscriptionStatus.ACTIVE);
                LocalDate baseExpiry = subscription.getExpiryDate() == null
                        ? subscription.getStartDate()
                        : subscription.getExpiryDate();
                subscription.setExpiryDate(BillingPeriods.add(baseExpiry, plan.getBillingPeriod()));
                if (subscription.getNextBillingDate() != null) {
                    subscription.setNextBillingDate(
                            BillingPeriods.add(subscription.getNextBillingDate(), plan.getBillingPeriod()));
                }
            }
            default -> throw ApiException.badRequest("Unknown subscription action: " + action);
        }
        return SubscriptionResponse.from(subscription);
    }

    private void notifyCreated(Subscription subscription) {
        notificationService.notify(subscription.getCustomer(), "SUBSCRIPTION_CREATED",
                "Subscription " + subscription.getNumber() + " created",
                "A draft subscription for " + subscription.getPlan().getName() + " has been created for you.",
                "SUBSCRIPTION", subscription.getId());
    }

    private Subscription baseSubscription(User customer, Plan plan, LocalDate startDate, LocalDate expiryDate,
            int paymentTermsDays, String currency) {
        LocalDate expiry = expiryDate == null ? BillingPeriods.add(startDate, plan.getBillingPeriod()) : expiryDate;
        if (expiry.isBefore(startDate)) {
            throw ApiException.badRequest("Expiry date cannot be before start date");
        }
        return Subscription.builder()
                .number(sequenceService.nextNumber("subscription", "SUB"))
                .customer(customer)
                .plan(plan)
                .startDate(startDate)
                .expiryDate(expiry)
                .paymentTermsDays(paymentTermsDays)
                .status(SubscriptionStatus.DRAFT)
                .nextBillingDate(startDate)
                .currency(currency == null ? customer.getPreferredCurrency() : currency)
                .build();
    }

    private SubscriptionLine buildLine(SubscriptionLineRequest request) {
        Product product = productService.find(request.productId());
        ProductVariant variant = resolveVariant(request.variantId(), product);
        Tax tax = request.taxId() == null ? null : taxService.find(request.taxId());
        BigDecimal unitPrice = request.unitPrice() != null
                ? request.unitPrice()
                : product.getSalesPrice().add(variant == null ? BigDecimal.ZERO : variant.getExtraPrice());
        return SubscriptionLine.builder()
                .product(product)
                .variant(variant)
                .qty(request.qty())
                .unitPrice(unitPrice)
                .tax(tax)
                .amount(unitPrice.multiply(BigDecimal.valueOf(request.qty())))
                .hsnSacCode(resolveHsn(product, tax))
                .cgstAmount(BigDecimal.ZERO)
                .sgstAmount(BigDecimal.ZERO)
                .igstAmount(BigDecimal.ZERO)
                .build();
    }

    private ProductVariant resolveVariant(Long variantId, Product product) {
        if (variantId == null) {
            return null;
        }
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> ApiException.notFound("Product variant"));
        if (!variant.getProduct().getId().equals(product.getId())) {
            throw ApiException.badRequest("Variant does not belong to the selected product");
        }
        return variant;
    }

    private String resolveHsn(Product product, Tax tax) {
        if (product.getHsnSacCode() != null && !product.getHsnSacCode().isBlank()) {
            return product.getHsnSacCode();
        }
        return tax == null ? null : tax.getHsnSacCode();
    }

    private User resolveCustomer(Long requestedCustomerId) {
        User current = currentUserService.get();
        if (current.getRole() == Role.CUSTOMER) {
            if (requestedCustomerId != null && !requestedCustomerId.equals(current.getId())) {
                throw ApiException.forbidden("Customers can only create subscriptions for themselves");
            }
            return current;
        }
        if (requestedCustomerId == null) {
            throw ApiException.badRequest("customerId is required");
        }
        User customer = userRepository.findById(requestedCustomerId)
                .orElseThrow(() -> ApiException.notFound("Customer"));
        if (customer.getRole() != Role.CUSTOMER) {
            throw ApiException.badRequest("Selected user is not a customer");
        }
        return customer;
    }

    private void assertPlanAvailable(Plan plan, LocalDate startDate) {
        if (plan.getAvailableFrom() != null && startDate.isBefore(plan.getAvailableFrom())) {
            throw ApiException.badRequest("Plan is not available before " + plan.getAvailableFrom());
        }
        if (plan.getAvailableTo() != null && startDate.isAfter(plan.getAvailableTo())) {
            throw ApiException.badRequest("Plan is not available after " + plan.getAvailableTo());
        }
    }

    private void requireStatus(Subscription subscription, SubscriptionStatus expected) {
        if (subscription.getStatus() != expected) {
            throw ApiException.badRequest(
                    "Action requires status " + expected + " but subscription is " + subscription.getStatus());
        }
    }

    private Subscription loadAuthorized(Long id) {
        User current = currentUserService.get();
        if (current.getRole() == Role.CUSTOMER) {
            return subscriptionRepository.findByIdAndCustomerId(id, current.getId())
                    .orElseThrow(() -> ApiException.notFound("Subscription"));
        }
        return subscriptionRepository.findById(id).orElseThrow(() -> ApiException.notFound("Subscription"));
    }

    public List<Subscription> dueForBilling(LocalDate today) {
        return subscriptionRepository.findByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus.ACTIVE, today);
    }

    public Subscription save(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public Subscription findEntity(Long id) {
        return subscriptionRepository.findById(id).orElseThrow(() -> ApiException.notFound("Subscription"));
    }

    @Transactional(readOnly = true)
    public List<Subscription> listAll() {
        return new ArrayList<>(subscriptionRepository.findAll());
    }
}
