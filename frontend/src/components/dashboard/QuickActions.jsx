import { ChevronRight, FileText, Receipt, Repeat, UserPlus } from 'lucide-react'
import { Link } from 'react-router-dom'
import { ROUTES } from '../../routes/paths.js'
import SectionCard from '../common/SectionCard.jsx'

const quickActions = [
  {
    label: 'Create Subscription',
    description: 'Start a recurring plan',
    to: ROUTES.admin.subscriptions,
    icon: Repeat,
    tone: 'bg-brand/10 text-brand',
  },
  {
    label: 'Add Customer',
    description: 'Create a billing profile',
    to: ROUTES.admin.customers,
    icon: UserPlus,
    tone: 'bg-sky-500/10 text-sky-600 dark:text-sky-300',
  },
  {
    label: 'Create Quotation',
    description: 'Draft a new proposal',
    to: ROUTES.admin.quotations,
    icon: FileText,
    tone: 'bg-indigo-500/10 text-indigo-600 dark:text-indigo-300',
  },
  {
    label: 'Generate Invoice',
    description: 'Issue a billing document',
    to: ROUTES.admin.invoices,
    icon: Receipt,
    tone: 'bg-amber-500/10 text-amber-600 dark:text-amber-300',
  },
]

export default function QuickActions({ className = '' }) {
  return (
    <SectionCard
      title="Quick Actions"
      description="Jump into the workflows your team uses most."
      className={className}
    >
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {quickActions.map((action) => {
          const Icon = action.icon

          return (
            <Link
              key={action.label}
              to={action.to}
              className="group flex items-center gap-3 rounded-xl border border-transparent bg-background/70 p-3.5 transition-colors hover:border-brand/20 hover:bg-brand/5"
            >
              <span
                className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg ${action.tone}`}
              >
                <Icon className="h-5 w-5" aria-hidden="true" />
              </span>
              <span className="min-w-0 flex-1">
                <span className="block truncate text-sm font-semibold text-foreground">
                  {action.label}
                </span>
                <span className="mt-0.5 block truncate text-xs text-muted">
                  {action.description}
                </span>
              </span>
              <ChevronRight
                className="h-4 w-4 shrink-0 text-muted transition-transform group-hover:translate-x-0.5 group-hover:text-brand"
                aria-hidden="true"
              />
            </Link>
          )
        })}
      </div>
    </SectionCard>
  )
}
