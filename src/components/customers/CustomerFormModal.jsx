import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Info, Plus, Save } from 'lucide-react'
import {
  customerCountries,
  customerStatuses,
  customerTypes,
  taxRegistrationTypes,
} from '../../data/customersData.js'

function getDefaultValues(customer) {
  return {
    customerType: customer?.customerType ?? 'Individual',
    firstName: customer?.firstName ?? '',
    lastName: customer?.lastName ?? '',
    companyName: customer?.companyName ?? '',
    email: customer?.email ?? '',
    phone: customer?.phone ?? '',
    country: customer?.country ?? 'India',
    billingAddress: customer?.billingAddress ?? '',
    city: customer?.city ?? '',
    state: customer?.state ?? '',
    postalCode: customer?.postalCode ?? '',
    billingCountry: customer?.billingCountry ?? 'India',
    gstin: customer?.gstin ?? '',
    taxRegistrationType: customer?.taxRegistrationType ?? 'Unregistered',
    notes: customer?.notes ?? '',
    status: customer?.status ?? 'Active',
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

function FormField({ label, htmlFor, error, hint, required, className = '', children }) {
  return (
    <div className={className}>
      <label htmlFor={htmlFor} className="mb-1.5 block text-sm font-medium text-foreground">
        {label}
        {required && <span className="ml-0.5 text-rose-500">*</span>}
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

function SectionHeader({ title }) {
  return (
    <div className="col-span-full mt-2 first:mt-0">
      <h3 className="text-sm font-semibold text-foreground">{title}</h3>
      <div className="mt-2 border-b border-border" />
    </div>
  )
}

export default function CustomerFormModal({ titleId, customer, onSubmit, onCancel }) {
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm({ defaultValues: getDefaultValues(customer) })

  useEffect(() => {
    reset(getDefaultValues(customer))
  }, [customer, reset])

  const customerTypeValue = watch('customerType')
  const notesValue = watch('notes') ?? ''

  function submit(values) {
    onSubmit({
      customerType: values.customerType,
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      companyName: values.companyName.trim(),
      email: values.email.trim(),
      phone: values.phone.trim(),
      country: values.country,
      billingAddress: values.billingAddress.trim(),
      city: values.city.trim(),
      state: values.state.trim(),
      postalCode: values.postalCode.trim(),
      billingCountry: values.billingCountry,
      gstin: values.gstin.trim().toUpperCase(),
      taxRegistrationType: values.taxRegistrationType,
      notes: values.notes.trim(),
      status: values.status,
    })
  }

  return (
    <form noValidate onSubmit={handleSubmit(submit)}>
      <div className="border-b border-border px-5 py-5 pr-14 sm:px-6 sm:py-6 sm:pr-16">
        <h2 id={titleId} className="text-lg font-semibold tracking-tight text-foreground">
          {customer ? 'Edit Customer' : 'Create Customer'}
        </h2>
        <p className="mt-1 text-sm text-muted">
          {customer
            ? 'Update the customer details below. Changes stay in this browser session.'
            : 'Create a new demonstration customer for this browser session.'}
        </p>
      </div>

      <div className="max-h-[60vh] overflow-y-auto px-5 py-5 sm:px-6 sm:py-6">
        <div className="mb-5 flex items-start gap-2 rounded-lg border border-brand/15 bg-brand/5 px-3 py-2.5 text-xs text-muted">
          <Info className="mt-0.5 h-3.5 w-3.5 shrink-0 text-brand" aria-hidden="true" />
          <span>Demo mode: customer records are saved locally and are not sent to a backend.</span>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          {/* ── Customer Information ── */}
          <SectionHeader title="Customer Information" />

          <FormField
            label="Customer Type"
            htmlFor="customer-type"
            error={errors.customerType?.message}
            required
          >
            <select
              id="customer-type"
              aria-invalid={Boolean(errors.customerType)}
              className={getFieldClassName(Boolean(errors.customerType), 'h-11 px-3')}
              {...register('customerType', { required: 'Customer type is required.' })}
            >
              {customerTypes.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </FormField>

          <FormField
            label="Country"
            htmlFor="customer-country"
            error={errors.country?.message}
            required
          >
            <select
              id="customer-country"
              aria-invalid={Boolean(errors.country)}
              className={getFieldClassName(Boolean(errors.country), 'h-11 px-3')}
              {...register('country', { required: 'Country is required.' })}
            >
              {customerCountries.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </FormField>

          <FormField
            label="First Name"
            htmlFor="customer-first-name"
            error={errors.firstName?.message}
            required
          >
            <input
              id="customer-first-name"
              type="text"
              data-autofocus
              autoComplete="given-name"
              placeholder="e.g. Arjun"
              aria-invalid={Boolean(errors.firstName)}
              aria-describedby={errors.firstName ? 'customer-first-name-error' : undefined}
              className={getFieldClassName(Boolean(errors.firstName), 'h-11 px-3')}
              {...register('firstName', {
                required: 'First name is required.',
                validate: (value) =>
                  value.trim().length >= 2 || 'Use at least 2 characters.',
                maxLength: { value: 60, message: 'Use 60 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="Last Name"
            htmlFor="customer-last-name"
            error={errors.lastName?.message}
            required
          >
            <input
              id="customer-last-name"
              type="text"
              autoComplete="family-name"
              placeholder="e.g. Mehta"
              aria-invalid={Boolean(errors.lastName)}
              aria-describedby={errors.lastName ? 'customer-last-name-error' : undefined}
              className={getFieldClassName(Boolean(errors.lastName), 'h-11 px-3')}
              {...register('lastName', {
                required: 'Last name is required.',
                validate: (value) =>
                  value.trim().length >= 2 || 'Use at least 2 characters.',
                maxLength: { value: 60, message: 'Use 60 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="Company Name"
            htmlFor="customer-company"
            error={errors.companyName?.message}
            required={customerTypeValue === 'Business'}
            className="sm:col-span-2"
          >
            <input
              id="customer-company"
              type="text"
              autoComplete="organization"
              placeholder="e.g. Mehta Digital Solutions Pvt. Ltd."
              aria-invalid={Boolean(errors.companyName)}
              aria-describedby={errors.companyName ? 'customer-company-error' : undefined}
              className={getFieldClassName(Boolean(errors.companyName), 'h-11 px-3')}
              {...register('companyName', {
                validate: (value) => {
                  if (customerTypeValue === 'Business' && !value.trim()) {
                    return 'Company name is required for Business customers.'
                  }
                  return true
                },
                maxLength: { value: 120, message: 'Use 120 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="Email"
            htmlFor="customer-email"
            error={errors.email?.message}
            required
          >
            <input
              id="customer-email"
              type="email"
              autoComplete="email"
              placeholder="e.g. arjun@example.com"
              aria-invalid={Boolean(errors.email)}
              aria-describedby={errors.email ? 'customer-email-error' : undefined}
              className={getFieldClassName(Boolean(errors.email), 'h-11 px-3')}
              {...register('email', {
                required: 'Email is required.',
                pattern: {
                  value: /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/,
                  message: 'Enter a valid email address.',
                },
              })}
            />
          </FormField>

          <FormField
            label="Phone Number"
            htmlFor="customer-phone"
            error={errors.phone?.message}
            required
          >
            <input
              id="customer-phone"
              type="tel"
              autoComplete="tel"
              placeholder="e.g. +91 98765 43210"
              aria-invalid={Boolean(errors.phone)}
              aria-describedby={errors.phone ? 'customer-phone-error' : undefined}
              className={getFieldClassName(Boolean(errors.phone), 'h-11 px-3')}
              {...register('phone', {
                required: 'Phone number is required.',
                pattern: {
                  value: /^\+?[\d\s\-()]{7,20}$/,
                  message: 'Enter a valid phone number.',
                },
              })}
            />
          </FormField>

          {/* ── Billing Information ── */}
          <SectionHeader title="Billing Information" />

          <FormField
            label="Billing Address"
            htmlFor="customer-billing-address"
            error={errors.billingAddress?.message}
            className="sm:col-span-2"
          >
            <input
              id="customer-billing-address"
              type="text"
              autoComplete="street-address"
              placeholder="e.g. 402, Pinnacle Business Park"
              aria-invalid={Boolean(errors.billingAddress)}
              className={getFieldClassName(Boolean(errors.billingAddress), 'h-11 px-3')}
              {...register('billingAddress', {
                maxLength: { value: 200, message: 'Use 200 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="City"
            htmlFor="customer-city"
            error={errors.city?.message}
          >
            <input
              id="customer-city"
              type="text"
              autoComplete="address-level2"
              placeholder="e.g. Mumbai"
              aria-invalid={Boolean(errors.city)}
              className={getFieldClassName(Boolean(errors.city), 'h-11 px-3')}
              {...register('city', {
                maxLength: { value: 80, message: 'Use 80 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="State"
            htmlFor="customer-state"
            error={errors.state?.message}
          >
            <input
              id="customer-state"
              type="text"
              autoComplete="address-level1"
              placeholder="e.g. Maharashtra"
              aria-invalid={Boolean(errors.state)}
              className={getFieldClassName(Boolean(errors.state), 'h-11 px-3')}
              {...register('state', {
                maxLength: { value: 80, message: 'Use 80 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="Postal Code"
            htmlFor="customer-postal-code"
            error={errors.postalCode?.message}
          >
            <input
              id="customer-postal-code"
              type="text"
              autoComplete="postal-code"
              placeholder="e.g. 400069"
              aria-invalid={Boolean(errors.postalCode)}
              className={getFieldClassName(Boolean(errors.postalCode), 'h-11 px-3')}
              {...register('postalCode', {
                maxLength: { value: 20, message: 'Use 20 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="Billing Country"
            htmlFor="customer-billing-country"
            error={errors.billingCountry?.message}
          >
            <select
              id="customer-billing-country"
              aria-invalid={Boolean(errors.billingCountry)}
              className={getFieldClassName(Boolean(errors.billingCountry), 'h-11 px-3')}
              {...register('billingCountry')}
            >
              {customerCountries.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </FormField>

          <FormField
            label="GSTIN"
            htmlFor="customer-gstin"
            error={errors.gstin?.message}
            hint="15-character GST identification number (optional)"
          >
            <input
              id="customer-gstin"
              type="text"
              autoComplete="off"
              placeholder="e.g. 27AABCM1234A1Z5"
              aria-invalid={Boolean(errors.gstin)}
              aria-describedby={errors.gstin ? 'customer-gstin-error' : 'customer-gstin-hint'}
              className={getFieldClassName(Boolean(errors.gstin), 'h-11 px-3 font-mono uppercase')}
              {...register('gstin', {
                validate: (value) => {
                  const trimmed = value.trim()
                  if (!trimmed) return true
                  if (!/^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/i.test(trimmed)) {
                    return 'Enter a valid 15-character GSTIN (e.g. 27AABCM1234A1Z5).'
                  }
                  return true
                },
              })}
            />
          </FormField>

          <FormField
            label="Tax Registration Type"
            htmlFor="customer-tax-type"
            error={errors.taxRegistrationType?.message}
          >
            <select
              id="customer-tax-type"
              aria-invalid={Boolean(errors.taxRegistrationType)}
              className={getFieldClassName(Boolean(errors.taxRegistrationType), 'h-11 px-3')}
              {...register('taxRegistrationType')}
            >
              {taxRegistrationTypes.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </FormField>

          {/* ── Additional Information ── */}
          <SectionHeader title="Additional Information" />

          <FormField
            label="Customer Notes"
            htmlFor="customer-notes"
            error={errors.notes?.message}
            hint={`${notesValue.length}/300 characters`}
            className="sm:col-span-2"
          >
            <textarea
              id="customer-notes"
              rows={3}
              maxLength={300}
              placeholder="Internal notes about this customer"
              aria-invalid={Boolean(errors.notes)}
              aria-describedby={
                errors.notes ? 'customer-notes-error' : 'customer-notes-hint'
              }
              className={getFieldClassName(Boolean(errors.notes), 'resize-y px-3 py-2.5')}
              {...register('notes', {
                maxLength: { value: 300, message: 'Use 300 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="Status"
            htmlFor="customer-status"
            error={errors.status?.message}
            required
            className="sm:col-span-2"
          >
            <select
              id="customer-status"
              aria-invalid={Boolean(errors.status)}
              className={getFieldClassName(Boolean(errors.status), 'h-11 px-3')}
              {...register('status', { required: 'Status is required.' })}
            >
              {customerStatuses.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </FormField>
        </div>
      </div>

      <div className="flex flex-col-reverse gap-2 border-t border-border bg-background/30 px-5 py-4 sm:flex-row sm:justify-end sm:px-6">
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
          {customer ? (
            <Save className="h-4 w-4" aria-hidden="true" />
          ) : (
            <Plus className="h-4 w-4" aria-hidden="true" />
          )}
          {customer ? 'Save Changes' : 'Create Customer'}
        </button>
      </div>
    </form>
  )
}
