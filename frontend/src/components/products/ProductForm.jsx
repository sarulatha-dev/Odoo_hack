import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Info, Plus, Save } from 'lucide-react'
import { productCategories, productCurrencies, productStatuses } from '../../data/productData.js'

function getDefaultValues(product) {
  return {
    name: product?.name ?? '',
    code: product?.code ?? '',
    description: product?.description ?? '',
    category: product?.category ?? productCategories[0],
    basePrice: product?.basePrice ?? '',
    currency: product?.currency ?? 'INR',
    status: product?.status ?? 'Active',
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

export default function ProductForm({ titleId, product, products, onSubmit, onCancel }) {
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm({ defaultValues: getDefaultValues(product) })

  useEffect(() => {
    reset(getDefaultValues(product))
  }, [product, reset])

  const descriptionValue = watch('description') ?? ''

  function submit(values) {
    onSubmit({
      name: values.name.trim(),
      code: values.code.trim().toUpperCase(),
      description: values.description.trim(),
      category: values.category,
      basePrice: Number(values.basePrice),
      currency: values.currency,
      status: values.status,
    })
  }

  return (
    <form noValidate onSubmit={handleSubmit(submit)}>
      <div className="border-b border-border px-5 py-5 pr-14 sm:px-6 sm:py-6 sm:pr-16">
        <h2 id={titleId} className="text-lg font-semibold tracking-tight text-foreground">
          {product ? 'Edit Product' : 'Add Product'}
        </h2>
        <p className="mt-1 text-sm text-muted">
          {product
            ? 'Update the product details below. Changes stay in this browser session.'
            : 'Create a new demonstration product for this browser session.'}
        </p>
      </div>

      <div className="px-5 py-5 sm:px-6 sm:py-6">
        <div className="mb-5 flex items-start gap-2 rounded-lg border border-brand/15 bg-brand/5 px-3 py-2.5 text-xs text-muted">
          <Info className="mt-0.5 h-3.5 w-3.5 shrink-0 text-brand" aria-hidden="true" />
          <span>Demo mode: product records are saved locally and are not sent to a backend.</span>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <FormField
            label="Product Name"
            htmlFor="product-name"
            error={errors.name?.message}
            className="sm:col-span-2"
          >
            <input
              id="product-name"
              type="text"
              data-autofocus
              autoComplete="off"
              placeholder="e.g. Analytics Pro Suite"
              aria-invalid={Boolean(errors.name)}
              aria-describedby={errors.name ? 'product-name-error' : undefined}
              className={getFieldClassName(Boolean(errors.name), 'h-11 px-3')}
              {...register('name', {
                required: 'Product name is required.',
                validate: (value) =>
                  value.trim().length >= 2 || 'Use at least 2 characters.',
                maxLength: { value: 80, message: 'Use 80 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="Product Code"
            htmlFor="product-code"
            error={errors.code?.message}
          >
            <input
              id="product-code"
              type="text"
              autoComplete="off"
              placeholder="e.g. ANLT-PRO"
              aria-invalid={Boolean(errors.code)}
              aria-describedby={errors.code ? 'product-code-error' : undefined}
              className={getFieldClassName(Boolean(errors.code), 'h-11 px-3 font-mono uppercase')}
              {...register('code', {
                required: 'Product code is required.',
                maxLength: { value: 24, message: 'Use 24 characters or fewer.' },
                pattern: {
                  value: /^[A-Za-z0-9-]+$/,
                  message: 'Use only letters, numbers, and hyphens.',
                },
                validate: (value) => {
                  const normalized = value.trim().toLowerCase()
                  const duplicate = products.some(
                    (item) => item.id !== product?.id && item.code.toLowerCase() === normalized,
                  )
                  return duplicate ? 'This product code is already in use.' : true
                },
              })}
            />
          </FormField>

          <FormField
            label="Category"
            htmlFor="product-category"
            error={errors.category?.message}
          >
            <select
              id="product-category"
              aria-invalid={Boolean(errors.category)}
              aria-describedby={errors.category ? 'product-category-error' : undefined}
              className={getFieldClassName(Boolean(errors.category), 'h-11 px-3')}
              {...register('category', { required: 'Category is required.' })}
            >
              {productCategories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </FormField>

          <FormField
            label="Description"
            htmlFor="product-description"
            error={errors.description?.message}
            hint={`${descriptionValue.length}/240 characters`}
            className="sm:col-span-2"
          >
            <textarea
              id="product-description"
              rows={3}
              maxLength={240}
              placeholder="Short description of the product offering"
              aria-invalid={Boolean(errors.description)}
              aria-describedby={
                errors.description ? 'product-description-error' : 'product-description-hint'
              }
              className={getFieldClassName(Boolean(errors.description), 'resize-y px-3 py-2.5')}
              {...register('description', {
                maxLength: { value: 240, message: 'Use 240 characters or fewer.' },
              })}
            />
          </FormField>

          <FormField
            label="Base Price"
            htmlFor="product-base-price"
            error={errors.basePrice?.message}
          >
            <input
              id="product-base-price"
              type="number"
              inputMode="decimal"
              min="0.01"
              step="0.01"
              placeholder="0.00"
              aria-invalid={Boolean(errors.basePrice)}
              aria-describedby={errors.basePrice ? 'product-base-price-error' : undefined}
              className={getFieldClassName(Boolean(errors.basePrice), 'h-11 px-3')}
              {...register('basePrice', {
                required: 'Base price is required.',
                validate: (value) => {
                  const amount = Number(value)
                  if (!Number.isFinite(amount) || amount <= 0) {
                    return 'Enter a base price greater than zero.'
                  }
                  return true
                },
              })}
            />
          </FormField>

          <FormField
            label="Currency"
            htmlFor="product-currency"
            error={errors.currency?.message}
          >
            <select
              id="product-currency"
              aria-invalid={Boolean(errors.currency)}
              aria-describedby={errors.currency ? 'product-currency-error' : undefined}
              className={getFieldClassName(Boolean(errors.currency), 'h-11 px-3')}
              {...register('currency', { required: 'Currency is required.' })}
            >
              {productCurrencies.map((currency) => (
                <option key={currency} value={currency}>
                  {currency}
                </option>
              ))}
            </select>
          </FormField>

          <FormField
            label="Status"
            htmlFor="product-status"
            error={errors.status?.message}
            className="sm:col-span-2"
          >
            <select
              id="product-status"
              aria-invalid={Boolean(errors.status)}
              aria-describedby={errors.status ? 'product-status-error' : undefined}
              className={getFieldClassName(Boolean(errors.status), 'h-11 px-3')}
              {...register('status', { required: 'Status is required.' })}
            >
              {productStatuses.map((status) => (
                <option key={status} value={status}>
                  {status}
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
          {product ? (
            <Save className="h-4 w-4" aria-hidden="true" />
          ) : (
            <Plus className="h-4 w-4" aria-hidden="true" />
          )}
          {product ? 'Save Changes' : 'Add Product'}
        </button>
      </div>
    </form>
  )
}
