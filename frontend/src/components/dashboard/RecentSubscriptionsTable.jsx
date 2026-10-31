import { dashboardDemoData } from '../../data/dashboardData.js'
import { formatDate } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import DataTable from '../common/DataTable.jsx'
import SectionCard from '../common/SectionCard.jsx'
import StatusBadge from '../common/StatusBadge.jsx'

const planTones = {
  Low: 'neutral',
  Medium: 'info',
  High: 'brand',
}

function getInitials(name) {
  return name
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()
}

const columns = [
  {
    key: 'customer',
    header: 'Customer Name',
    render: (row) => (
      <div className="flex items-center gap-3">
        <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand/10 text-[11px] font-semibold text-brand">
          {getInitials(row.customer)}
        </span>
        <span className="font-medium text-foreground">{row.customer}</span>
      </div>
    ),
  },
  {
    key: 'plan',
    header: 'Plan',
    render: (row) => <Badge tone={planTones[row.plan] ?? 'neutral'}>{row.plan}</Badge>,
  },
  {
    key: 'billingCycle',
    header: 'Billing Cycle',
    render: (row) => <span className="text-muted">{row.billingCycle}</span>,
  },
  {
    key: 'startDate',
    header: 'Start Date',
    render: (row) => <span className="text-muted">{formatDate(row.startDate)}</span>,
  },
  {
    key: 'status',
    header: 'Status',
    render: (row) => <StatusBadge status={row.status} />,
  },
]

export default function RecentSubscriptionsTable({ className = '' }) {
  const subscriptions = dashboardDemoData.recentSubscriptions

  return (
    <SectionCard
      title="Recent Subscriptions"
      description="Latest subscription activity across plan tiers."
      actions={
        <span className="text-[13px] font-medium text-muted">{subscriptions.length} recent records</span>
      }
      className={className}
    >
      <DataTable
        columns={columns}
        rows={subscriptions}
        getRowKey={(row) => row.id}
        emptyMessage="No recent subscriptions yet."
        ariaLabel="Recent subscriptions"
      />
    </SectionCard>
  )
}
