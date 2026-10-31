package com.brightloop.subscription.catalog.plan;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.catalog.plan.dto.PlanRequest;
import com.brightloop.subscription.catalog.plan.dto.PlanResponse;
import com.brightloop.subscription.common.ApiException;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> list() {
        return planRepository.findAll().stream().map(PlanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse get(Long id) {
        return PlanResponse.from(find(id));
    }

    @Transactional
    public PlanResponse create(PlanRequest request) {
        if (planRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A plan with this name already exists");
        }
        Plan plan = Plan.builder()
                .name(request.name().trim())
                .tier(request.tier())
                .price(request.price())
                .billingPeriod(request.billingPeriod())
                .minQty(request.minQty())
                .availableFrom(request.availableFrom())
                .availableTo(request.availableTo())
                .autoClose(request.autoClose())
                .closable(request.closable())
                .pausable(request.pausable())
                .renewable(request.renewable())
                .features(request.features() == null ? new ArrayList<>() : new ArrayList<>(request.features()))
                .build();
        return PlanResponse.from(planRepository.save(plan));
    }

    @Transactional
    public PlanResponse update(Long id, PlanRequest request) {
        Plan plan = find(id);
        if (!plan.getName().equalsIgnoreCase(request.name().trim())
                && planRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw ApiException.conflict("A plan with this name already exists");
        }
        plan.setName(request.name().trim());
        plan.setTier(request.tier());
        plan.setPrice(request.price());
        plan.setBillingPeriod(request.billingPeriod());
        plan.setMinQty(request.minQty());
        plan.setAvailableFrom(request.availableFrom());
        plan.setAvailableTo(request.availableTo());
        plan.setAutoClose(request.autoClose());
        plan.setClosable(request.closable());
        plan.setPausable(request.pausable());
        plan.setRenewable(request.renewable());
        plan.getFeatures().clear();
        if (request.features() != null) {
            plan.getFeatures().addAll(request.features());
        }
        return PlanResponse.from(plan);
    }

    @Transactional
    public void delete(Long id) {
        planRepository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public Plan find(Long id) {
        return planRepository.findById(id).orElseThrow(() -> ApiException.notFound("Plan"));
    }
}
