package com.brightloop.subscription.catalog.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.catalog.product.dto.ProductRequest;
import com.brightloop.subscription.catalog.product.dto.ProductResponse;
import com.brightloop.subscription.catalog.product.dto.VariantRequest;
import com.brightloop.subscription.common.ApiException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A product with this name already exists");
        }
        Product product = Product.builder()
                .name(request.name().trim())
                .type(request.type())
                .salesPrice(request.salesPrice())
                .costPrice(request.costPrice())
                .notes(request.notes())
                .hsnSacCode(request.hsnSacCode())
                .build();
        applyVariants(product, request.variants());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        if (!product.getName().equalsIgnoreCase(request.name().trim())
                && productRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A product with this name already exists");
        }
        product.setName(request.name().trim());
        product.setType(request.type());
        product.setSalesPrice(request.salesPrice());
        product.setCostPrice(request.costPrice());
        product.setNotes(request.notes());
        product.setHsnSacCode(request.hsnSacCode());
        product.getVariants().clear();
        applyVariants(product, request.variants());
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public Product find(Long id) {
        return productRepository.findById(id).orElseThrow(() -> ApiException.notFound("Product"));
    }

    private void applyVariants(Product product, List<VariantRequest> variants) {
        if (variants == null) {
            return;
        }
        for (VariantRequest variant : variants) {
            product.addVariant(ProductVariant.builder()
                    .attribute(variant.attribute().trim())
                    .value(variant.value().trim())
                    .extraPrice(variant.extraPrice())
                    .build());
        }
    }
}
