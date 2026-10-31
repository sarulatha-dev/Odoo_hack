import {
  CalendarDays,
  CheckCircle2,
  Eye,
  Layers,
  Pencil,
  Plus,
  SearchX,
  Star,
  Trash2,
} from 'lucide-react'
import { formatCurrency, formatDate } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import SectionCard from '../common/SectionCard.jsx'
import StatusBadge from '../common/StatusBadge.jsx'

const tierTones = {
  Low: 'success',
  Medium: 'warning',
  High: 'brand',
}

const billingSuffixes = {
  Daily: 'day',
  Weekly: 'week',
  Monthly: 'month',
  Quarterly: 'quarter',
  'Half-yearly': 'half-year',
  Yearly: 'year',
}

const actionButtonClassName =
  'inline-flex h-9 w-9 items-center justify-center rounded-lg border border-border bg-card text-muted transition-colors'

export default function PlanCatalogue({
  plans,
  onView,
  onEdit,
  onDelete,
  onClearFilters,
  onAdd,
  hasActiveFilters,
}) {
  return (
    <SectionCard
      title="Plan Catalogue"
      description="Compare pricing, billing cycles, and included features."
      actions={
        <span className="text-[13px] font-medium text-muted">
          {plans.length} {plans.length === 1 ? 'plan' : 'plans'}
        </span>
      }
    >
      {plans.length === 0 ? (
        <div className="flex flex-col items-center justify-center px-4 py-12 text-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand/10 text-brand">
            {hasActiveFilters ? (
              <SearchX className="h-6 w-6" aria-hidden="true" />
            ) : (
              <Layers className="h-6 w-6" aria-hidden="true" />
            )}
          </span>
          <h3 className="mt-4 text-base font-semibold text-foreground">
            {hasActiveFilters ? 'No plans match your filters' : 'No plans yet'}
          </h3>
          <p className="mt-1.5 max-w-md text-sm text-muted">
            {hasActiveFilters
              ? 'Try a different search term or clear the current tier, billing, and status filters.'
              : 'Create your first demonstration plan to start building the catalogue.'}
          </p>
          {hasActiveFilters ? (
            <button
              type="button"
              onClick={onClearFilters}
              className="mt-5 inline-flex h-9 items-center justify-center rounded-lg border border-border bg-card px-4 text-sm font-semibold text-foreground transition-colors hover:border-brand/40 hover:text-brand"
            >
              Clear filters
            </button>
          ) : (
            <button
              type="button"
              onClick={onAdd}
              className="mt-5 inline-flex h-9 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white transition-colors hover:bg-brand-dark"
            >
              <Plus className="h-4 w-4" aria-hidden="true" />
              Create Plan
            </button>
          )}
        </div>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
          {plans.map((plan) => {
            const isPopular = Boolean(plan.popular)
            const trialLabel =
              plan.trialDays > 0 ? `${plan.trialDays}-day free trial` : 'No trial period'

            return (
              <article
                key={plan.id}
                className={`group relative flex h-full flex-col overflow-hidden rounded-2xl border bg-card p-5 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md ${
                  isPopular
                    ? 'border-brand/35 shadow-brand/5 dark:border-brand/40'
                    : 'border-border hover:border-brand/25'
                }`}
              >
                {isPopular && (
                  <span
                    className="pointer-events-none absolute inset-x-0 top-0 h-1 bg-linear-to-r from-brand via-brand/70 to-transparent"
                    aria-hidden="true"
                  />
                )}

                <div className="flex items-start justify-between gap-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <Badge tone={tierTones[plan.tier] ?? 'neutral'}>{plan.tier} tier</Badge>
                    {isPopular && (
                      <Badge tone="brand" className="gap-1">
                        <Star className="h-3 w-3" aria-hidden="true" />
                        Popular
                      </Badge>
                    )}
                  </div>
                  <StatusBadge status={plan.status} />
                </div>

                <div className="mt-4">
                  <p className="font-mono text-[11px] font-semibold tracking-wide text-muted">
                    {plan.code}
                  </p>
                  <h3 className="mt-1 text-lg font-bold tracking-tight text-foreground">
                    {plan.name}
                  </h3>
                  <p
                    className="mt-1.5 line-clamp-2 text-sm leading-6 text-muted"
                    title={plan.description}
                  >
                    {plan.description}
                  </p>
                </div>

                <div className="mt-4 rounded-xl border border-border/70 bg-background/60 px-4 py-3.5">
                  <p className="flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
                    <span className="text-2xl font-bold tracking-tight text-foreground">
                      {formatCurrency(plan.price, plan.currency)}
                    </span>
                    <span className="text-xs font-medium text-muted">
                      per {billingSuffixes[plan.billingCycle] ?? plan.billingCycle.toLowerCase()}
                    </span>
                  </p>
                  <p className="mt-1.5 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-muted">
                    <span className="font-semibold text-foreground">{plan.currency}</span>
                    <span aria-hidden="true">•</span>
                    <span>{plan.billingCycle}</span>
                    <span aria-hidden="true">•</span>
                    <span>{trialLabel}</span>
                  </p>
                </div>

                <div className="mt-4 flex-1">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.1em] text-muted">
                    Includes
                  </p>
                  <ul className="mt-2.5 space-y-1.5">
                    {plan.features.map((feature) => (
                      <li
                        key={feature}
                        className="flex items-start gap-2 text-sm leading-5 text-foreground"
                      >
                        <CheckCircle2
                          className="mt-0.5 h-3.5 w-3.5 shrink-0 text-brand"
                          aria-hidden="true"
                        />
                        <span>{feature}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <p className="mt-4 inline-flex items-center gap-1.5 text-xs text-muted">
                  <CalendarDays className="h-3.5 w-3.5" aria-hidden="true" />
                  Created {formatDate(plan.createdAt)}
                </p>

                <div className="mt-4 flex items-center gap-2 border-t border-border pt-4">
                  <button
                    type="button"
                    onClick={() => onView(plan)}
                    className="inline-flex h-9 flex-1 items-center justify-center gap-2 rounded-lg bg-brand/10 px-3 text-xs font-semibold text-brand transition-colors hover:bg-brand/15"
                  >
                    <Eye className="h-3.5 w-3.5" aria-hidden="true" />
                    View plan
                  </button>
                  <button
                    type="button"
                    onClick={() => onEdit(plan)}
                    aria-label={`Edit ${plan.name} plan`}
                    title="Edit plan"
                    className={`${actionButtonClassName} hover:border-sky-500/30 hover:bg-sky-500/10 hover:text-sky-600 dark:hover:text-sky-300`}
                  >
                    <Pencil className="h-4 w-4" aria-hidden="true" />
                  </button>
                  <button
                    type="button"
                    onClick={() => onDelete(plan)}
                    aria-label={`Delete ${plan.name} plan`}
                    title="Delete plan"
                    className={`${actionButtonClassName} hover:border-rose-500/30 hover:bg-rose-500/10 hover:text-rose-600 dark:hover:text-rose-300`}
                  >
                    <Trash2 className="h-4 w-4" aria-hidden="true" />
                  </button>
                </div>
              </article>
            )
          })}
        </div>
      )}
    </SectionCard>
  )
}
