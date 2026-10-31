import { dashboardDemoData } from '../../data/dashboardData.js'
import { formatDate, formatINR } from '../../utils/formatters.js'
import DataTable from '../common/DataTable.jsx'
import SectionCard from '../common/SectionCard.jsx'
import StatusBadge from '../common/StatusBadge.jsx'

const columns = [
  {
    key: 'id',
    header: 'Payment ID',
    render: (row) => (
      <span className="font-mono text-xs font-medium text-muted">{row.id}</span>
    ),
  },
  {
    key: 'customer',
    header: 'Customer',
    render: (row) => <span className="font-medium text-foreground">{row.customer}</span>,
  },
  {
    key: 'amount',
    header: 'Amount',
    align: 'right',
    render: (row) => (
      <span className="font-semibold text-foreground">{formatINR(row.amount)}</span>
    ),
  },
  {
    key: 'method',
    header: 'Payment Method',
    render: (row) => <span className="text-muted">{row.method}</span>,
  },
  {
    key: 'date',
    header: 'Date',
    render: (row) => <span className="text-muted">{formatDate(row.date)}</span>,
  },
  {
    key: 'status',
    header: 'Status',
    render: (row) => <StatusBadge status={row.status} />,
  },
]

export default function RecentPaymentsTable({ className = '' }) {
  const payments = dashboardDemoData.recentPayments

  return (
    <SectionCard
      title="Recent Payments"
      description="Most recent payment attempts and settlements."
      actions={<span className="text-[13px] font-medium text-muted">{payments.length} recent records</span>}
      className={className}
    >
      <DataTable
        columns={columns}
        rows={payments}
        getRowKey={(row) => row.id}
        emptyMessage="No recent payments yet."
        minWidthClassName="min-w-[720px]"
        ariaLabel="Recent payments"
      />
    </SectionCard>
  )
}
