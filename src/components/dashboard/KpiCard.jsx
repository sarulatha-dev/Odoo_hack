import { ArrowDownRight, ArrowUpRight } from 'lucide-react'

const iconToneStyles = {
  brand: 'bg-brand/10 text-brand',
  info: 'bg-sky-500/10 text-sky-600 dark:text-sky-300',
  warning: 'bg-amber-500/10 text-amber-600 dark:text-amber-300',
  danger: 'bg-rose-500/10 text-rose-600 dark:text-rose-300',
}

const changeToneStyles = {
  positive: 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-300',
  negative: 'bg-rose-500/10 text-rose-700 dark:text-rose-300',
  neutral: 'bg-slate-500/10 text-slate-600 dark:text-slate-300',
}

const featuredChangeToneStyles = {
  positive: 'bg-white/10 text-emerald-200',
  negative: 'bg-white/10 text-rose-200',
  neutral: 'bg-white/10 text-white/80',
}

const cardVariantStyles = {
  default: 'border-border bg-card shadow-sm hover:border-brand/30',
  featured:
    'border-brand-dark/30 bg-linear-to-br from-brand-dark via-[#124e4a] to-navy shadow-lg shadow-brand-dark/20 hover:shadow-xl hover:shadow-brand-dark/25 dark:border-brand/30',
  danger:
    'border-rose-500/20 bg-rose-500/[0.035] shadow-sm hover:border-rose-500/35 dark:bg-rose-500/[0.06]',
}

export default function KpiCard({
  title,
  value,
  change,
  trend = 'up',
  changeTone = 'neutral',
  icon: Icon,
  iconTone = 'brand',
  variant = 'default',
}) {
  const TrendIcon = trend === 'down' ? ArrowDownRight : ArrowUpRight
  const isFeatured = variant === 'featured'
  const iconClasses = iconToneStyles[iconTone] ?? iconToneStyles.brand
  const changeClasses = isFeatured
    ? (featuredChangeToneStyles[changeTone] ?? featuredChangeToneStyles.neutral)
    : (changeToneStyles[changeTone] ?? changeToneStyles.neutral)
  const cardClasses = cardVariantStyles[variant] ?? cardVariantStyles.default

  return (
    <article
      className={`group relative flex h-full flex-col overflow-hidden rounded-2xl border p-6 transition-all duration-200 hover:-translate-y-0.5 ${cardClasses}`}
    >
      {isFeatured && (
        <span
          className="pointer-events-none absolute -right-8 -top-10 h-32 w-32 rounded-full bg-white/5 blur-2xl"
          aria-hidden="true"
        />
      )}

      <div className="relative flex items-start justify-between gap-4">
        <span
          className={`flex h-12 w-12 items-center justify-center rounded-xl transition-transform duration-200 group-hover:scale-105 ${
            isFeatured ? 'bg-white/10 text-white ring-1 ring-inset ring-white/15' : iconClasses
          }`}
        >
          <Icon className="h-6 w-6" aria-hidden="true" />
        </span>
        <span
          className={`inline-flex items-center gap-1 rounded-full px-3 py-1.5 text-xs font-semibold ${changeClasses}`}
        >
          <TrendIcon className="h-3.5 w-3.5" aria-hidden="true" />
          {change}
        </span>
      </div>

      <div className="relative mt-5">
        <p className={`text-sm font-medium ${isFeatured ? 'text-white/70' : 'text-muted'}`}>
          {title}
        </p>
        <p
          className={`mt-2 text-3xl font-bold leading-none tracking-tight ${
            isFeatured ? 'text-white' : 'text-foreground'
          }`}
        >
          {value}
        </p>
      </div>

      <p className={`relative mt-auto pt-4 text-xs ${isFeatured ? 'text-white/70' : 'text-muted'}`}>
        vs previous period
      </p>
    </article>
  )
}
