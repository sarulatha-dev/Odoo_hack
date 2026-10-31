import { CalendarDays, CheckCircle2, Pencil, Sparkles, Star } from 'lucide-react'
import { formatCurrency, formatDate } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import Modal from '../common/Modal.jsx'
import StatusBadge from '../common/StatusBadge.jsx'

const titleId = 'plan-details-title'

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

function DetailItem({ label, children }) {
  return (
    <div>
      <dt className="text-xs font-medium uppercase tracking-[0.08em] text-muted">{label}</dt>
      <dd className="mt-1.5 text-sm font-medium text-foreground">{children}</dd>
    </div>
  )
}

export default function PlanDetailsModal({ open, plan, onClose, onEdit }) {
  if (!open || !plan) {
    return null
  }

  const trialLabel =
    plan.trialDays > 0 ? `${plan.trialDays}-day free trial` : 'No trial period'

  return (
    <Modal open={open} onClose={onClose} titleId={titleId} maxWidthClassName="max-w-3xl">
      <div className="flex max-h-[calc(100dvh-2rem)] flex-col sm:max-h-[calc(100dvh-3rem)]">
        <div className="shrink-0 border-b border-border bg-background/30 px-5 py-5 pr-14 sm:px-6 sm:py-6 sm:pr-16">
          <div className="flex items-start gap-3">
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-brand/10 text-brand">
              <Sparkles className="h-5 w-5" aria-hidden="true" />
            </span>
            <div className="min-w-0">
              <div className="flex flex-wrap items-center gap-2">
                <h2
                  id={titleId}
                  className="break-words text-lg font-semibold tracking-tight text-foreground"
                >
                  {plan.name}
                </h2>
                {plan.popular && (
                  <Badge tone="brand" className="gap-1">
                    <Star className="h-3 w-3" aria-hidden="true" />
                    Most popular
                  </Badge>
                )}
              </div>
              <div className="mt-1.5 flex flex-wrap items-center gap-2 text-sm text-muted">
                <span className="font-mono text-xs font-semibold">{plan.code}</span>
                <span aria-hidden="true">•</span>
                <Badge tone={tierTones[plan.tier] ?? 'neutral'}>{plan.tier} tier</Badge>
                <StatusBadge status={plan.status} />
              </div>
            </div>
          </div>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto px-5 py-5 sm:px-6 sm:py-6">
          <div className="rounded-xl border border-brand/20 bg-brand/5 p-4">
            <p className="flex flex-wrap items-baseline gap-x-2 gap-y-1">
              <span className="text-3xl font-bold tracking-tight text-foreground">
                {formatCurrency(plan.price, plan.currency)}
              </span>
              <span className="text-sm font-medium text-muted">
                per {billingSuffixes[plan.billingCycle] ?? plan.billingCycle.toLowerCase()}
              </span>
            </p>
            <p className="mt-2 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-muted">
              <span className="font-semibold text-foreground">{plan.currency}</span>
              <span aria-hidden="true">•</span>
              <span>{plan.billingCycle} billing</span>
              <span aria-hidden="true">•</span>
              <span>{trialLabel}</span>
            </p>
          </div>

          <div className="mt-5">
            <h3 className="text-xs font-medium uppercase tracking-[0.08em] text-muted">
              Description
            </h3>
            <p className="mt-1.5 text-sm leading-6 text-foreground">{plan.description}</p>
          </div>

          <dl className="mt-5 grid gap-x-6 gap-y-5 sm:grid-cols-2">
            <DetailItem label="Plan Tier">
              <Badge tone={tierTones[plan.tier] ?? 'neutral'}>{plan.tier}</Badge>
            </DetailItem>

            <DetailItem label="Billing Cycle">{plan.billingCycle}</DetailItem>

            <DetailItem label="Trial Period">
              {plan.trialDays > 0 ? `${plan.trialDays} days` : 'None'}
            </DetailItem>

            <DetailItem label="Currency">{plan.currency}</DetailItem>

            <DetailItem label="Created Date">
              <span className="inline-flex items-center gap-1.5 text-muted">
                <CalendarDays className="h-3.5 w-3.5" aria-hidden="true" />
                {formatDate(plan.createdAt)}
              </span>
            </DetailItem>

            <DetailItem label="Last Updated">
              <span className="inline-flex items-center gap-1.5 text-muted">
                <CalendarDays className="h-3.5 w-3.5" aria-hidden="true" />
                {formatDate(plan.updatedAt)}
              </span>
            </DetailItem>
          </dl>

          <section className="mt-6 border-t border-border pt-5">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <h3 className="text-sm font-semibold text-foreground">Features Included</h3>
              <span className="text-xs text-muted">
                {plan.features.length} {plan.features.length === 1 ? 'feature' : 'features'}
              </span>
            </div>
            {plan.features.length > 0 ? (
              <ul className="mt-3 grid gap-2 sm:grid-cols-2">
                {plan.features.map((feature) => (
                  <li
                    key={feature}
                    className="flex items-start gap-2 rounded-lg border border-border bg-background/40 px-3 py-2.5 text-sm text-foreground"
                  >
                    <CheckCircle2
                      className="mt-0.5 h-4 w-4 shrink-0 text-brand"
                      aria-hidden="true"
                    />
                    <span>{feature}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="mt-2 text-sm text-muted">No features added to this plan.</p>
            )}
          </section>
        </div>

        <div className="flex shrink-0 flex-col-reverse gap-2 border-t border-border bg-background/30 px-5 py-4 sm:flex-row sm:justify-end sm:px-6">
          <button
            type="button"
            onClick={onClose}
            className="inline-flex h-10 items-center justify-center rounded-lg border border-border bg-card px-4 text-sm font-semibold text-muted transition-colors hover:border-brand/40 hover:text-foreground"
          >
            Close
          </button>
          <button
            type="button"
            onClick={onEdit}
            className="inline-flex h-10 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white transition-colors hover:bg-brand-dark"
          >
            <Pencil className="h-4 w-4" aria-hidden="true" />
            Edit Plan
          </button>
        </div>
      </div>
    </Modal>
  )
}
