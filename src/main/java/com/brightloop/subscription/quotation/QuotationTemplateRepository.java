package com.brightloop.subscription.quotation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationTemplateRepository extends JpaRepository<QuotationTemplate, Long> {

    boolean existsByNameIgnoreCase(String name);
}
