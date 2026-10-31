package com.brightloop.subscription.catalog.tax;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<Tax, Long> {

    Optional<Tax> findFirstByDefaultTaxTrue();

    boolean existsByNameIgnoreCase(String name);
}
