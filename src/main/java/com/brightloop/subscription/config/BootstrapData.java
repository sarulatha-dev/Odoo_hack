package com.brightloop.subscription.config;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.catalog.discount.Discount;
import com.brightloop.subscription.catalog.discount.DiscountRepository;
import com.brightloop.subscription.catalog.discount.DiscountScope;
import com.brightloop.subscription.catalog.discount.DiscountType;
import com.brightloop.subscription.catalog.plan.BillingPeriod;
import com.brightloop.subscription.catalog.plan.Plan;
import com.brightloop.subscription.catalog.plan.PlanRepository;
import com.brightloop.subscription.catalog.plan.PlanTier;
import com.brightloop.subscription.catalog.product.Product;
import com.brightloop.subscription.catalog.product.ProductRepository;
import com.brightloop.subscription.catalog.product.ProductType;
import com.brightloop.subscription.catalog.product.ProductVariant;
import com.brightloop.subscription.catalog.tax.GstType;
import com.brightloop.subscription.catalog.tax.Tax;
import com.brightloop.subscription.catalog.tax.TaxRepository;
import com.brightloop.subscription.currency.CurrencyRate;
import com.brightloop.subscription.currency.CurrencyRateRepository;
import com.brightloop.subscription.user.IndianState;
import com.brightloop.subscription.user.Role;
import com.brightloop.subscription.user.User;
import com.brightloop.subscription.user.UserRepository;

@Component
@Order(1)
public class BootstrapData implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapData.class);

    private final UserRepository userRepository;
    private final CurrencyRateRepository currencyRateRepository;
    private final ProductRepository productRepository;
    private final PlanRepository planRepository;
    private final TaxRepository taxRepository;
    private final DiscountRepository discountRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public BootstrapData(UserRepository userRepository, CurrencyRateRepository currencyRateRepository,
            ProductRepository productRepository, PlanRepository planRepository, TaxRepository taxRepository,
            DiscountRepository discountRepository, PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:true}") boolean enabled) {
        this.userRepository = userRepository;
        this.currencyRateRepository = currencyRateRepository;
        this.productRepository = productRepository;
        this.planRepository = planRepository;
        this.taxRepository = taxRepository;
        this.discountRepository = discountRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        createIfMissing("admin@brightloop.test", "Admin@12345", "Brightloop Admin", Role.ADMIN, IndianState.KARNATAKA, "29ABCDE1234F1Z5");
        createIfMissing("staff@brightloop.test", "Staff@12345", "Brightloop Staff", Role.STAFF, IndianState.KARNATAKA, null);
        createIfMissing("customer@brightloop.test", "Customer@12345", "Demo Customer", Role.CUSTOMER, IndianState.KARNATAKA, "29AAAAA0000A1Z5");
        seedCurrency("USD", "83.000000");
        seedCurrency("EUR", "90.000000");
        seedCurrency("GBP", "105.000000");
        seedCurrency("AED", "22.600000");
        seedCurrency("SGD", "62.000000");
        seedCatalog();
    }

    private void seedCatalog() {
        Product license = ensureProduct("Odoo License", ProductType.GOODS, "1000.00", "400.00",
                "9983", "Per seat license");
        ensureVariant(license, "Brand", "Odoo", "560.00");
        ensureProduct("Implementation Support", ProductType.SERVICE, "800.00", "300.00",
                "9983", "Onboarding and implementation hours");
        ensureProduct("Analytics Add-on", ProductType.SERVICE, "600.00", "150.00",
                "9983", "Advanced reporting and dashboards");

        ensureTax("GST 18", "18.000", GstType.IGST, "9983", true);
        ensureTax("GST 5", "5.000", GstType.IGST, "9983", false);

        ensurePlan("Starter", PlanTier.LOW, "499.00", List.of("Email support", "1 project"),
                true, true, true, true);
        if (planRepository.findFirstByTier(PlanTier.MEDIUM).isEmpty()) {
            ensurePlan("Growth", PlanTier.MEDIUM, "1499.00", List.of("Billing", "CRM", "Reports"),
                    true, true, true, true);
        }
        if (planRepository.findFirstByTier(PlanTier.HIGH).isEmpty()) {
            ensurePlan("Scale", PlanTier.HIGH, "2499.00",
                    List.of("Billing", "CRM", "Reports", "Priority support", "Advanced analytics", "SSO"),
                    true, true, true, true);
        }

        ensureDiscount("Festive 15%", DiscountType.PERCENT, "15.00", "1000.00", 200,
                DiscountScope.SUBSCRIPTION);
        log.info("Catalog seed ready: products={}, plans={}, taxes={}, discounts={}",
                productRepository.count(), planRepository.count(), taxRepository.count(),
                discountRepository.count());
    }

    private Product ensureProduct(String name, ProductType type, String salesPrice, String costPrice,
            String hsnSacCode, String notes) {
        return productRepository.findFirstByNameIgnoreCase(name).orElseGet(() -> {
            Product product = Product.builder()
                    .name(name)
                    .type(type)
                    .salesPrice(new BigDecimal(salesPrice))
                    .costPrice(new BigDecimal(costPrice))
                    .hsnSacCode(hsnSacCode)
                    .notes(notes)
                    .build();
            productRepository.save(product);
            log.info("Seeded product {}", name);
            return product;
        });
    }

    private void ensureVariant(Product product, String attribute, String value, String extraPrice) {
        boolean exists = product.getVariants().stream()
                .anyMatch(variant -> variant.getAttribute().equalsIgnoreCase(attribute)
                        && variant.getValue().equalsIgnoreCase(value));
        if (exists) {
            return;
        }
        product.addVariant(ProductVariant.builder()
                .attribute(attribute)
                .value(value)
                .extraPrice(new BigDecimal(extraPrice))
                .build());
        productRepository.save(product);
        log.info("Seeded variant {}: {} for {}", attribute, value, product.getName());
    }

    private Tax ensureTax(String name, String percent, GstType gstType, String hsnSacCode, boolean defaultTax) {
        if (taxRepository.existsByNameIgnoreCase(name)) {
            return null;
        }
        Tax tax = Tax.builder()
                .name(name)
                .percent(new BigDecimal(percent))
                .gstType(gstType)
                .hsnSacCode(hsnSacCode)
                .defaultTax(defaultTax)
                .build();
        taxRepository.save(tax);
        log.info("Seeded tax {}", name);
        return tax;
    }

    private void ensurePlan(String name, PlanTier tier, String price, List<String> features,
            boolean autoClose, boolean closable, boolean pausable, boolean renewable) {
        if (planRepository.existsByNameIgnoreCase(name)) {
            return;
        }
        planRepository.save(Plan.builder()
                .name(name)
                .tier(tier)
                .price(new BigDecimal(price))
                .billingPeriod(BillingPeriod.MONTHLY)
                .minQty(1)
                .autoClose(autoClose)
                .closable(closable)
                .pausable(pausable)
                .renewable(renewable)
                .features(new java.util.ArrayList<>(features))
                .build());
        log.info("Seeded plan {} ({})", name, tier);
    }

    private void ensureDiscount(String name, DiscountType type, String value, String minAmount,
            Integer maxUses, DiscountScope scope) {
        if (discountRepository.existsByNameIgnoreCase(name)) {
            return;
        }
        discountRepository.save(Discount.builder()
                .name(name)
                .type(type)
                .value(new BigDecimal(value))
                .minAmount(new BigDecimal(minAmount))
                .maxUses(maxUses)
                .usedCount(0)
                .appliesTo(scope)
                .build());
        log.info("Seeded discount {}", name);
    }

    private void seedCurrency(String currency, String rate) {
        if (currencyRateRepository.existsById(currency)) {
            return;
        }
        currencyRateRepository.save(CurrencyRate.builder()
                .currency(currency)
                .rateToInr(new BigDecimal(rate))
                .updatedAt(java.time.Instant.now())
                .build());
        log.info("Seeded currency rate {} = {} INR", currency, rate);
    }

    private void createIfMissing(String email, String password, String name, Role role, IndianState state,
            String gstin) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(name)
                .role(role)
                .state(state)
                .gstin(gstin)
                .preferredCurrency("INR")
                .autopayEnabled(false)
                .active(true)
                .build());
        log.info("Seeded {} account: {}", role, email);
    }
}
