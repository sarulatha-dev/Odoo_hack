import { useMemo } from 'react'
import {
  Bar,
  CartesianGrid,
  ComposedChart,
  Legend,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { dashboardDemoData } from '../../data/dashboardData.js'
import { useTheme } from '../../hooks/useTheme.js'
import { formatINR, formatINRCompact, formatNumber } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import SectionCard from '../common/SectionCard.jsx'

function RevenueTooltip({ active, payload, label, palette }) {
  if (!active || !payload?.length) {
    return null
  }

  return (
    <div className="min-w-[210px] rounded-xl border border-border bg-card px-4 py-3.5 shadow-xl">
      <p className="text-xs font-semibold uppercase tracking-wider text-muted">{label}</p>
      <div className="mt-2.5 space-y-2">
        {payload.map((entry) => (
          <div key={entry.dataKey} className="flex items-center justify-between gap-5 text-sm">
            <span className="flex items-center gap-2 text-muted">
              <span
                className="h-2 w-2 rounded-full"
                style={{
                  backgroundColor:
                    entry.dataKey === 'revenue' ? palette.revenue : palette.subscriptions,
                }}
              />
              {entry.name}
            </span>
            <span className="font-semibold text-foreground">
              {entry.dataKey === 'revenue'
                ? formatINR(entry.value)
                : formatNumber(entry.value)}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

export default function RevenueChart({ className = '' }) {
  const { theme } = useTheme()

  const palette = useMemo(
    () => ({
      revenue: theme === 'dark' ? '#2dd4bf' : '#0d9488',
      subscriptions: theme === 'dark' ? '#93c5fd' : '#115e59',
      grid: theme === 'dark' ? '#1e293b' : '#e2e8f0',
      axis: theme === 'dark' ? '#cbd5e1' : '#475569',
      cursor: theme === 'dark' ? 'rgba(148, 163, 184, 0.08)' : 'rgba(15, 23, 42, 0.04)',
    }),
    [theme],
  )

  const summary = useMemo(() => {
    const rows = dashboardDemoData.revenueOverview
    const totalRevenue = rows.reduce((sum, row) => sum + row.revenue, 0)
    const totalSubscriptions = rows.reduce((sum, row) => sum + row.subscriptions, 0)

    return {
      totalRevenue,
      averageRevenue: Math.round(totalRevenue / rows.length),
      totalSubscriptions,
    }
  }, [])

  return (
    <SectionCard
      title="Revenue Overview"
      description="Monthly revenue and new subscription growth across the year."
      actions={<Badge tone="neutral">12 months</Badge>}
      className={className}
    >
      <div className="mb-5 grid gap-4 border-b border-border/70 pb-5 sm:grid-cols-3">
        <div>
          <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-muted">
            Annual revenue
          </p>
          <p className="mt-1.5 text-lg font-semibold tracking-tight text-foreground">
            {formatINR(summary.totalRevenue)}
          </p>
        </div>
        <div className="sm:border-l sm:border-border/70 sm:pl-5">
          <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-muted">
            Avg. monthly
          </p>
          <p className="mt-1.5 text-lg font-semibold tracking-tight text-foreground">
            {formatINR(summary.averageRevenue)}
          </p>
        </div>
        <div className="sm:border-l sm:border-border/70 sm:pl-5">
          <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-muted">
            New subscriptions
          </p>
          <p className="mt-1.5 text-lg font-semibold tracking-tight text-foreground">
            {formatNumber(summary.totalSubscriptions)}
          </p>
        </div>
      </div>

      <div
        className="h-[300px] w-full sm:h-[320px] xl:h-[350px]"
        role="img"
        aria-label={`Revenue overview chart. Annual revenue ${formatINR(
          summary.totalRevenue,
        )}, average monthly revenue ${formatINR(
          summary.averageRevenue,
        )}, and ${formatNumber(summary.totalSubscriptions)} new subscriptions across 12 months.`}
      >
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart
            data={dashboardDemoData.revenueOverview}
            margin={{ top: 16, right: 8, left: 0, bottom: 0 }}
          >
            <defs>
              <linearGradient id="revenueGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={palette.revenue} stopOpacity={0.95} />
                <stop offset="95%" stopColor={palette.revenue} stopOpacity={0.35} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="4 4" vertical={false} stroke={palette.grid} />
            <XAxis
              dataKey="month"
              tickLine={false}
              axisLine={false}
              tick={{ fill: palette.axis, fontSize: 12, fontWeight: 500 }}
              tickMargin={10}
              minTickGap={8}
            />
            <YAxis
              yAxisId="revenue"
              tickLine={false}
              axisLine={false}
              tick={{ fill: palette.axis, fontSize: 12, fontWeight: 500 }}
              tickFormatter={formatINRCompact}
              width={68}
            />
            <YAxis
              yAxisId="subscriptions"
              orientation="right"
              tickLine={false}
              axisLine={false}
              tick={{ fill: palette.axis, fontSize: 12, fontWeight: 500 }}
              width={40}
              allowDecimals={false}
            />
            <Tooltip
              content={<RevenueTooltip palette={palette} />}
              cursor={{ fill: palette.cursor }}
            />
            <Legend
              verticalAlign="top"
              align="right"
              iconType="circle"
              iconSize={10}
              wrapperStyle={{ paddingBottom: 20, fontSize: 13 }}
              formatter={(value) => <span className="font-medium text-muted">{value}</span>}
            />
            <Bar
              yAxisId="revenue"
              dataKey="revenue"
              name="Revenue"
              fill="url(#revenueGradient)"
              radius={[6, 6, 0, 0]}
              maxBarSize={20}
            />
            <Line
              yAxisId="subscriptions"
              type="monotone"
              dataKey="subscriptions"
              name="New subscriptions"
              stroke={palette.subscriptions}
              strokeWidth={2.5}
              dot={false}
              activeDot={{ r: 4, strokeWidth: 0 }}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>
    </SectionCard>
  )
}
