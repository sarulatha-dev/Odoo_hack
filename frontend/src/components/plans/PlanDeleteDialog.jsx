import { AlertTriangle, Trash2 } from 'lucide-react'
import Badge from '../common/Badge.jsx'
import Modal from '../common/Modal.jsx'
import StatusBadge from '../common/StatusBadge.jsx'

const titleId = 'plan-delete-title'

const tierTones = {
  Low: 'success',
  Medium: 'warning',
  High: 'brand',
}

export default function PlanDeleteDialog({ open, plan, onClose, onConfirm }) {
  if (!open || !plan) {
    return null
  }

  return (
    <Modal open={open} onClose={onClose} titleId={titleId} maxWidthClassName="max-w-md">
      <div className="flex max-h-[calc(100dvh-2rem)] flex-col sm:max-h-[calc(100dvh-3rem)]">
        <div className="min-h-0 flex-1 overflow-y-auto p-5 pr-14 sm:p-6 sm:pr-16">
          <div className="flex items-start gap-3">
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-rose-500/10 text-rose-600 dark:text-rose-400">
              <AlertTriangle className="h-5 w-5" aria-hidden="true" />
            </span>
            <div className="min-w-0">
              <h2
                id={titleId}
                className="text-lg font-semibold tracking-tight text-foreground"
              >
                Delete Plan?
              </h2>
              <p className="mt-1 text-sm leading-6 text-muted">
                This plan will be removed from the local demonstration catalogue for this
                session. No backend data is affected.
              </p>
            </div>
          </div>

          <div className="mt-4 rounded-xl border border-border bg-background/50 p-3.5">
            <p className="break-words text-sm font-semibold text-foreground">{plan.name}</p>
            <div className="mt-1.5 flex flex-wrap items-center gap-2">
              <span className="font-mono text-xs font-semibold text-muted">{plan.code}</span>
              <Badge tone={tierTones[plan.tier] ?? 'neutral'}>{plan.tier}</Badge>
              <StatusBadge status={plan.status} />
            </div>
          </div>

          <p className="mt-4 text-xs text-muted">
            Demo data only — no live subscriptions, invoices, or customer records are affected.
          </p>
        </div>

        <div className="flex shrink-0 flex-col-reverse gap-2 border-t border-border bg-background/30 px-5 py-4 sm:flex-row sm:justify-end sm:px-6">
          <button
            type="button"
            onClick={onClose}
            data-autofocus
            className="inline-flex h-10 items-center justify-center rounded-lg border border-border bg-card px-4 text-sm font-semibold text-muted transition-colors hover:border-brand/40 hover:text-foreground"
          >
            Keep Plan
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="inline-flex h-10 items-center justify-center gap-2 rounded-lg bg-rose-600 px-4 text-sm font-semibold text-white transition-colors hover:bg-rose-700"
          >
            <Trash2 className="h-4 w-4" aria-hidden="true" />
            Delete Plan
          </button>
        </div>
      </div>
    </Modal>
  )
}
