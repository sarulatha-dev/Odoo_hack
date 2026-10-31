import { useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Info, Plus, Save, Sparkles, X } from 'lucide-react'
import {
  planBillingCycles,
  planCurrencies,
  planStatuses,
  planTiers,
} from '../../data/plansData.js'
import Modal from '../common/Modal.jsx'

const titleId = 'plan-form-title'

function getDefaultValues(plan) {
  return {
    name: plan?.name ?? '',
    code: plan?.code ?? '',
    description: plan?.description ?? '',
    tier: plan?.tier ?? 'Medium',
    price: plan?.price ?? '',
    currency: plan?.currency ?? 'INR',
    billingCycle: plan?.billingCycle ?? 'Monthly',
    trialDays: plan?.trialDays ?? 14,
    status: plan?.status ?? 'Active',
  }
}

function getFieldClassName(hasError, extraClassName = '') {
  return [
    'w-full rounded-lg border bg-card text-sm text-foreground shadow-sm transition-colors focus:outline-none',
    hasError
      ? 'border-rose-500 focus:border-rose-500'
      : 'border-border hover:border-brand/40 focus:border-brand',
    extraClassName,
  ].join(' ')
}

function FormField({ label, htmlFor, error, hint, className = '', children }) {
  return (
    <div className={className}>
      <label htmlFor={htmlFor} className="mb-1.5 block text-sm font-medium text-foreground">
        {label}
      </label>
      {children}
      {error ? (
        <p id={`${htmlFor}-error`} className="mt-1.5 text-xs font-medium text-rose-600 dark:text-rose-400">
          {error}
        </p>
      ) : (
        hint && (
          <p id={`${htmlFor}-hint`} className="mt-1.5 text-xs text-muted">
            {hint}
          </p>
        )
      )}
    </div>
  )
}

function PlanForm({ plan, plans, onSave, onCancel }) {
  const [features, setFeatures] = useState(() => (plan?.features ? [...plan.features] : []))
  const [featureDraft, setFeatureDraft] = useState('')
  const [featuresError, setFeaturesError] = useState('')
  const featureInputRef = useRef(null)

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({ defaultValues: getDefaultValues(plan) })

  const descriptionValue = watch('description') ?? ''

  function addFeature() {
    const value = featureDraft.trim().replace(/\s+/g, ' ')

    if (!value) {
      setFeaturesError('Enter a feature before adding it.')
      featureInputRef.current?.focus()
      return
    }

    if (value.length > 60) {
      setFeaturesError('Keep each feature to 60 characters or fewer.')
      featureInputRef.current?.focus()
      return
    }

    if (features.length >= 8) {
      setFeaturesError('You can add up to 8 features.')
      return
    }

    if (features.some((feature) => feature.toLowerCase() === value.toLowerCase())) {
      setFeaturesError('That feature is already included.')
      featureInputRef.current?.focus()
      return
    }

    setFeatures((current) => [...current, value])
    setFeatureDraft('')
    setFeaturesError('')
    featureInputRef.current?.focus()
  }

  function removeFeature(featureToRemove) {
    setFeatures((current) => current.filter((feature) => feature !== featureToRemove))
    setFeaturesError('')
    window.requestAnimationFrame(() => featureInputRef.current?.focus())
  }

  function submit(values) {
    if (features.length === 0) {
      setFeaturesError('Add at least one feature before saving the plan.')
      featureInputRef.current?.focus()
      return
    }

    onSave({
      name: values.name.trim(),
      code: values.code.trim().toUpperCase(),
      description: values.description.trim(),
      tier: values.tier,
      price: Number(values.price),
      currency: values.currency,
      billingCycle: values.billingCycle,
      trialDays: Number(values.trialDays),
      features,
      status: values.status,
    })
  }

  return (
    <form
      noValidate
      onSubmit={handleSubmit(submit)}
      className="flex max-h-[calc(100dvh-2rem)] flex-col sm:max-h-[calc(100dvh-3rem)]"
    >
      <div className="shrink-0 border-b border-border px-5 py-5 pr-14 sm:px-6 sm:py-6 sm:pr-16">
        <h2 id={titleId} className="text-lg font-semibold tracking-tight text-foreground">
          {plan ? 'Edit Plan' : 'Create Plan'}
        </h2>
        <p className="mt-1 text-sm text-muted">
          {plan
            ? 'Update the plan details below. Changes stay in this browser session.'
            : 'Define a demonstration plan for the local catalogue.'}
        </p>
      </div>

      <div className="min-h-0 flex-1 overflow-y-auto px-5 py-5 sm:px-6 sm:py-6">
        <div className="mb-5 flex items-start gap-2 rounded-lg border border-brand/15 bg-brand/5 px-3 py-2.5 text-xs text-muted">
          <Info className="mt-0.5 h-3.5 w-3.5 shrink-0 text-brand" aria-hidden="true" />
          <span>
            Demo mode: this plan is stored locally only. No payments, invoices, or live
            subscriptions are created.
          </span>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <FormField
            label="Plan Name"
            htmlFor="plan-name"
            error={errors.name?.message}
            className="sm:col-span-2"
          >
            <input
              id="plan-name"
              type="text"
              data-autofocus
              autoComplete="off"
              placeholder="e.g. Growth"
              aria-invalid={Boolean(errors.name)}
              aria-describedby={errors.name ? 'plan-name-error' : undefined}
              className={getFieldClassName(Boolean(errors.name), 'h-11 px-3')}
              {...register('name', {
                required: 'Plan name is required.',
                validate: (value) =>
                  value.trim().length >= 2 || 'Use at least 2 characters.',
                maxLength: { value: 80, message: 'Use 80 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField label="Plan Code" htmlFor="plan-code" error={errors.code?.message}>
            <input
              id="plan-code"
              type="text"
              autoComplete="off"
              placeholder="e.g. PLN-GRW-MON"
              aria-invalid={Boolean(errors.code)}
              aria-describedby={errors.code ? 'plan-code-error' : undefined}
              className={getFieldClassName(Boolean(errors.code), 'h-11 px-3 font-mono uppercase')}
              {...register('code', {
                required: 'Plan code is required.',
                maxLength: { value: 24, message: 'Use 24 characters or fewer.' },
                pattern: {
                  value: /^[A-Za-z0-9-]+$/,
                  message: 'Use only letters, numbers, and hyphens.',
                },
                validate: (value) => {
                  const normalized = value.trim().toLowerCase()
                  const duplicate = plans.some(
                    (item) => item.id !== plan?.id && item.code.toLowerCase() === normalized,
                  )
                  return duplicate ? 'This plan code is already in use.' : true
                },
              })}
            />
          </FormField>

          <FormField label="Plan Tier" htmlFor="plan-tier" error={errors.tier?.message}>
            <select
              id="plan-tier"
              aria-invalid={Boolean(errors.tier)}
              aria-describedby={errors.tier ? 'plan-tier-error' : undefined}
              className={getFieldClassName(Boolean(errors.tier), 'h-11 px-3')}
              {...register('tier', { required: 'Plan tier is required.' })}
            >
              {planTiers.map((tier) => (
                <option key={tier} value={tier}>
                  {tier}
                </option>
              ))}
            </select>
          </FormField>

          <FormField
            label="Description"
            htmlFor="plan-description"
            error={errors.description?.message}
            hint={`${descriptionValue.length}/240 characters`}
            className="sm:col-span-2"
          >
            <textarea
              id="plan-description"
              rows={3}
              maxLength={240}
              placeholder="Describe who this plan is for and what it includes"
              aria-invalid={Boolean(errors.description)}
              aria-describedby={
                errors.description ? 'plan-description-error' : 'plan-description-hint'
              }
              className={getFieldClassName(Boolean(errors.description), 'resize-y px-3 py-2.5')}
              {...register('description', {
                required: 'Description is required.',
                validate: (value) =>
                  value.trim().length >= 10 || 'Use at least 10 characters.',
                maxLength: { value: 240, message: 'Use 240 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField label="Price" htmlFor="plan-price" error={errors.price?.message}>
            <input
              id="plan-price"
              type="number"
              inputMode="decimal"
              min="0.01"
              step="0.01"
              placeholder="0.00"
              aria-invalid={Boolean(errors.price)}
              aria-describedby={errors.price ? 'plan-price-error' : undefined}
              className={getFieldClassName(Boolean(errors.price), 'h-11 px-3')}
              {...register('price', {
                required: 'Price is required.',
                validate: (value) => {
                  const amount = Number(value)
                  if (!Number.isFinite(amount) || amount <= 0) {
                    return 'Enter a price greater than zero.'
                  }
                  return true
                },
              })}
            />
          </FormField>

          <FormField label="Currency" htmlFor="plan-currency" error={errors.currency?.message}>
            <select
              id="plan-currency"
              aria-invalid={Boolean(errors.currency)}
              aria-describedby={errors.currency ? 'plan-currency-error' : undefined}
              className={getFieldClassName(Boolean(errors.currency), 'h-11 px-3')}
              {...register('currency', { required: 'Currency is required.' })}
            >
              {planCurrencies.map((currency) => (
                <option key={currency} value={currency}>
                  {currency}
                </option>
              ))}
            </select>
          </FormField>

          <FormField
            label="Billing Cycle"
            htmlFor="plan-billing-cycle"
            error={errors.billingCycle?.message}
          >
            <select
              id="plan-billing-cycle"
              aria-invalid={Boolean(errors.billingCycle)}
              aria-describedby={errors.billingCycle ? 'plan-billing-cycle-error' : undefined}
              className={getFieldClassName(Boolean(errors.billingCycle), 'h-11 px-3')}
              {...register('billingCycle', { required: 'Billing cycle is required.' })}
            >
              {planBillingCycles.map((cycle) => (
                <option key={cycle} value={cycle}>
                  {cycle}
                </option>
              ))}
            </select>
          </FormField>

          <FormField
            label="Trial Period (days)"
            htmlFor="plan-trial-days"
            error={errors.trialDays?.message}
          >
            <input
              id="plan-trial-days"
              type="number"
              inputMode="numeric"
              min="0"
              step="1"
              placeholder="0"
              aria-invalid={Boolean(errors.trialDays)}
              aria-describedby={errors.trialDays ? 'plan-trial-days-error' : undefined}
              className={getFieldClassName(Boolean(errors.trialDays), 'h-11 px-3')}
              {...register('trialDays', {
                required: 'Trial period is required.',
                validate: (value) => {
                  const days = Number(value)
                  if (!Number.isInteger(days) || days < 0) {
                    return 'Enter 0 or a positive whole number.'
                  }
                  return true
                },
              })}
            />
          </FormField>

          <FormField
            label="Status"
            htmlFor="plan-status"
            error={errors.status?.message}
            className="sm:col-span-2"
          >
            <select
              id="plan-status"
              aria-invalid={Boolean(errors.status)}
              aria-describedby={errors.status ? 'plan-status-error' : undefined}
              className={getFieldClassName(Boolean(errors.status), 'h-11 px-3')}
              {...register('status', { required: 'Status is required.' })}
            >
              {planStatuses.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </FormField>

          <div className="sm:col-span-2">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <label htmlFor="plan-feature-draft" className="text-sm font-medium text-foreground">
                Plan Features
              </label>
              <span className="text-xs text-muted">{features.length}/8 features</span>
            </div>
            <div className="mt-1.5 flex flex-col gap-2 sm:flex-row">
              <input
                id="plan-feature-draft"
                ref={featureInputRef}
                type="text"
                value={featureDraft}
                onChange={(event) => {
                  setFeatureDraft(event.target.value)
                  if (featuresError) {
                    setFeaturesError('')
                  }
                }}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    event.preventDefault()
                    addFeature()
                  }
                }}
                autoComplete="off"
                maxLength={60}
                placeholder="e.g. Unlimited users"
                aria-invalid={Boolean(featuresError)}
                aria-describedby={featuresError ? 'plan-features-error' : 'plan-features-hint'}
                className={getFieldClassName(Boolean(featuresError), 'h-11 flex-1 px-3')}
              />
              <button
                type="button"
                onClick={addFeature}
                disabled={features.length >= 8}
                className="inline-flex h-11 shrink-0 items-center justify-center gap-2 rounded-lg border border-brand/30 bg-brand/10 px-4 text-sm font-semibold text-brand transition-colors hover:bg-brand/15 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <Plus className="h-4 w-4" aria-hidden="true" />
                Add feature
              </button>
            </div>
            {featuresError ? (
              <p
                id="plan-features-error"
                role="alert"
                className="mt-1.5 text-xs font-medium text-rose-600 dark:text-rose-400"
              >
                {featuresError}
              </p>
            ) : (
              <p id="plan-features-hint" className="mt-1.5 text-xs text-muted">
                Add up to 8 features. Press Enter to add the current feature.
              </p>
            )}

            {features.length > 0 && (
              <ul className="mt-3 flex flex-wrap gap-2" aria-label="Included plan features">
                {features.map((feature) => (
                  <li
                    key={feature}
                    className="inline-flex max-w-full items-center gap-1.5 rounded-full border border-brand/20 bg-brand/10 py-1 pl-3 pr-1.5 text-xs font-medium text-brand"
                  >
                    <Sparkles className="h-3 w-3 shrink-0" aria-hidden="true" />
                    <span className="truncate" title={feature}>
                      {feature}
                    </span>
                    <button
                      type="button"
                      onClick={() => removeFeature(feature)}
                      aria-label={`Remove ${feature}`}
                      className="inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-brand/80 transition-colors hover:bg-brand/15 hover:text-brand"
                    >
                      <X className="h-3 w-3" aria-hidden="true" />
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>

      <div className="flex shrink-0 flex-col-reverse gap-2 border-t border-border bg-background/30 px-5 py-4 sm:flex-row sm:justify-end sm:px-6">
        <button
          type="button"
          onClick={onCancel}
          className="inline-flex h-10 items-center justify-center rounded-lg border border-border bg-card px-4 text-sm font-semibold text-muted transition-colors hover:border-brand/40 hover:text-foreground"
        >
          Cancel
        </button>
        <button
          type="submit"
          className="inline-flex h-10 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white transition-colors hover:bg-brand-dark"
        >
          {plan ? (
            <Save className="h-4 w-4" aria-hidden="true" />
          ) : (
            <Plus className="h-4 w-4" aria-hidden="true" />
          )}
          {plan ? 'Save Changes' : 'Create Plan'}
        </button>
      </div>
    </form>
  )
}

export default function PlanFormModal({ open, plan, plans, onSave, onClose }) {
  if (!open) {
    return null
  }

  return (
    <Modal open={open} onClose={onClose} titleId={titleId} maxWidthClassName="max-w-3xl">
      <PlanForm plan={plan} plans={plans} onSave={onSave} onCancel={onClose} />
    </Modal>
  )
}
