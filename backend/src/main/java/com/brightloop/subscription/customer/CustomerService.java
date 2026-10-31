package com.brightloop.subscription.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.Invoice;
import com.brightloop.subscription.billing.InvoiceRepository;
import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.customer.dto.CustomerRequest;
import com.brightloop.subscription.customer.dto.CustomerResponse;
import com.brightloop.subscription.customer.dto.CustomerResponse.ActivityItem;
import com.brightloop.subscription.customer.dto.CustomerResponse.FinancialSummary;
import com.brightloop.subscription.customer.dto.CustomerResponse.SubscriptionSummary;
import com.brightloop.subscription.payment.Payment;
import com.brightloop.subscription.payment.PaymentRepository;
import com.brightloop.subscription.subscription.Subscription;
import com.brightloop.subscription.subscription.SubscriptionRepository;
import com.brightloop.subscription.subscription.SubscriptionStatus;
import com.brightloop.subscription.user.IndianState;
import com.brightloop.subscription.user.Role;
import com.brightloop.subscription.user.User;
import com.brightloop.subscription.user.UserRepository;

@Service
public class CustomerService {

    private static final String DEFAULT_PASSWORD = "Customer@12345";

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(UserRepository userRepository,
            SubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> list() {
        return userRepository.findByRoleOrderByFullNameAsc(Role.CUSTOMER).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Email is already registered");
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(blankToNull(request.password()) == null
                        ? DEFAULT_PASSWORD
                        : request.password()))
                .role(Role.CUSTOMER)
                .autopayEnabled(false)
                .active(isActive(request.status()))
                .build();
        applyProfile(user, request);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        User user = find(id);
        String email = normalize(request.email());
        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw ApiException.conflict("Email is already registered");
            }
        });
        user.setEmail(email);
        String password = blankToNull(request.password());
        if (password != null) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        user.setActive(isActive(request.status()));
        applyProfile(user, request);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = find(id);
        boolean referenced = subscriptionRepository.countByCustomerId(id) > 0
                || invoiceRepository.existsByCustomerId(id);
        if (referenced) {
            user.setActive(false);
            userRepository.save(user);
            return;
        }
        userRepository.delete(user);
    }

    private User find(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Customer"));
        if (user.getRole() != Role.CUSTOMER) {
            throw ApiException.notFound("Customer");
        }
        return user;
    }

    private void applyProfile(User user, CustomerRequest request) {
        user.setFullName(request.fullName().trim());
        user.setCompanyName(blankToNull(request.companyName()));
        user.setPhone(blankToNull(request.phone()));
        user.setCountry(blankToNull(request.country()));
        user.setCustomerType(blankToNull(request.customerType()));
        user.setBillingAddress(blankToNull(request.billingAddress()));
        user.setCity(blankToNull(request.city()));
        user.setPostalCode(blankToNull(request.postalCode()));
        user.setBillingCountry(blankToNull(request.billingCountry()));
        user.setGstin(blankToNull(request.gstin()));
        user.setTaxRegistrationType(blankToNull(request.taxRegistrationType()));
        user.setNotes(blankToNull(request.notes()));
        user.setState(parseState(request.state()));
        user.setPreferredCurrency(request.preferredCurrency() == null
                ? "INR"
                : request.preferredCurrency().toUpperCase(Locale.ROOT));
    }

    private CustomerResponse toResponse(User user) {
        long total = subscriptionRepository.countByCustomerId(user.getId());
        long active = subscriptionRepository.countByCustomerIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE);
        long cancelled = subscriptionRepository.countByCustomerIdAndStatus(user.getId(), SubscriptionStatus.CLOSED);
        List<String> plans = subscriptionRepository.findDistinctPlanNamesByCustomerId(user.getId());

        BigDecimal invoiced = invoiceRepository.sumInvoicedInBaseByCustomer(user.getId());
        BigDecimal paid = paymentRepository.sumInBase(null, null, user.getId(), null, null, null);
        BigDecimal outstanding = invoiced.subtract(paid).max(BigDecimal.ZERO);
        BigDecimal overdue = invoiceRepository.sumOverdueInBaseByCustomer(user.getId(), LocalDate.now());

        List<ActivityItem> activity = new ArrayList<>();
        activity.add(new ActivityItem("customer_created", "Customer account created", toDate(user)));
        subscriptionRepository.findTop5ByCustomerIdOrderByCreatedAtDesc(user.getId()).stream()
                .limit(3)
                .forEach(subscription -> activity.add(new ActivityItem("subscription_created",
                        "Subscription " + subscription.getNumber() + " created",
                        toDate(subscription))));
        invoiceRepository.findTop5ByCustomerIdOrderByIssueDateDesc(user.getId()).stream()
                .limit(3)
                .forEach(invoice -> activity.add(new ActivityItem("invoice_generated",
                        "Invoice " + invoice.getNumber() + " generated", invoice.getIssueDate())));
        paymentRepository.findTop5ByInvoiceCustomerIdOrderByPaidOnDesc(user.getId()).stream()
                .limit(3)
                .forEach(payment -> activity.add(new ActivityItem("payment_received",
                        "Payment " + payment.getAmount() + " " + payment.getInvoice().getCurrency()
                                + " received for " + payment.getInvoice().getNumber(),
                        payment.getPaidOn())));
        activity.sort(Comparator.comparing(ActivityItem::date).reversed());

        String fullName = user.getFullName() == null ? "" : user.getFullName().trim();
        int split = fullName.indexOf(' ');
        String firstName = split < 0 ? fullName : fullName.substring(0, split);
        String lastName = split < 0 ? "" : fullName.substring(split + 1);

        return new CustomerResponse(
                user.getId(),
                user.getEmail(),
                fullName,
                firstName,
                lastName,
                user.getCompanyName(),
                user.getPhone(),
                user.getCountry(),
                user.getCustomerType() == null ? "Individual" : user.getCustomerType(),
                user.getBillingAddress(),
                user.getCity(),
                user.getState() == null ? null : user.getState().label(),
                user.getPostalCode(),
                user.getBillingCountry(),
                user.getGstin(),
                user.getTaxRegistrationType() == null ? "Unregistered" : user.getTaxRegistrationType(),
                user.getNotes(),
                user.isActive(),
                user.isActive() ? "Active" : "Inactive",
                user.getPreferredCurrency(),
                user.getMonthlyBudget(),
                user.isAutopayEnabled(),
                user.getAutopayMethod(),
                user.getCreatedAt(),
                new SubscriptionSummary(total, active, cancelled, plans),
                new FinancialSummary(invoiced, paid, outstanding, overdue, "INR"),
                activity.stream().limit(8).toList());
    }

    private LocalDate toDate(User user) {
        return user.getCreatedAt() == null ? LocalDate.now()
                : user.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private LocalDate toDate(Subscription subscription) {
        return subscription.getCreatedAt() == null ? LocalDate.now()
                : subscription.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private LocalDate toDate(Invoice invoice) {
        return invoice.getCreatedAt() == null ? LocalDate.now()
                : invoice.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private LocalDate toDate(Payment payment) {
        return payment.getPaidOn();
    }

    private boolean isActive(String status) {
        return status == null || !status.equalsIgnoreCase("inactive");
    }

    private IndianState parseState(String value) {
        String raw = blankToNull(value);
        if (raw == null) {
            return null;
        }
        try {
            return IndianState.valueOf(raw.toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
