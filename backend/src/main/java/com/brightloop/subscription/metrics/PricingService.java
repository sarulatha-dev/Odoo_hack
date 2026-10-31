package com.brightloop.subscription.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.catalog.plan.BillingPeriod;
import com.brightloop.subscription.catalog.plan.Plan;
import com.brightloop.subscription.catalog.plan.PlanRepository;
import com.brightloop.subscription.catalog.plan.PlanTier;
import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.metrics.dto.PricingCardResponse;
import com.brightloop.subscription.metrics.dto.PricingResponse;
import com.brightloop.subscription.security.CurrentUserService;
import com.brightloop.subscription.user.User;

@Service
public class PricingService {

    private final PlanRepository planRepository;
    private final PlanViewRepository planViewRepository;
    private final CurrentUserService currentUserService;

    public PricingService(PlanRepository planRepository, PlanViewRepository planViewRepository,
            CurrentUserService currentUserService) {
        this.planRepository = planRepository;
        this.planViewRepository = planViewRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public PricingResponse pricing() {
        LocalDate today = LocalDate.now();
        List<Plan> plans = planRepository.findAll().stream()
                .filter(plan -> plan.getAvailableFrom() == null || !plan.getAvailableFrom().isAfter(today))
                .filter(plan -> plan.getAvailableTo() == null || !plan.getAvailableTo().isBefore(today))
                .toList();

        Map<PlanTier, Plan> cheapest = new EnumMap<>(PlanTier.class);
        for (Plan plan : plans) {
            cheapest.merge(plan.getTier(), plan,
                    (current, candidate) -> current.getPrice().compareTo(candidate.getPrice()) <= 0 ? current : candidate);
        }

        Plan low = cheapest.get(PlanTier.LOW);
        Plan medium = cheapest.get(PlanTier.MEDIUM);
        Plan high = cheapest.get(PlanTier.HIGH);

        List<PricingCardResponse> cards = new ArrayList<>();
        if (low != null) {
            cards.add(card(low, false, nudgeLow(low, medium)));
        }
        if (medium != null) {
            cards.add(card(medium, true, nudgeMedium(medium, high)));
        }
        if (high != null) {
            cards.add(card(high, false, nudgeHigh(high, medium)));
        }
        return new PricingResponse("INR", cards);
    }

    @Transactional
    public void logView(Long planId) {
        log(planId, PlanViewAction.VIEWED);
    }

    @Transactional
    public void logSelection(Long planId) {
        log(planId, PlanViewAction.SELECTED);
    }

    private void log(Long planId, PlanViewAction action) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> ApiException.notFound("Plan"));
        User user = currentUserService.get();
        planViewRepository.save(PlanView.builder()
                .plan(plan)
                .user(user)
                .action(action)
                .build());
    }

    private PricingCardResponse card(Plan plan, boolean highlighted, String nudgeMessage) {
        return new PricingCardResponse(
                plan.getId(),
                plan.getName(),
                plan.getTier().name(),
                plan.getPrice(),
                plan.getBillingPeriod().name(),
                List.copyOf(plan.getFeatures()),
                highlighted,
                nudgeMessage);
    }

    private String nudgeLow(Plan low, Plan medium) {
        if (medium == null) {
            return null;
        }
        BigDecimal delta = medium.getPrice().subtract(low.getPrice());
        List<String> extras = extraFeatures(low, medium);
        if (delta.signum() <= 0 || extras.isEmpty()) {
            return null;
        }
        return "Medium adds " + String.join(", ", extras) + " for INR " + delta + " more per "
                + periodLabel(low.getBillingPeriod()) + ".";
    }

    private String nudgeMedium(Plan medium, Plan high) {
        if (high == null) {
            return null;
        }
        BigDecimal delta = high.getPrice().subtract(medium.getPrice());
        if (delta.signum() <= 0) {
            return "High is available at the same or lower price than Medium.";
        }
        List<String> extras = extraFeatures(medium, high);
        String period = periodLabel(medium.getBillingPeriod());
        if (extras.isEmpty()) {
            return "Only INR " + delta + " more per " + period + " for High.";
        }
        return "Only INR " + delta + " more per " + period + " for High: you get "
                + String.join(", ", extras) + " that Medium doesn't include.";
    }

    private String nudgeHigh(Plan high, Plan medium) {
        if (medium == null) {
            return null;
        }
        BigDecimal delta = high.getPrice().subtract(medium.getPrice());
        if (delta.signum() <= 0) {
            return "High costs the same or less than Medium.";
        }
        List<String> extras = extraFeatures(medium, high);
        if (extras.isEmpty()) {
            return "High adds capacity beyond Medium.";
        }
        BigDecimal perFeature = delta.divide(BigDecimal.valueOf(extras.size()), 2, RoundingMode.HALF_UP);
        return "Each extra High feature costs about INR " + perFeature + " per "
                + periodLabel(high.getBillingPeriod()) + " more than Medium.";
    }

    private List<String> extraFeatures(Plan from, Plan to) {
        List<String> fromFeatures = from.getFeatures().stream()
                .map(feature -> feature.toLowerCase())
                .toList();
        return to.getFeatures().stream()
                .filter(feature -> !fromFeatures.contains(feature.toLowerCase()))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private String periodLabel(BillingPeriod period) {
        return switch (period) {
            case DAILY -> "day";
            case WEEKLY -> "week";
            case MONTHLY -> "month";
            case YEARLY -> "year";
        };
    }
}
