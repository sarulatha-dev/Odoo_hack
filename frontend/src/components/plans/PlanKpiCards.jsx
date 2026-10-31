import { CheckCircle2, CircleOff, Layers, Star } from 'lucide-react'
import { formatNumber } from '../../utils/formatters.js'

function getSurfaceClassName(card) {
  if (card.featured) {
    return 'border-brand-dark/30 bg-linear-to-br from-brand-dark via-[#124e4a] to-navy text-white shadow-brand-dark/15 dark:border-brand/30'
  }

  if (card.tinted) {
    return 'border-rose-500/20 bg-rose-500/[0.035] hover:border-rose-500/30 dark:bg-rose-500/[0.06]'
  }

  return 'border-border bg-card hover:border-brand/30'
}

export default function PlanKpiCards({ plans }) {
  const activeCount = plans.filter((plan) => plan.status === 'Active').length
  const inactiveCount = plans.filter((plan) => plan.status === 'Inactive').length
  const popularPlan =
    plans.find((plan) => plan.popular) ??
    plans.find((plan) => plan.status === 'Active') ??
    plans[0] ??
    null

  const cards = [
    {
      label: 'Total Plans',
      value: formatNumber(plans.length),
      helper: 'Across all tiers',
      icon: Layers,
      featured: true,
    },
    {
      label: 'Active Plans',
      value: formatNumber(activeCount),
      helper: 'Currently available',
      icon: CheckCircle2,
      tone: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-300',
    },
    {
      label: 'Inactive Plans',
      value: formatNumber(inactiveCount),
      helper: 'Not available for purchase',
      icon: CircleOff,
      tone: 'bg-rose-500/10 text-rose-600 dark:text-rose-300',
      tinted: true,
    },
    {
      label: 'Most Popular Plan',
      value: popularPlan?.name ?? '—',
      helper: popularPlan
        ? `${popularPlan.tier} tier · ${popularPlan.billingCycle} billing`
        : 'Create a plan to track adoption',
      icon: Star,
      tone: 'bg-amber-500/10 text-amber-600 dark:text-amber-300',
      isName: true,
    },
  ]

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map((card) => {
        const Icon = card.icon

        return (
          <article
            key={card.label}
            className={`relative flex h-full flex-col overflow-hidden rounded-2xl border p-5 shadow-sm transition-all duration-200 hover:-translate-y-0.5 ${getSurfaceClassName(
              card,
            )}`}
          >
            {card.featured && (
              <span
                className="pointer-events-none absolute -right-8 -top-10 h-32 w-32 rounded-full bg-white/5 blur-2xl"
                aria-hidden="true"
              />
            )}

            <div className="relative flex items-start justify-between gap-4">
              <span
                className={`flex h-11 w-11 items-center justify-center rounded-xl ${
                  card.featured
                    ? 'bg-white/10 text-white ring-1 ring-inset ring-white/15'
                    : card.tone
                }`}
              >
                <Icon className="h-5 w-5" aria-hidden="true" />
              </span>
            </div>

            <div className="relative mt-4 flex-1">
              <p className={`text-sm font-medium ${card.featured ? 'text-white/70' : 'text-muted'}`}>
                {card.label}
              </p>
              <p
                className={`mt-1.5 font-bold tracking-tight ${
                  card.isName ? 'break-words text-2xl leading-tight' : 'text-3xl leading-none'
                } ${card.featured ? 'text-white' : 'text-foreground'}`}
              >
                {card.value}
              </p>
              <p className={`mt-3 text-xs ${card.featured ? 'text-white/70' : 'text-muted'}`}>
                {card.helper}
              </p>
            </div>
          </article>
        )
      })}
    </div>
  )
}
