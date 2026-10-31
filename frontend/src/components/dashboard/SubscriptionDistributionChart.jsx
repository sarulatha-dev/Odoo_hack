import { useMemo } from 'react'
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { dashboardDemoData } from '../../data/dashboardData.js'
import { useTheme } from '../../hooks/useTheme.js'
import { formatNumber } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import SectionCard from '../common/SectionCard.jsx'

function DistributionTooltip({ active, payload, palette }) {
  if (!active || !payload?.length) {
    return null
  }

  const item = payload[0].payload

  return (
    <div className="rounded-xl border border-border bg-card px-3.5 py-2.5 shadow-xl">
      <div className="flex items-center gap-2 text-sm font-semibold text-foreground">
        <span
          className="h-2 w-2 rounded-full"
          style={{ backgroundColor: palette.colors[item.name] }}
        />
        {item.name} plan
      </div>
      <p className="mt-1 text-xs text-muted">
        {formatNumber(item.value)} subscriptions · {item.percentage}%
      </p>
    </div>
  )
}

export default function SubscriptionDistributionChart({ className = '' }) {
  const { theme } = useTheme()

  const palette = useMemo(
    () => ({
      colors:
        theme === 'dark'
          ? { Low: '#5eead4', Medium: '#2dd4bf', High: '#0d9488' }
          : { Low: '#5eead4', Medium: '#0d9488', High: '#115e59' },
      surface: theme === 'dark' ? '#111c33' : '#ffffff',
    }),
    [theme],
  )

  const distribution = dashboardDemoData.subscriptionDistribution
  const total = distribution.reduce((sum, item) => sum + item.value, 0)
  const distributionLabel = distribution
    .map((item) => `${item.name} plan ${item.percentage}%`)
    .join(', ')

  function renderPercentageLabel({
    cx,
    cy,
    midAngle,
    innerRadius,
    outerRadius,
    percent,
    name,
  }) {
    const radius = innerRadius + (outerRadius - innerRadius) * 0.55
    const x = cx + radius * Math.cos(-midAngle * (Math.PI / 180))
    const y = cy + radius * Math.sin(-midAngle * (Math.PI / 180))
    const useDarkText = theme === 'dark' || name !== 'High'

    return (
      <text
        x={x}
        y={y}
        fill={useDarkText ? '#0f172a' : '#ffffff'}
        textAnchor="middle"
        dominantBaseline="central"
        fontSize={12}
        fontWeight={700}
      >
        {`${Math.round(percent * 100)}%`}
      </text>
    )
  }

  return (
    <SectionCard
      title="Subscription Distribution"
      description="Active subscriptions grouped by plan tier."
      actions={<Badge tone="brand">{formatNumber(total)} active</Badge>}
      className={className}
    >
      <div
        className="relative h-60 w-full"
        role="img"
        aria-label={`Subscription distribution by plan tier: ${distributionLabel}. Total ${formatNumber(
          total,
        )} active subscriptions.`}
      >
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={distribution}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={92}
              paddingAngle={3}
              stroke={palette.surface}
              strokeWidth={2}
              labelLine={false}
              label={renderPercentageLabel}
            >
              {distribution.map((item) => (
                <Cell key={item.name} fill={palette.colors[item.name]} />
              ))}
            </Pie>
            <Tooltip content={<DistributionTooltip palette={palette} />} />
          </PieChart>
        </ResponsiveContainer>
        <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-3xl font-bold tracking-tight text-foreground">
            {formatNumber(total)}
          </span>
          <span className="mt-0.5 text-[11px] text-muted">Active subscriptions</span>
        </div>
      </div>

      <div className="mt-5 grid gap-2.5">
        {distribution.map((item) => (
          <div
            key={item.name}
            className="flex items-center justify-between gap-3 rounded-lg bg-background/60 px-3.5 py-2.5"
          >
            <span className="flex items-center gap-2.5 text-sm font-medium text-foreground">
              <span
                className="h-2.5 w-2.5 rounded-full"
                style={{ backgroundColor: palette.colors[item.name] }}
              />
              {item.name} plan
            </span>
            <span className="text-xs text-muted">
              <span className="text-sm font-semibold text-foreground">
                {formatNumber(item.value)}
              </span>{' '}
              · {item.percentage}%
            </span>
          </div>
        ))}
      </div>
    </SectionCard>
  )
}
