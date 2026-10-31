import { useState } from 'react'
import toast from 'react-hot-toast'
import { Info, Layers, Plus } from 'lucide-react'
import Badge from '../../components/common/Badge.jsx'
import PlanCatalogue from '../../components/plans/PlanCatalogue.jsx'
import PlanDeleteDialog from '../../components/plans/PlanDeleteDialog.jsx'
import PlanDetailsModal from '../../components/plans/PlanDetailsModal.jsx'
import PlanFilters from '../../components/plans/PlanFilters.jsx'
import PlanFormModal from '../../components/plans/PlanFormModal.jsx'
import PlanKpiCards from '../../components/plans/PlanKpiCards.jsx'
import {
  planBillingCycles,
  plansDemoData,
  planTiers,
} from '../../data/plansData.js'

function getInitialPlans() {
  return plansDemoData.map((plan) => ({
    ...plan,
    features: [...plan.features],
  }))
}

function getToday() {
  const today = new Date()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${today.getFullYear()}-${month}-${day}`
}

export default function PlansPage() {
  const [plans, setPlans] = useState(getInitialPlans)
  const [search, setSearch] = useState('')
  const [tier, setTier] = useState('all')
  const [billingCycle, setBillingCycle] = useState('all')
  const [status, setStatus] = useState('all')
  const [formPlanId, setFormPlanId] = useState(null)
  const [viewingPlanId, setViewingPlanId] = useState(null)
  const [deletingPlanId, setDeletingPlanId] = useState(null)

  const normalizedSearch = search.trim().toLowerCase()
  const hasActiveFilters =
    Boolean(normalizedSearch) ||
    tier !== 'all' ||
    billingCycle !== 'all' ||
    status !== 'all'

  const filteredPlans = plans.filter((plan) => {
    const matchesSearch =
      !normalizedSearch ||
      plan.name.toLowerCase().includes(normalizedSearch) ||
      plan.code.toLowerCase().includes(normalizedSearch)
    const matchesTier = tier === 'all' || plan.tier === tier
    const matchesCycle =
      billingCycle === 'all' || plan.billingCycle === billingCycle
    const matchesStatus = status === 'all' || plan.status === status

    return matchesSearch && matchesTier && matchesCycle && matchesStatus
  })

  const editingPlan = plans.find((plan) => plan.id === formPlanId) ?? null
  const viewingPlan = plans.find((plan) => plan.id === viewingPlanId) ?? null
  const deletingPlan = plans.find((plan) => plan.id === deletingPlanId) ?? null

  function handleClearFilters() {
    setSearch('')
    setTier('all')
    setBillingCycle('all')
    setStatus('all')
  }

  function handleFormSubmit(values) {
    if (editingPlan) {
      setPlans((current) =>
        current.map((plan) =>
          plan.id === editingPlan.id
            ? { ...plan, ...values, updatedAt: getToday() }
            : plan,
        ),
      )
      toast.success('Plan updated successfully.')
    } else {
      const newPlan = {
        id: `plan-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
        ...values,
        popular: false,
        createdAt: getToday(),
        updatedAt: getToday(),
      }
      setPlans((current) => [newPlan, ...current])
      handleClearFilters()
      toast.success('Plan created successfully.')
    }

    setFormPlanId(null)
  }

  function handleDeleteConfirm() {
    if (!deletingPlan) {
      return
    }

    const deletedName = deletingPlan.name
    setPlans((current) => current.filter((plan) => plan.id !== deletingPlan.id))
    setDeletingPlanId(null)
    toast.success(`${deletedName} deleted successfully.`)
  }

  return (
    <div className="mx-auto w-full max-w-[1400px] px-4 py-8 sm:px-6 lg:px-8 lg:py-10">
      <div className="relative overflow-hidden rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-6 lg:p-7">
        <div
          className="pointer-events-none absolute -right-16 -top-24 h-64 w-64 rounded-full bg-brand/10 blur-3xl"
          aria-hidden="true"
        />
        <div
          className="pointer-events-none absolute -bottom-28 left-1/3 h-48 w-48 rounded-full bg-brand/5 blur-3xl"
          aria-hidden="true"
        />

        <div className="relative flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="flex flex-wrap items-center gap-2.5">
              <span className="inline-flex items-center gap-2 rounded-full border border-brand/20 bg-brand/10 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.16em] text-brand">
                <Layers className="h-3.5 w-3.5" aria-hidden="true" />
                Plan Management
              </span>
              <Badge tone="neutral">Demo data</Badge>
            </div>

            <h1 className="mt-4 text-2xl font-bold tracking-tight text-foreground sm:text-3xl">
              Subscription Plans
            </h1>
            <p className="mt-1.5 max-w-2xl text-sm text-muted sm:text-base">
              Create and manage flexible subscription plans.
            </p>
          </div>

          <button
            type="button"
            onClick={() => setFormPlanId('new')}
            data-modal-fallback-focus
            className="inline-flex h-10 shrink-0 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white shadow-sm shadow-brand/20 transition-colors hover:bg-brand-dark"
          >
            <Plus className="h-4 w-4" aria-hidden="true" />
            Create Plan
          </button>
        </div>
      </div>

      <div className="mt-6 flex items-start gap-2.5 rounded-xl border border-brand/15 bg-brand/5 px-4 py-3 text-sm text-muted">
        <Info className="mt-0.5 h-4 w-4 shrink-0 text-brand" aria-hidden="true" />
        <p>
          This module uses local demonstration data. Create, edit, and delete actions update this
          page only — no payments are processed and changes reset when you refresh the browser.
        </p>
      </div>

      <div className="mt-6">
        <PlanKpiCards plans={plans} />
      </div>

      <div className="mt-6">
        <PlanFilters
          search={search}
          onSearchChange={setSearch}
          tier={tier}
          onTierChange={setTier}
          billingCycle={billingCycle}
          onBillingCycleChange={setBillingCycle}
          status={status}
          onStatusChange={setStatus}
          tiers={planTiers}
          billingCycles={planBillingCycles}
          resultCount={filteredPlans.length}
          totalCount={plans.length}
          onClear={handleClearFilters}
          hasActiveFilters={hasActiveFilters}
        />
      </div>

      <div className="mt-6">
        <PlanCatalogue
          plans={filteredPlans}
          onView={(plan) => setViewingPlanId(plan.id)}
          onEdit={(plan) => setFormPlanId(plan.id)}
          onDelete={(plan) => setDeletingPlanId(plan.id)}
          onClearFilters={handleClearFilters}
          onAdd={() => setFormPlanId('new')}
          hasActiveFilters={hasActiveFilters}
        />
      </div>

      <p className="mt-8 text-center text-xs text-muted">
        Plan records on this page are demonstration data and are not connected to a backend,
        payment processor, or billing service.
      </p>

      <PlanFormModal
        open={formPlanId !== null}
        plan={editingPlan}
        plans={plans}
        onSave={handleFormSubmit}
        onClose={() => setFormPlanId(null)}
      />

      <PlanDetailsModal
        open={Boolean(viewingPlan)}
        plan={viewingPlan}
        onClose={() => setViewingPlanId(null)}
        onEdit={() => {
          setFormPlanId(viewingPlan.id)
          setViewingPlanId(null)
        }}
      />

      <PlanDeleteDialog
        open={Boolean(deletingPlan)}
        plan={deletingPlan}
        onClose={() => setDeletingPlanId(null)}
        onConfirm={handleDeleteConfirm}
      />
    </div>
  )
}
