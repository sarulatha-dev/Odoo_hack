import { AlertTriangle, Trash2 } from 'lucide-react'

export default function ProductDeleteDialog({ titleId, product, onCancel, onConfirm }) {
  return (
    <div>
      <div className="px-5 py-5 pr-14 sm:px-6 sm:py-6 sm:pr-16">
        <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-rose-500/10 text-rose-600 dark:text-rose-400">
          <AlertTriangle className="h-5 w-5" aria-hidden="true" />
        </span>
        <h2 id={titleId} className="mt-4 text-lg font-semibold tracking-tight text-foreground">
          Delete product?
        </h2>
        <p className="mt-1.5 text-sm leading-6 text-muted">
          This removes the record from the local catalogue for the current browser session. This
          action cannot be undone here.
        </p>
      </div>

      <div className="border-y border-border bg-background/30 px-5 py-3.5 sm:px-6">
        <p className="text-sm font-semibold text-foreground">{product.name}</p>
        <p className="mt-0.5 font-mono text-xs font-semibold text-muted">{product.code}</p>
      </div>

      <div className="flex flex-col-reverse gap-2 px-5 py-4 sm:flex-row sm:justify-end sm:px-6">
        <button
          type="button"
          data-autofocus
          onClick={onCancel}
          className="inline-flex h-10 items-center justify-center rounded-lg border border-border bg-card px-4 text-sm font-semibold text-muted transition-colors hover:border-brand/40 hover:text-foreground"
        >
          Cancel
        </button>
        <button
          type="button"
          onClick={onConfirm}
          className="inline-flex h-10 items-center justify-center gap-2 rounded-lg bg-rose-600 px-4 text-sm font-semibold text-white transition-colors hover:bg-rose-700"
        >
          <Trash2 className="h-4 w-4" aria-hidden="true" />
          Delete Product
        </button>
      </div>
    </div>
  )
}
