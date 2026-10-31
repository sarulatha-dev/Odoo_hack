package com.brightloop.subscription.catalog.discount;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByAppliesTo(DiscountScope appliesTo);

    boolean existsByNameIgnoreCase(String name);
}
