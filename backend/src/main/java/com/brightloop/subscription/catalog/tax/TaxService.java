package com.brightloop.subscription.catalog.tax;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.catalog.tax.dto.TaxRequest;
import com.brightloop.subscription.catalog.tax.dto.TaxResponse;
import com.brightloop.subscription.common.ApiException;

@Service
public class TaxService {

    private final TaxRepository taxRepository;

    public TaxService(TaxRepository taxRepository) {
        this.taxRepository = taxRepository;
    }

    @Transactional(readOnly = true)
    public List<TaxResponse> list() {
        return taxRepository.findAll().stream().map(TaxResponse::from).toList();
    }

    @Transactional
    public TaxResponse create(TaxRequest request) {
        if (taxRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A tax with this name already exists");
        }
        clearDefaultIfNeeded(request.defaultTax());
        Tax tax = Tax.builder()
                .name(request.name().trim())
                .percent(request.percent())
                .gstType(request.gstType())
                .hsnSacCode(request.hsnSacCode())
                .defaultTax(request.defaultTax())
                .build();
        return TaxResponse.from(taxRepository.save(tax));
    }

    @Transactional
    public TaxResponse update(Long id, TaxRequest request) {
        Tax tax = find(id);
        if (!tax.getName().equalsIgnoreCase(request.name().trim())
                && taxRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A tax with this name already exists");
        }
        clearDefaultIfNeeded(request.defaultTax());
        tax.setName(request.name().trim());
        tax.setPercent(request.percent());
        tax.setGstType(request.gstType());
        tax.setHsnSacCode(request.hsnSacCode());
        tax.setDefaultTax(request.defaultTax());
        return TaxResponse.from(tax);
    }

    @Transactional
    public void delete(Long id) {
        taxRepository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public Tax find(Long id) {
        return taxRepository.findById(id).orElseThrow(() -> ApiException.notFound("Tax"));
    }

    @Transactional(readOnly = true)
    public Tax resolveDefault() {
        return taxRepository.findFirstByDefaultTaxTrue().orElse(null);
    }

    private void clearDefaultIfNeeded(boolean isDefault) {
        if (isDefault) {
            taxRepository.findFirstByDefaultTaxTrue().ifPresent(existing -> existing.setDefaultTax(false));
        }
    }
}
