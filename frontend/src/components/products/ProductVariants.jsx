import { useEffect, useRef, useState } from 'react'
import { Check, Layers, Pencil, Plus, Trash2, X } from 'lucide-react'
import toast from 'react-hot-toast'
import { productStatuses } from '../../data/productData.js'
import { formatCurrency } from '../../utils/formatters.js'
import StatusBadge from '../common/StatusBadge.jsx'

const emptyForm = {
  name: '',
  code: '',
  price: '',
  status: 'Active',
}

function createVariantId() {
  return `var-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`
}

function getInputClassName(hasError) {
  return [
    'h-9 w-full rounded-lg border bg-card px-2.5 text-sm text-foreground shadow-sm transition-colors focus:outline-none',
    hasError
      ? 'border-rose-500 focus:border-rose-500'
      : 'border-border hover:border-brand/40 focus:border-brand',
  ].join(' ')
}

export default function ProductVariants({ product, variants = [], onVariantsChange }) {
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [errors, setErrors] = useState({})
  const [confirmDeleteId, setConfirmDeleteId] = useState(null)
  const nameInputRef = useRef(null)
  const addVariantButtonRef = useRef(null)
  const confirmCancelRef = useRef(null)

  useEffect(() => {
    if (editingId) {
      nameInputRef.current?.focus()
    }
  }, [editingId])

  useEffect(() => {
    if (confirmDeleteId) {
      confirmCancelRef.current?.focus()
    }
  }, [confirmDeleteId])

  useEffect(() => {
    setEditingId(null)
    setConfirmDeleteId(null)
    setErrors({})
  }, [product.id])

  function startAdding() {
    setEditingId('new')
    setForm(emptyForm)
    setErrors({})
    setConfirmDeleteId(null)
  }

  function startEditing(variant) {
    setEditingId(variant.id)
    setForm({
      name: variant.name,
      code: variant.code,
      price: String(variant.price),
      status: variant.status,
    })
    setErrors({})
    setConfirmDeleteId(null)
  }

  function cancelEditing() {
    setEditingId(null)
    setForm(emptyForm)
    setErrors({})
    window.requestAnimationFrame(() => addVariantButtonRef.current?.focus())
  }

  function cancelVariantDelete(variantId) {
    setConfirmDeleteId(null)
    window.requestAnimationFrame(() => {
      document.getElementById(`variant-delete-${variantId}`)?.focus()
    })
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    if (errors[field]) {
      setErrors((current) => ({ ...current, [field]: undefined }))
    }
  }

  function validate() {
    const nextErrors = {}
    const name = form.name.trim()
    const code = form.code.trim().toUpperCase()
    const amount = Number(form.price)

    if (!name) {
      nextErrors.name = 'Name is required.'
    }

    if (!code) {
      nextErrors.code = 'Code is required.'
    } else if (!/^[A-Za-z0-9-]+$/.test(code)) {
      nextErrors.code = 'Use letters, numbers, and hyphens only.'
    } else {
      const duplicate = variants.some(
        (variant) => variant.id !== editingId && variant.code.toLowerCase() === code.toLowerCase(),
      )
      if (duplicate) {
        nextErrors.code = 'This code is already used for this product.'
      }
    }

    if (form.price === '' || !Number.isFinite(amount) || amount <= 0) {
      nextErrors.price = 'Enter a price above zero.'
    }

    setErrors(nextErrors)
    return Object.keys(nextErrors).length === 0
  }

  function handleSave(event) {
    event.preventDefault()

    if (!validate()) {
      return
    }

    const isNew = editingId === 'new'
    const variant = {
      id: isNew ? createVariantId() : editingId,
      name: form.name.trim(),
      code: form.code.trim().toUpperCase(),
      price: Number(form.price),
      status: form.status,
    }

    const nextVariants = isNew
      ? [...variants, variant]
      : variants.map((item) => (item.id === editingId ? variant : item))

    onVariantsChange(nextVariants)
    toast.success(isNew ? 'Variant added successfully.' : 'Variant updated successfully.')
    cancelEditing()
  }

  function removeVariant(variant) {
    onVariantsChange(variants.filter((item) => item.id !== variant.id))
    toast.success('Variant removed successfully.')
    setConfirmDeleteId(null)

    if (editingId === variant.id) {
      cancelEditing()
    } else {
      window.requestAnimationFrame(() => addVariantButtonRef.current?.focus())
    }
  }

  const editor = (
    <form
      onSubmit={handleSave}
      className="rounded-xl border border-brand/25 bg-brand/[0.04] p-3.5"
      noValidate
    >
      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <label
            htmlFor="variant-name"
            className="mb-1 block text-xs font-medium text-muted"
          >
            Variant name
          </label>
          <input
            id="variant-name"
            ref={nameInputRef}
            type="text"
            value={form.name}
            onChange={(event) => updateField('name', event.target.value)}
            autoComplete="off"
            placeholder="e.g. Growth"
            aria-invalid={Boolean(errors.name)}
            aria-describedby={errors.name ? 'variant-name-error' : undefined}
            className={getInputClassName(Boolean(errors.name))}
          />
          {errors.name && (
            <p id="variant-name-error" className="mt-1 text-xs font-medium text-rose-600 dark:text-rose-400">
              {errors.name}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="variant-code"
            className="mb-1 block text-xs font-medium text-muted"
          >
            Variant code
          </label>
          <input
            id="variant-code"
            type="text"
            value={form.code}
            onChange={(event) => updateField('code', event.target.value)}
            autoComplete="off"
            placeholder="e.g. ANL-GRW"
            aria-invalid={Boolean(errors.code)}
            aria-describedby={errors.code ? 'variant-code-error' : undefined}
            className={`${getInputClassName(Boolean(errors.code))} font-mono uppercase`}
          />
          {errors.code && (
            <p id="variant-code-error" className="mt-1 text-xs font-medium text-rose-600 dark:text-rose-400">
              {errors.code}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="variant-price"
            className="mb-1 block text-xs font-medium text-muted"
          >
            Price ({product.currency})
          </label>
          <input
            id="variant-price"
            type="number"
            inputMode="decimal"
            min="0.01"
            step="0.01"
            value={form.price}
            onChange={(event) => updateField('price', event.target.value)}
            placeholder="0.00"
            aria-invalid={Boolean(errors.price)}
            aria-describedby={errors.price ? 'variant-price-error' : undefined}
            className={getInputClassName(Boolean(errors.price))}
          />
          {errors.price && (
            <p id="variant-price-error" className="mt-1 text-xs font-medium text-rose-600 dark:text-rose-400">
              {errors.price}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="variant-status"
            className="mb-1 block text-xs font-medium text-muted"
          >
            Status
          </label>
          <select
            id="variant-status"
            value={form.status}
            onChange={(event) => updateField('status', event.target.value)}
            className={getInputClassName(false)}
          >
            {productStatuses.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="mt-3 flex justify-end gap-2">
        <button
          type="button"
          onClick={cancelEditing}
          className="inline-flex h-8 items-center justify-center gap-1.5 rounded-lg border border-border bg-card px-3 text-xs font-semibold text-muted transition-colors hover:text-foreground"
        >
          <X className="h-3.5 w-3.5" aria-hidden="true" />
          Cancel
        </button>
        <button
          type="submit"
          className="inline-flex h-8 items-center justify-center gap-1.5 rounded-lg bg-brand px-3 text-xs font-semibold text-white transition-colors hover:bg-brand-dark"
        >
          <Check className="h-3.5 w-3.5" aria-hidden="true" />
          {editingId === 'new' ? 'Add variant' : 'Save variant'}
        </button>
      </div>
    </form>
  )

  return (
    <section className="mt-6 border-t border-border pt-5">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <h3 className="flex items-center gap-2 text-sm font-semibold text-foreground">
            <Layers className="h-4 w-4 text-brand" aria-hidden="true" />
            Variants
            <span className="rounded-full bg-background px-2 py-0.5 text-[11px] font-semibold text-muted">
              {variants.length}
            </span>
          </h3>
          <p className="mt-1 text-xs text-muted">
            Variant prices are shown in {product.currency}.
          </p>
        </div>
        {editingId !== 'new' && (
          <button
            type="button"
            ref={addVariantButtonRef}
            onClick={startAdding}
            className="inline-flex h-8 items-center justify-center gap-1.5 rounded-lg border border-brand/30 bg-brand/10 px-3 text-xs font-semibold text-brand transition-colors hover:bg-brand/15"
          >
            <Plus className="h-3.5 w-3.5" aria-hidden="true" />
            Add variant
          </button>
        )}
      </div>

      <div className="mt-3 space-y-2.5">
        {editingId === 'new' && editor}

        {variants.map((variant) => {
          if (editingId === variant.id) {
            return <div key={variant.id}>{editor}</div>
          }

          const isConfirmingDelete = confirmDeleteId === variant.id

          return (
            <div
              key={variant.id}
              className="rounded-xl border border-border bg-background/30 px-3.5 py-3"
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-semibold text-foreground">{variant.name}</p>
                    <StatusBadge status={variant.status} />
                  </div>
                  <p className="mt-1 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-muted">
                    <span className="font-mono font-semibold">{variant.code}</span>
                    <span aria-hidden="true">•</span>
                    <span>{formatCurrency(variant.price, product.currency)}</span>
                  </p>
                </div>

                {!isConfirmingDelete && (
                  <div className="flex items-center gap-1">
                    <button
                      type="button"
                      onClick={() => startEditing(variant)}
                      aria-label={`Edit ${variant.name} variant`}
                      title="Edit variant"
                      className="inline-flex h-8 w-8 items-center justify-center rounded-lg text-muted transition-colors hover:bg-sky-500/10 hover:text-sky-600 dark:hover:text-sky-300"
                    >
                      <Pencil className="h-3.5 w-3.5" aria-hidden="true" />
                    </button>
                    <button
                      type="button"
                      id={`variant-delete-${variant.id}`}
                      onClick={() => setConfirmDeleteId(variant.id)}
                      aria-label={`Delete ${variant.name} variant`}
                      title="Delete variant"
                      className="inline-flex h-8 w-8 items-center justify-center rounded-lg text-muted transition-colors hover:bg-rose-500/10 hover:text-rose-600 dark:hover:text-rose-300"
                    >
                      <Trash2 className="h-3.5 w-3.5" aria-hidden="true" />
                    </button>
                  </div>
                )}
              </div>

              {isConfirmingDelete && (
                <div className="mt-2 flex flex-wrap items-center justify-between gap-2 rounded-lg border border-rose-500/20 bg-rose-500/5 px-2.5 py-2">
                  <span className="text-xs font-medium text-foreground">
                    Remove this variant?
                  </span>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      ref={confirmCancelRef}
                      onClick={() => cancelVariantDelete(variant.id)}
                      className="inline-flex h-7 items-center rounded-md border border-border bg-card px-2.5 text-xs font-semibold text-muted transition-colors hover:text-foreground"
                    >
                      Cancel
                    </button>
                    <button
                      type="button"
                      onClick={() => removeVariant(variant)}
                      className="inline-flex h-7 items-center rounded-md bg-rose-600 px-2.5 text-xs font-semibold text-white transition-colors hover:bg-rose-700"
                    >
                      Remove
                    </button>
                  </div>
                </div>
              )}
            </div>
          )
        })}

        {variants.length === 0 && editingId !== 'new' && (
          <div className="rounded-xl border border-dashed border-border px-4 py-6 text-center">
            <p className="text-sm font-medium text-foreground">No variants configured</p>
            <p className="mt-1 text-xs text-muted">
              Add optional pricing or packaging variants for this product.
            </p>
          </div>
        )}
      </div>
    </section>
  )
}
