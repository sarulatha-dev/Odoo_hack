package com.brightloop.subscription.quotation;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.catalog.plan.Plan;
import com.brightloop.subscription.catalog.plan.PlanService;
import com.brightloop.subscription.catalog.product.Product;
import com.brightloop.subscription.catalog.product.ProductService;
import com.brightloop.subscription.catalog.product.ProductVariant;
import com.brightloop.subscription.catalog.product.ProductVariantRepository;
import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.quotation.dto.TemplateLineRequest;
import com.brightloop.subscription.quotation.dto.TemplateRequest;
import com.brightloop.subscription.quotation.dto.TemplateResponse;

@Service
public class QuotationTemplateService {

    private final QuotationTemplateRepository templateRepository;
    private final PlanService planService;
    private final ProductService productService;
    private final ProductVariantRepository variantRepository;

    public QuotationTemplateService(QuotationTemplateRepository templateRepository,
            PlanService planService,
            ProductService productService,
            ProductVariantRepository variantRepository) {
        this.templateRepository = templateRepository;
        this.planService = planService;
        this.productService = productService;
        this.variantRepository = variantRepository;
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> list() {
        return templateRepository.findAll().stream().map(TemplateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TemplateResponse get(Long id) {
        return TemplateResponse.from(find(id));
    }

    @Transactional
    public TemplateResponse create(TemplateRequest request) {
        if (templateRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A quotation template with this name already exists");
        }
        Plan plan = planService.find(request.planId());
        QuotationTemplate template = QuotationTemplate.builder()
                .name(request.name().trim())
                .validityDays(request.validityDays())
                .plan(plan)
                .build();
        applyLines(template, request.lines());
        return TemplateResponse.from(templateRepository.save(template));
    }

    @Transactional
    public TemplateResponse update(Long id, TemplateRequest request) {
        QuotationTemplate template = find(id);
        if (!template.getName().equalsIgnoreCase(request.name().trim())
                && templateRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A quotation template with this name already exists");
        }
        template.setName(request.name().trim());
        template.setValidityDays(request.validityDays());
        template.setPlan(planService.find(request.planId()));
        template.getLines().clear();
        applyLines(template, request.lines());
        return TemplateResponse.from(template);
    }

    @Transactional
    public void delete(Long id) {
        templateRepository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public QuotationTemplate find(Long id) {
        return templateRepository.findById(id).orElseThrow(() -> ApiException.notFound("Quotation template"));
    }

    private void applyLines(QuotationTemplate template, List<TemplateLineRequest> lines) {
        for (TemplateLineRequest request : lines) {
            Product product = productService.find(request.productId());
            ProductVariant variant = resolveVariant(request.variantId(), product);
            BigDecimal unitPrice = request.unitPrice() != null
                    ? request.unitPrice()
                    : product.getSalesPrice().add(variant == null ? BigDecimal.ZERO : variant.getExtraPrice());
            template.addLine(TemplateLine.builder()
                    .product(product)
                    .variant(variant)
                    .qty(request.qty())
                    .unitPrice(unitPrice)
                    .build());
        }
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
}
