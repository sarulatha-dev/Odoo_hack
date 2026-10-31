import { dashboardDemoData } from '../../data/dashboardData.js'
import { formatDate, formatINR } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import SectionCard from '../common/SectionCard.jsx'

export default function OverdueInvoices({ className = '' }) {
  const invoices = dashboardDemoData.overdueInvoices

  return (
    <SectionCard
      title="Overdue Invoices"
      description="Invoices past their payment due date."
      actions={<Badge tone="danger">{invoices.length} overdue</Badge>}
      className={className}
      contentClassName="pt-3"
    >
      <ul className="divide-y divide-border">
        {invoices.map((invoice) => (
          <li key={invoice.id} className="py-3 first:pt-0 last:pb-0">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="truncate text-sm font-semibold text-foreground">{invoice.customer}</p>
                <p className="mt-0.5 text-xs text-muted">
                  {invoice.invoiceNumber} · Due {formatDate(invoice.dueDate)}
                </p>
              </div>
              <p className="shrink-0 text-sm font-bold text-rose-600 dark:text-rose-400">
                {formatINR(invoice.amount)}
              </p>
            </div>
            <div className="mt-2 flex items-center justify-between gap-3">
              <span className="text-xs text-muted">Days overdue</span>
              <Badge tone="danger">{invoice.daysOverdue} days</Badge>
            </div>
          </li>
        ))}
      </ul>
    </SectionCard>
  )
}
