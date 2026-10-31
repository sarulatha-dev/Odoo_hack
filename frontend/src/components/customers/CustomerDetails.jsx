import {
  CalendarDays,
  CreditCard,
  FileText,
  Globe,
  Mail,
  MapPin,
  Pencil,
  Phone,
  Receipt,
  ShieldCheck,
  User,
} from 'lucide-react'
import { formatCurrency, formatDate } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import StatusBadge from '../common/StatusBadge.jsx'

function DetailItem({ label, children, icon: Icon }) {
  return (
    <div>
      <dt className="flex items-center gap-1.5 text-xs font-medium uppercase tracking-[0.08em] text-muted">
        {Icon && <Icon className="h-3 w-3" aria-hidden="true" />}
        {label}
      </dt>
      <dd className="mt-1.5 text-sm font-medium text-foreground">{children}</dd>
    </div>
  )
}

function SectionTitle({ title, icon: Icon }) {
  return (
    <div className="col-span-full mt-6 first:mt-0">
      <div className="flex items-center gap-2">
        {Icon && (
          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-brand/10 text-brand">
            <Icon className="h-3.5 w-3.5" aria-hidden="true" />
          </span>
        )}
        <h3 className="text-sm font-semibold text-foreground">{title}</h3>
      </div>
      <div className="mt-2 border-b border-border" />
    </div>
  )
}

function ActivityIcon({ type }) {
  const iconMap = {
    customer_created: { icon: User, color: 'text-brand bg-brand/10' },
    subscription_created: { icon: FileText, color: 'text-emerald-600 bg-emerald-500/10 dark:text-emerald-300' },
    subscription_renewed: { icon: ShieldCheck, color: 'text-sky-600 bg-sky-500/10 dark:text-sky-300' },
    subscription_cancelled: { icon: FileText, color: 'text-rose-600 bg-rose-500/10 dark:text-rose-300' },
    invoice_generated: { icon: Receipt, color: 'text-amber-600 bg-amber-500/10 dark:text-amber-300' },
    payment_received: { icon: CreditCard, color: 'text-emerald-600 bg-emerald-500/10 dark:text-emerald-300' },
  }

  const config = iconMap[type] || { icon: FileText, color: 'text-muted bg-slate-500/10' }
  const Icon = config.icon

  return (
    <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${config.color}`}>
      <Icon className="h-3.5 w-3.5" aria-hidden="true" />
    </span>
  )
}

export default function CustomerDetails({ titleId, customer, onEdit, onCancel }) {
  const fullName = `${customer.firstName} ${customer.lastName}`
  const hasGstin = Boolean(customer.gstin)
  const fin = customer.financial || {}
  const subs = customer.subscriptions || {}
  const activity = customer.activity || []

  return (
    <div>
      {/* ── Header ── */}
      <div className="border-b border-border bg-background/30 px-5 py-5 pr-14 sm:px-6 sm:py-6 sm:pr-16">
        <div className="flex items-start gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-brand/10 text-brand">
            <User className="h-5 w-5" aria-hidden="true" />
          </span>
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h2
                id={titleId}
                className="break-words text-lg font-semibold tracking-tight text-foreground"
              >
                {fullName}
              </h2>
              <StatusBadge status={customer.status} />
              <Badge tone={customer.customerType === 'Business' ? 'brand' : 'info'}>
                {customer.customerType}
              </Badge>
            </div>
            <div className="mt-1.5 flex flex-wrap items-center gap-2 text-sm text-muted">
              <span className="font-mono text-xs font-semibold">{customer.id}</span>
              {customer.companyName && (
                <>
                  <span aria-hidden="true">•</span>
                  <span>{customer.companyName}</span>
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* ── Content ── */}
      <div className="max-h-[62vh] overflow-y-auto px-5 py-5 sm:px-6 sm:py-6">
        <dl className="grid gap-x-6 gap-y-5 sm:grid-cols-2">
          {/* ── Profile Overview ── */}
          <SectionTitle title="Profile Overview" icon={User} />

          <DetailItem label="Full Name" icon={User}>{fullName}</DetailItem>
          {customer.companyName && (
            <DetailItem label="Company" icon={Globe}>{customer.companyName}</DetailItem>
          )}
          <DetailItem label="Email" icon={Mail}>
            <a href={`mailto:${customer.email}`} className="text-brand hover:underline">
              {customer.email}
            </a>
          </DetailItem>
          <DetailItem label="Phone" icon={Phone}>{customer.phone}</DetailItem>
          <DetailItem label="Country" icon={Globe}>{customer.country}</DetailItem>
          <DetailItem label="Customer Type">
            <Badge tone={customer.customerType === 'Business' ? 'brand' : 'info'}>
              {customer.customerType}
            </Badge>
          </DetailItem>

          {/* ── Billing Address ── */}
          <SectionTitle title="Billing Address" icon={MapPin} />

          <div className="sm:col-span-2">
            <dt className="text-xs font-medium uppercase tracking-[0.08em] text-muted">Address</dt>
            <dd className="mt-1.5 text-sm leading-6 text-foreground">
              {customer.billingAddress || '—'}
              {customer.city && <>, {customer.city}</>}
              {customer.state && <>, {customer.state}</>}
              {customer.postalCode && <> – {customer.postalCode}</>}
              <br />
              {customer.billingCountry || '—'}
            </dd>
          </div>

          {/* ── GST Information ── */}
          {(hasGstin || customer.taxRegistrationType !== 'Unregistered') && (
            <>
              <SectionTitle title="GST Information" icon={Receipt} />
              {hasGstin && (
                <DetailItem label="GSTIN">
                  <span className="font-mono text-sm font-semibold">{customer.gstin}</span>
                </DetailItem>
              )}
              <DetailItem label="Tax Registration">
                <Badge tone="neutral">{customer.taxRegistrationType}</Badge>
              </DetailItem>
            </>
          )}

          {/* ── Account ── */}
          <DetailItem label="Status">
            <StatusBadge status={customer.status} />
          </DetailItem>
          <DetailItem label="Created" icon={CalendarDays}>
            {formatDate(customer.createdAt)}
          </DetailItem>

          {customer.notes && (
            <div className="sm:col-span-2">
              <dt className="text-xs font-medium uppercase tracking-[0.08em] text-muted">Notes</dt>
              <dd className="mt-1.5 text-sm leading-6 text-foreground">{customer.notes}</dd>
            </div>
          )}

          {/* ── Subscription Summary ── */}
          <SectionTitle title="Subscription Summary" icon={FileText} />

          <div className="sm:col-span-2">
            <div className="grid gap-3 sm:grid-cols-4">
              {[
                { label: 'Total', value: subs.total ?? 0 },
                { label: 'Active', value: subs.active ?? 0, color: 'text-emerald-600 dark:text-emerald-300' },
                { label: 'Cancelled', value: subs.cancelled ?? 0, color: 'text-rose-600 dark:text-rose-300' },
              ].map((item) => (
                <div
                  key={item.label}
                  className="rounded-xl border border-border bg-background/50 px-4 py-3"
                >
                  <p className="text-xs font-medium text-muted">{item.label}</p>
                  <p className={`mt-1 text-2xl font-bold ${item.color ?? 'text-foreground'}`}>
                    {item.value}
                  </p>
                </div>
              ))}
              <div className="rounded-xl border border-border bg-background/50 px-4 py-3">
                <p className="text-xs font-medium text-muted">Current Plans</p>
                <p className="mt-1 text-2xl font-bold text-foreground">
                  {subs.plans?.length ?? 0}
                </p>
              </div>
            </div>
            {subs.plans && subs.plans.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-1.5">
                {subs.plans.map((plan) => (
                  <Badge key={plan} tone="brand">{plan}</Badge>
                ))}
              </div>
            )}
          </div>

          {/* ── Financial Summary ── */}
          <SectionTitle title="Financial Summary" icon={CreditCard} />

          <div className="sm:col-span-2">
            <div className="grid gap-3 sm:grid-cols-4">
              {[
                { label: 'Total Invoiced', value: fin.totalInvoiced ?? 0 },
                { label: 'Total Paid', value: fin.totalPaid ?? 0, color: 'text-emerald-600 dark:text-emerald-300' },
                { label: 'Outstanding', value: fin.outstanding ?? 0, color: 'text-amber-600 dark:text-amber-300' },
                {
                  label: 'Overdue',
                  value: fin.overdue ?? 0,
                  color: (fin.overdue ?? 0) > 0
                    ? 'text-rose-600 dark:text-rose-300'
                    : 'text-foreground',
                },
              ].map((item) => (
                <div
                  key={item.label}
                  className="rounded-xl border border-border bg-background/50 px-4 py-3"
                >
                  <p className="text-xs font-medium text-muted">{item.label}</p>
                  <p className={`mt-1 text-lg font-bold ${item.color ?? 'text-foreground'}`}>
                    {formatCurrency(item.value, fin.currency ?? 'INR')}
                  </p>
                </div>
              ))}
            </div>
          </div>

          {/* ── Recent Activity ── */}
          {activity.length > 0 && (
            <>
              <SectionTitle title="Recent Activity" icon={CalendarDays} />
              <div className="sm:col-span-2">
                <div className="space-y-3">
                  {activity.map((item, index) => (
                    <div key={index} className="flex items-start gap-3">
                      <ActivityIcon type={item.type} />
                      <div className="min-w-0 flex-1">
                        <p className="text-sm text-foreground">{item.description}</p>
                        <p className="mt-0.5 text-xs text-muted">{formatDate(item.date)}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}
        </dl>
      </div>

      {/* ── Footer ── */}
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
          Edit Customer
        </button>
      </div>
    </div>
  )
}
