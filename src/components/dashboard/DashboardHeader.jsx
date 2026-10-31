import {
  CalendarDays,
  CalendarRange,
  ChevronDown,
  LayoutDashboard,
  Plus,
  RefreshCw,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { dateRangeOptions } from '../../data/dashboardData.js'
import { ROUTES } from '../../routes/paths.js'
import { formatLongDate } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'

function getGreeting() {
  const hour = new Date().getHours()

  if (hour < 12) {
    return 'Good morning'
  }

  if (hour < 17) {
    return 'Good afternoon'
  }

  return 'Good evening'
}

export default function DashboardHeader({
  selectedRange,
  onRangeChange,
  onRefresh,
  isRefreshing,
}) {
  return (
    <div className="relative overflow-hidden rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-6 lg:p-7">
      <div
        className="pointer-events-none absolute -right-16 -top-24 h-64 w-64 rounded-full bg-brand/10 blur-3xl"
        aria-hidden="true"
      />

      <div className="relative flex flex-col gap-6 xl:flex-row xl:items-center xl:justify-between">
        <div>
          <div className="flex flex-wrap items-center gap-2.5">
            <span className="inline-flex items-center gap-2 rounded-full border border-brand/20 bg-brand/10 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.16em] text-brand">
              <LayoutDashboard className="h-3.5 w-3.5" aria-hidden="true" />
              Dashboard
            </span>
            <Badge tone="neutral">Demo data</Badge>
          </div>

          <h1 className="mt-4 text-2xl font-bold tracking-tight text-foreground sm:text-3xl">
            {getGreeting()}, Admin!
          </h1>
          <p className="mt-1.5 text-sm text-muted sm:text-base">
            Welcome to your subscription overview.
          </p>
          <p className="mt-3 inline-flex items-center gap-2 text-sm text-muted">
            <CalendarDays className="h-4 w-4 text-brand" aria-hidden="true" />
            {formatLongDate()}
          </p>
        </div>

        <div className="flex flex-col gap-2.5 sm:flex-row sm:flex-wrap sm:items-center">
          <div className="relative">
            <label htmlFor="dashboard-date-range" className="sr-only">
              Date range
            </label>
            <p id="dashboard-range-note" className="sr-only">
              Date range selection is illustrative and does not filter the demo data.
            </p>
            <CalendarRange
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted"
              aria-hidden="true"
            />
            <select
              id="dashboard-date-range"
              value={selectedRange}
              onChange={(event) => onRangeChange(event.target.value)}
              aria-describedby="dashboard-range-note"
              title="Demo range selector; chart data is illustrative"
              className="h-10 w-full appearance-none rounded-lg border border-border bg-card py-2 pl-9 pr-10 text-sm font-medium text-foreground shadow-sm transition-colors hover:border-brand/40 focus:border-brand focus:outline-none sm:w-48"
            >
              {dateRangeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
            <ChevronDown
              className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted"
              aria-hidden="true"
            />
          </div>

          <button
            type="button"
            onClick={onRefresh}
            disabled={isRefreshing}
            aria-label="Simulate dashboard refresh using demo data"
            title="Simulates a refresh using demo data"
            className="inline-flex h-10 items-center justify-center gap-2 rounded-lg border border-border bg-card px-4 text-sm font-semibold text-foreground shadow-sm transition-colors hover:border-brand/40 hover:text-brand disabled:cursor-not-allowed disabled:opacity-70"
          >
            <RefreshCw
              className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`}
              aria-hidden="true"
            />
            {isRefreshing ? 'Refreshing...' : 'Refresh'}
          </button>

          <Link
            to={ROUTES.admin.subscriptions}
            className="inline-flex h-10 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white shadow-sm shadow-brand/20 transition-colors hover:bg-brand-dark"
          >
            <Plus className="h-4 w-4" aria-hidden="true" />
            Create subscription
          </Link>
        </div>
      </div>
    </div>
  )
}
