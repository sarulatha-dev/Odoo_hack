import { CheckCircle2, FileText, RefreshCw, UserPlus, XCircle } from 'lucide-react'
import { dashboardDemoData } from '../../data/dashboardData.js'
import Badge from '../common/Badge.jsx'
import SectionCard from '../common/SectionCard.jsx'

const activityStyles = {
  subscription: {
    icon: UserPlus,
    tone: 'bg-brand/10 text-brand',
  },
  payment: {
    icon: CheckCircle2,
    tone: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-300',
  },
  invoice: {
    icon: FileText,
    tone: 'bg-sky-500/10 text-sky-600 dark:text-sky-300',
  },
  renewal: {
    icon: RefreshCw,
    tone: 'bg-indigo-500/10 text-indigo-600 dark:text-indigo-300',
  },
  failure: {
    icon: XCircle,
    tone: 'bg-rose-500/10 text-rose-600 dark:text-rose-300',
  },
}

export default function RecentActivity({ className = '' }) {
  const activities = dashboardDemoData.recentActivity

  return (
    <SectionCard
      title="Recent Activity"
      description="Latest subscription and billing events."
      actions={<Badge tone="neutral">Demo</Badge>}
      className={className}
      contentClassName="pt-4"
    >
      <ol>
        {activities.map((activity, index) => {
          const style = activityStyles[activity.type] ?? activityStyles.subscription
          const Icon = style.icon
          const isLast = index === activities.length - 1

          return (
            <li key={activity.id} className="flex gap-3">
              <div className="flex flex-col items-center">
                <span
                  className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${style.tone}`}
                >
                  <Icon className="h-4 w-4" aria-hidden="true" />
                </span>
                {!isLast && <span className="my-1 w-px flex-1 bg-border" aria-hidden="true" />}
              </div>
              <div className={`min-w-0 flex-1 ${isLast ? 'pb-0' : 'pb-5'}`}>
                <div className="flex items-start justify-between gap-3">
                  <p className="text-sm font-semibold text-foreground">{activity.title}</p>
                  <span className="shrink-0 text-[11px] text-muted">{activity.time}</span>
                </div>
                <p className="mt-1 text-[13px] leading-5 text-muted">{activity.description}</p>
              </div>
            </li>
          )
        })}
      </ol>
    </SectionCard>
  )
}
