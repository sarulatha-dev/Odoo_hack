import { dashboardDemoData } from '../../data/dashboardData.js'
import { formatCurrency } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import SectionCard from '../common/SectionCard.jsx'

const currencyBarStyles = {
  INR: 'bg-brand',
  USD: 'bg-sky-500',
  SGD: 'bg-indigo-500',
  EUR: 'bg-amber-500',
}

export default function RevenueByCurrency({ className = '' }) {
  const currencies = dashboardDemoData.revenueByCurrency

  return (
    <SectionCard
      title="Revenue by Currency"
      description="Illustrative revenue split across billing currencies."
      actions={<Badge tone="neutral">4 currencies</Badge>}
      className={className}
      contentClassName="pt-3"
    >
      <ul className="space-y-4">
        {currencies.map((item) => (
          <li key={item.code}>
            <div className="flex items-center justify-between gap-3">
              <div className="flex min-w-0 items-center gap-2.5">
                <span className="flex h-8 w-11 shrink-0 items-center justify-center rounded-md border border-border bg-background text-[11px] font-bold tracking-wide text-foreground">
                  {item.code}
                </span>
                <span className="truncate text-sm text-muted">{item.name}</span>
              </div>
              <span className="shrink-0 text-sm font-semibold text-foreground">
                {formatCurrency(item.amount, item.code)}
              </span>
            </div>
            <div className="mt-2 flex items-center gap-3">
              <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-foreground/10">
                <div
                  className={`h-full rounded-full ${currencyBarStyles[item.code] ?? 'bg-brand'}`}
                  style={{ width: `${item.share}%` }}
                />
              </div>
              <span className="w-8 text-right text-xs font-medium text-muted">{item.share}%</span>
            </div>
          </li>
        ))}
      </ul>
    </SectionCard>
  )
}
