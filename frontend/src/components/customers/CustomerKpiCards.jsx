import { AlertTriangle, CalendarPlus, CheckCircle2, Users } from 'lucide-react'
import { formatNumber } from '../../utils/formatters.js'

function getSurfaceClassName(card) {
  if (card.featured) {
    return 'border-brand-dark/30 bg-linear-to-br from-brand-dark via-[#124e4a] to-navy text-white shadow-brand-dark/15'
  }

  if (card.tinted) {
    return 'border-rose-500/20 bg-rose-500/[0.035] hover:border-rose-500/30 dark:bg-rose-500/[0.06]'
  }

  return 'border-border bg-card hover:border-brand/30'
}

export default function CustomerKpiCards({ customers }) {
  const now = new Date()
  const thisMonth = now.getMonth()
  const thisYear = now.getFullYear()

  const activeCount = customers.filter((c) => c.status === 'Active').length
  const newThisMonth = customers.filter((c) => {
    const d = new Date(`${c.createdAt}T00:00:00`)
    return d.getMonth() === thisMonth && d.getFullYear() === thisYear
  }).length
  const overdueCount = customers.filter(
    (c) => c.financial && c.financial.overdue > 0,
  ).length

  const cards = [
    {
      label: 'Total Customers',
      value: customers.length,
      helper: 'All registered accounts',
      icon: Users,
      featured: true,
    },
    {
      label: 'Active Customers',
      value: activeCount,
      helper: 'Currently active accounts',
      icon: CheckCircle2,
      tone: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-300',
    },
    {
      label: 'New This Month',
      value: newThisMonth,
      helper: 'Customers added this month',
      icon: CalendarPlus,
      tone: 'bg-sky-500/10 text-sky-600 dark:text-sky-300',
    },
    {
      label: 'Overdue Payments',
      value: overdueCount,
      helper: 'Customers with overdue balance',
      icon: AlertTriangle,
      tone: 'bg-rose-500/10 text-rose-600 dark:text-rose-300',
      tinted: true,
    },
  ]

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map((card) => {
        const Icon = card.icon

        return (
          <article
            key={card.label}
            className={`relative overflow-hidden rounded-2xl border p-5 shadow-sm transition-all duration-200 hover:-translate-y-0.5 ${getSurfaceClassName(
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
            <div className="relative mt-4">
              <p className={`text-sm font-medium ${card.featured ? 'text-white/70' : 'text-muted'}`}>
                {card.label}
              </p>
              <p
                className={`mt-1.5 text-3xl font-bold leading-none tracking-tight ${
                  card.featured ? 'text-white' : 'text-foreground'
                }`}
              >
                {formatNumber(card.value)}
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
