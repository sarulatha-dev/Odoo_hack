package com.brightloop.subscription.catalog.discount;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.catalog.discount.dto.DiscountRequest;
import com.brightloop.subscription.catalog.discount.dto.DiscountResponse;
import com.brightloop.subscription.catalog.product.Product;
import com.brightloop.subscription.catalog.product.ProductRepository;
import com.brightloop.subscription.common.ApiException;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    public DiscountService(DiscountRepository discountRepository, ProductRepository productRepository) {
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<DiscountResponse> list() {
        return discountRepository.findAll().stream().map(DiscountResponse::from).toList();
    }

    @Transactional
    public DiscountResponse create(DiscountRequest request) {
        if (discountRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A discount with this name already exists");
        }
        validate(request);
        Discount discount = Discount.builder()
                .name(request.name().trim())
                .type(request.type())
                .value(request.value())
                .minAmount(request.minAmount())
                .minQty(request.minQty())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .maxUses(request.maxUses())
                .usedCount(0)
                .appliesTo(request.appliesTo())
                .product(resolveProduct(request))
                .build();
        return DiscountResponse.from(discountRepository.save(discount));
    }

    @Transactional
    public DiscountResponse update(Long id, DiscountRequest request) {
        Discount discount = find(id);
        if (!discount.getName().equalsIgnoreCase(request.name().trim())
                && discountRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A discount with this name already exists");
        }
        validate(request);
        discount.setName(request.name().trim());
        discount.setType(request.type());
        discount.setValue(request.value());
        discount.setMinAmount(request.minAmount());
        discount.setMinQty(request.minQty());
        discount.setStartDate(request.startDate());
        discount.setEndDate(request.endDate());
        discount.setMaxUses(request.maxUses());
        discount.setAppliesTo(request.appliesTo());
        discount.setProduct(resolveProduct(request));
        return DiscountResponse.from(discount);
    }

    @Transactional
    public void delete(Long id) {
        discountRepository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public Discount find(Long id) {
        return discountRepository.findById(id).orElseThrow(() -> ApiException.notFound("Discount"));
    }

    private void validate(DiscountRequest request) {
        if (request.startDate() != null && request.endDate() != null
                && request.endDate().isBefore(request.startDate())) {
            throw ApiException.badRequest("Discount end date cannot be before start date");
        }
        if (request.type() == DiscountType.PERCENT && request.value().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            throw ApiException.badRequest("Percentage discount cannot exceed 100");
        }
        if (request.appliesTo() == DiscountScope.PRODUCT && request.productId() == null) {
            throw ApiException.badRequest("A product is required when the discount applies to a product");
        }
    }

    private Product resolveProduct(DiscountRequest request) {
        if (request.appliesTo() != DiscountScope.PRODUCT || request.productId() == null) {
            return null;
        }
        return productRepository.findById(request.productId())
                .orElseThrow(() -> ApiException.notFound("Product"));
    }
}
