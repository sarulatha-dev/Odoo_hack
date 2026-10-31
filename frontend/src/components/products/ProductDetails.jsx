import { CalendarDays, Pencil, Package } from 'lucide-react'
import { formatCurrency, formatDate } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import StatusBadge from '../common/StatusBadge.jsx'
import ProductVariants from './ProductVariants.jsx'

function DetailItem({ label, children }) {
  return (
    <div>
      <dt className="text-xs font-medium uppercase tracking-[0.08em] text-muted">{label}</dt>
      <dd className="mt-1.5 text-sm font-medium text-foreground">{children}</dd>
    </div>
  )
}

export default function ProductDetails({ titleId, product, onEdit, onCancel, onVariantsChange }) {
  return (
    <div>
      <div className="border-b border-border bg-background/30 px-5 py-5 pr-14 sm:px-6 sm:py-6 sm:pr-16">
        <div className="flex items-start gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-brand/10 text-brand">
            <Package className="h-5 w-5" aria-hidden="true" />
          </span>
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h2
                id={titleId}
                className="break-words text-lg font-semibold tracking-tight text-foreground"
              >
                {product.name}
              </h2>
              <StatusBadge status={product.status} />
            </div>
            <div className="mt-1.5 flex flex-wrap items-center gap-2 text-sm text-muted">
              <span className="font-mono text-xs font-semibold">{product.code}</span>
              <span aria-hidden="true">•</span>
              <span>{product.category}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="max-h-[62vh] overflow-y-auto px-5 py-5 sm:px-6 sm:py-6">
        <dl className="grid gap-x-6 gap-y-5 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <dt className="text-xs font-medium uppercase tracking-[0.08em] text-muted">
              Description
            </dt>
            <dd className="mt-1.5 text-sm leading-6 text-foreground">
              {product.description || 'No description provided.'}
            </dd>
          </div>

          <DetailItem label="Base Price">
            <span className="text-xl font-bold tracking-tight text-foreground">
              {formatCurrency(product.basePrice, product.currency)}
            </span>
          </DetailItem>

          <DetailItem label="Currency">
            <Badge tone="neutral">{product.currency}</Badge>
          </DetailItem>

          <DetailItem label="Category">
            <Badge tone="brand">{product.category}</Badge>
          </DetailItem>

          <DetailItem label="Status">
            <StatusBadge status={product.status} />
          </DetailItem>

          <DetailItem label="Created Date">
            <span className="inline-flex items-center gap-1.5 text-muted">
              <CalendarDays className="h-3.5 w-3.5" aria-hidden="true" />
              {formatDate(product.createdAt)}
            </span>
          </DetailItem>

          <DetailItem label="Last Updated">
            <span className="inline-flex items-center gap-1.5 text-muted">
              <CalendarDays className="h-3.5 w-3.5" aria-hidden="true" />
              {formatDate(product.updatedAt)}
            </span>
          </DetailItem>
        </dl>

        <ProductVariants
          product={product}
          variants={product.variants}
          onVariantsChange={onVariantsChange}
        />
      </div>

      <div className="flex flex-col-reverse gap-2 border-t border-border bg-background/30 px-5 py-4 sm:flex-row sm:justify-end sm:px-6">
        <button
          type="button"
          onClick={onCancel}
          className="inline-flex h-10 items-center justify-center rounded-lg border border-border bg-card px-4 text-sm font-semibold text-muted transition-colors hover:border-brand/40 hover:text-foreground"
        >
          Close
        </button>
        <button
          type="button"
          onClick={onEdit}
          className="inline-flex h-10 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white transition-colors hover:bg-brand-dark"
        >
          <Pencil className="h-4 w-4" aria-hidden="true" />
          Edit Product
        </button>
      </div>
    </div>
  )
}
