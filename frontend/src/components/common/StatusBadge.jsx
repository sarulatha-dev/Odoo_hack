import Badge from './Badge.jsx'

const statusStyles = {
  active: { label: 'Active', tone: 'success' },
  pending: { label: 'Pending', tone: 'warning' },
  paused: { label: 'Paused', tone: 'info' },
  cancelled: { label: 'Cancelled', tone: 'neutral' },
  success: { label: 'Success', tone: 'success' },
  failed: { label: 'Failed', tone: 'danger' },
  overdue: { label: 'Overdue', tone: 'danger' },
  inactive: { label: 'Inactive', tone: 'neutral' },
}

export default function StatusBadge({ status }) {
  const config = statusStyles[String(status).toLowerCase()] ?? {
    label: status,
    tone: 'neutral',
  }

  return (
    <Badge tone={config.tone} dot>
      {config.label}
    </Badge>
  )
}
