package com.brightloop.subscription.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.brightloop.subscription.catalog.discount.Discount;
import com.brightloop.subscription.catalog.discount.DiscountRepository;
import com.brightloop.subscription.catalog.discount.DiscountScope;
import com.brightloop.subscription.catalog.discount.DiscountType;

@Component
public class DiscountEngine {

    public record LineGross(Long productId, BigDecimal gross, int qty) {
    }

    public record Application(Discount discount, BigDecimal amount) {
    }

    private final DiscountRepository discountRepository;

    public DiscountEngine(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public List<Application> evaluate(List<LineGross> lines, LocalDate today, BigDecimal exchangeRate) {
        List<Application> applications = new ArrayList<>();
        BigDecimal totalGross = lines.stream().map(LineGross::gross).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalGross.signum() <= 0) {
            return applications;
        }

        for (Discount discount : discountRepository.findAll()) {
            if (!isValid(discount, today)) {
                continue;
            }

            BigDecimal base;
            int qty;
            if (discount.getAppliesTo() == DiscountScope.SUBSCRIPTION) {
                base = totalGross;
                qty = lines.stream().mapToInt(LineGross::qty).sum();
            } else {
                Long productId = discount.getProduct() == null ? null : discount.getProduct().getId();
                List<LineGross> matching = lines.stream()
                        .filter(line -> productId != null && productId.equals(line.productId()))
                        .toList();
                base = matching.stream().map(LineGross::gross).reduce(BigDecimal.ZERO, BigDecimal::add);
                qty = matching.stream().mapToInt(LineGross::qty).sum();
            }

            if (base.signum() <= 0) {
                continue;
            }
            if (discount.getMinAmount() != null
                    && base.compareTo(convert(discount.getMinAmount(), exchangeRate)) < 0) {
                continue;
            }
            if (discount.getMinQty() != null && qty < discount.getMinQty()) {
                continue;
            }

            BigDecimal amount = discount.getType() == DiscountType.FIXED
                    ? convert(discount.getValue(), exchangeRate)
                    : base.multiply(discount.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            amount = amount.min(base);
            if (amount.signum() > 0) {
                applications.add(new Application(discount, amount));
            }
        }

        BigDecimal totalDiscount = applications.stream().map(Application::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDiscount.compareTo(totalGross) > 0) {
            BigDecimal factor = totalGross.divide(totalDiscount, 6, RoundingMode.HALF_UP);
            applications = applications.stream()
                    .map(app -> new Application(app.discount(),
                            app.amount().multiply(factor).setScale(2, RoundingMode.HALF_UP)))
                    .toList();
        }
        return applications;
    }

    private boolean isValid(Discount discount, LocalDate today) {
        if (discount.getStartDate() != null && today.isBefore(discount.getStartDate())) {
            return false;
        }
        if (discount.getEndDate() != null && today.isAfter(discount.getEndDate())) {
            return false;
        }
        return discount.getMaxUses() == null || discount.getUsedCount() < discount.getMaxUses();
    }

    private BigDecimal convert(BigDecimal amount, BigDecimal exchangeRate) {
        return amount.divide(exchangeRate, 2, RoundingMode.HALF_UP);
    }
}
