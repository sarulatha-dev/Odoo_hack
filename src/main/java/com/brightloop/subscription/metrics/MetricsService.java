package com.brightloop.subscription.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.Invoice;
import com.brightloop.subscription.billing.InvoiceRepository;
import com.brightloop.subscription.billing.InvoiceStatus;
import com.brightloop.subscription.catalog.plan.Plan;
import com.brightloop.subscription.catalog.plan.PlanRepository;
import com.brightloop.subscription.catalog.plan.PlanTier;
import com.brightloop.subscription.config.AppProperties;
import com.brightloop.subscription.metrics.dto.FunnelResponse;
import com.brightloop.subscription.metrics.dto.MetricsResponse;
import com.brightloop.subscription.metrics.dto.PlanMetricResponse;
import com.brightloop.subscription.metrics.dto.TierDistribution;
import com.brightloop.subscription.subscription.Subscription;
import com.brightloop.subscription.subscription.SubscriptionRepository;
import com.brightloop.subscription.subscription.SubscriptionStatus;

@Service
public class MetricsService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PlanViewRepository planViewRepository;
    private final AppProperties properties;

    public MetricsService(PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository,
            PlanViewRepository planViewRepository,
            AppProperties properties) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.planViewRepository = planViewRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public MetricsResponse plans() {
        List<Plan> plans = planRepository.findAll();
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();

        Map<Long, long[]> counts = new HashMap<>();
        Map<PlanTier, Long> activeByTier = new EnumMap<>(PlanTier.class);
        FunnelResponse funnel = funnel(subscriptions);
        for (Subscription subscription : subscriptions) {
            long[] values = counts.computeIfAbsent(subscription.getPlan().getId(), key -> new long[3]);
            values[0]++;
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                values[1]++;
                activeByTier.merge(subscription.getPlan().getTier(), 1L, Long::sum);
            }
            if (subscription.getStatus() == SubscriptionStatus.CLOSED) {
                values[2]++;
            }
        }

        Map<Long, BigDecimal> revenue = new HashMap<>();
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
                continue;
            }
            BigDecimal amount = invoice.getSubtotal().multiply(invoice.getExchangeRate());
            revenue.merge(invoice.getSubscription().getPlan().getId(), amount, BigDecimal::add);
        }

        Map<Long, Long> views = grouped(PlanViewAction.VIEWED);
        Map<Long, Long> selections = grouped(PlanViewAction.SELECTED);
        BigDecimal threshold = properties.metrics() == null
                || properties.metrics().underperformingChurnThreshold() == null
                        ? new BigDecimal("0.20")
                        : properties.metrics().underperformingChurnThreshold();

        List<PlanMetricResponse> metrics = new ArrayList<>();
        for (Plan plan : plans) {
            long[] values = counts.getOrDefault(plan.getId(), new long[3]);
            long total = values[0];
            long active = values[1];
            long closed = values[2];
            BigDecimal churnRate = total == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(closed * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
            BigDecimal churnFraction = total == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(closed).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
            long viewCount = views.getOrDefault(plan.getId(), 0L);
            long selectionCount = selections.getOrDefault(plan.getId(), 0L);
            BigDecimal selectionRate = viewCount == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(selectionCount * 100.0 / viewCount).setScale(2, RoundingMode.HALF_UP);
            metrics.add(new PlanMetricResponse(
                    plan.getId(),
                    plan.getName(),
                    plan.getTier().name(),
                    plan.getPrice(),
                    plan.getBillingPeriod().name(),
                    active,
                    total,
                    closed,
                    churnRate,
                    revenue.getOrDefault(plan.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                    viewCount,
                    selectionCount,
                    selectionRate,
                    total > 0 && churnFraction.compareTo(threshold) >= 0));
        }

        List<PlanMetricResponse> topBySubscribers = metrics.stream()
                .sorted(Comparator.comparingLong(PlanMetricResponse::activeSubscribers).reversed()
                        .thenComparing(PlanMetricResponse::name))
                .limit(5)
                .toList();
        List<PlanMetricResponse> topByRevenue = metrics.stream()
                .sorted(Comparator.comparing(PlanMetricResponse::revenueInBase).reversed()
                        .thenComparing(PlanMetricResponse::name))
                .limit(5)
                .toList();
        List<PlanMetricResponse> underperforming = metrics.stream()
                .filter(PlanMetricResponse::underperforming)
                .toList();

        long totalViews = planViewRepository.countByAction(PlanViewAction.VIEWED);
        long totalSelections = planViewRepository.countByAction(PlanViewAction.SELECTED);
        long highViews = metrics.stream().filter(metric -> "HIGH".equals(metric.tier()))
                .mapToLong(PlanMetricResponse::views).sum();
        long highSelections = metrics.stream().filter(metric -> "HIGH".equals(metric.tier()))
                .mapToLong(PlanMetricResponse::selections).sum();
        BigDecimal highSelectionRate = highViews == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(highSelections * 100.0 / highViews).setScale(2, RoundingMode.HALF_UP);

        TierDistribution tierDistribution = new TierDistribution(
                activeByTier.getOrDefault(PlanTier.LOW, 0L),
                activeByTier.getOrDefault(PlanTier.MEDIUM, 0L),
                activeByTier.getOrDefault(PlanTier.HIGH, 0L),
                activeByTier.values().stream().mapToLong(Long::longValue).sum());

        return new MetricsResponse(metrics, tierDistribution, funnel, topBySubscribers, topByRevenue,
                underperforming, totalViews, totalSelections, highViews, highSelections, highSelectionRate);
    }

    private Map<Long, Long> grouped(PlanViewAction action) {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : planViewRepository.countByActionGrouped(action)) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    private FunnelResponse funnel(List<Subscription> subscriptions) {
        Map<SubscriptionStatus, Long> counts = new EnumMap<>(SubscriptionStatus.class);
        for (Subscription subscription : subscriptions) {
            counts.merge(subscription.getStatus(), 1L, Long::sum);
        }
        return new FunnelResponse(
                counts.getOrDefault(SubscriptionStatus.DRAFT, 0L),
                counts.getOrDefault(SubscriptionStatus.QUOTATION, 0L),
                counts.getOrDefault(SubscriptionStatus.CONFIRMED, 0L),
                counts.getOrDefault(SubscriptionStatus.ACTIVE, 0L),
                counts.getOrDefault(SubscriptionStatus.PAUSED, 0L),
                counts.getOrDefault(SubscriptionStatus.CLOSED, 0L));
    }
}
