import { useEffect, useRef, useState } from 'react'
import toast from 'react-hot-toast'
import { Activity, AlertCircle, IndianRupee, Wallet } from 'lucide-react'
import DashboardHeader from '../../components/dashboard/DashboardHeader.jsx'
import KpiCard from '../../components/dashboard/KpiCard.jsx'
import OverdueInvoices from '../../components/dashboard/OverdueInvoices.jsx'
import QuickActions from '../../components/dashboard/QuickActions.jsx'
import RecentActivity from '../../components/dashboard/RecentActivity.jsx'
import RecentPaymentsTable from '../../components/dashboard/RecentPaymentsTable.jsx'
import RecentSubscriptionsTable from '../../components/dashboard/RecentSubscriptionsTable.jsx'
import RevenueByCurrency from '../../components/dashboard/RevenueByCurrency.jsx'
import RevenueChart from '../../components/dashboard/RevenueChart.jsx'
import SubscriptionDistributionChart from '../../components/dashboard/SubscriptionDistributionChart.jsx'
import { dashboardDemoData } from '../../data/dashboardData.js'
import { formatINR, formatNumber } from '../../utils/formatters.js'

const kpiVisuals = {
  activeSubscriptions: { icon: Activity, iconTone: 'brand', variant: 'default' },
  monthlyRecurringRevenue: { icon: IndianRupee, iconTone: 'brand', variant: 'featured' },
  outstandingAmount: { icon: Wallet, iconTone: 'warning', variant: 'default' },
  overdueInvoices: { icon: AlertCircle, iconTone: 'danger', variant: 'danger' },
}

export default function DashboardPage() {
  const [selectedRange, setSelectedRange] = useState('Last 30 Days')
  const [isRefreshing, setIsRefreshing] = useState(false)
  const refreshTimerRef = useRef(null)

  useEffect(() => {
    return () => {
      if (refreshTimerRef.current) {
        window.clearTimeout(refreshTimerRef.current)
      }
    }
  }, [])

  function handleRefresh() {
    if (isRefreshing) {
      return
    }

    setIsRefreshing(true)
    refreshTimerRef.current = window.setTimeout(() => {
      setIsRefreshing(false)
      toast.success('Demo refresh complete — no live data was fetched.')
    }, 700)
  }

  return (
    <div className="mx-auto w-full max-w-[1400px] px-4 py-8 sm:px-6 lg:px-8 lg:py-10">
      <DashboardHeader
        selectedRange={selectedRange}
        onRangeChange={setSelectedRange}
        onRefresh={handleRefresh}
        isRefreshing={isRefreshing}
      />

      <div className="mt-8 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {dashboardDemoData.kpis.map((metric) => {
          const visual = kpiVisuals[metric.key] ?? {
            icon: Activity,
            iconTone: 'brand',
            variant: 'default',
          }

          return (
            <KpiCard
              key={metric.key}
              {...metric}
              value={
                metric.format === 'currency' ? formatINR(metric.value) : formatNumber(metric.value)
              }
              icon={visual.icon}
              iconTone={visual.iconTone}
              variant={visual.variant}
            />
          )
        })}
      </div>

      <div className="mt-7 grid gap-6 xl:grid-cols-3">
        <RevenueChart className="xl:col-span-2" />
        <SubscriptionDistributionChart />
      </div>

      <QuickActions className="mt-7" />

      <div className="mt-7 grid gap-6 xl:grid-cols-3">
        <RecentActivity />
        <RecentSubscriptionsTable className="xl:col-span-2" />
      </div>

      <div className="mt-7 grid gap-6 xl:grid-cols-3">
        <RecentPaymentsTable className="xl:col-span-2" />
        <div className="flex flex-col gap-6">
          <OverdueInvoices />
          <RevenueByCurrency />
        </div>
      </div>

      <p className="mt-8 text-center text-xs text-muted">
        All figures on this dashboard are demonstration data and are not live business data.
      </p>
    </div>
  )
}
