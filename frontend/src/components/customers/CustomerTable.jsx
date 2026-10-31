import {
  ChevronLeft,
  ChevronRight,
  Eye,
  Pencil,
  Plus,
  Power,
  Trash2,
  UserSearch,
} from 'lucide-react'
import { formatCurrency, formatDate } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import DataTable from '../common/DataTable.jsx'
import SectionCard from '../common/SectionCard.jsx'
import StatusBadge from '../common/StatusBadge.jsx'

const actionButtonClassName =
  'inline-flex h-8 w-8 items-center justify-center rounded-lg text-muted transition-colors'

export default function CustomerTable({
  customers,
  filteredCount,
  page,
  pageSize,
  totalPages,
  onPageChange,
  onView,
  onEdit,
  onToggleStatus,
  onDelete,
  onClearFilters,
  onAdd,
  hasActiveFilters,
}) {
  const pageStart = filteredCount === 0 ? 0 : (page - 1) * pageSize + 1
  const pageEnd = Math.min(page * pageSize, filteredCount)

  const columns = [
    {
      key: 'id',
      header: 'Customer ID',
      render: (row) => (
        <span className="whitespace-nowrap font-mono text-xs font-semibold text-muted">
          {row.id}
        </span>
      ),
    },
    {
      key: 'name',
      header: 'Customer Name',
      render: (row) => (
        <div className="min-w-[140px]">
          <span className="block truncate font-semibold text-foreground" title={`${row.firstName} ${row.lastName}`}>
            {row.firstName} {row.lastName}
          </span>
          {row.companyName && (
            <span className="block truncate text-xs text-muted" title={row.companyName}>
              {row.companyName}
            </span>
          )}
        </div>
      ),
    },
    {
      key: 'email',
      header: 'Email',
      render: (row) => (
        <span className="block max-w-[200px] truncate text-muted" title={row.email}>
          {row.email}
        </span>
      ),
    },
    {
      key: 'phone',
      header: 'Phone',
      render: (row) => (
        <span className="whitespace-nowrap text-muted">{row.phone}</span>
      ),
    },
    {
      key: 'country',
      header: 'Country',
      render: (row) => (
        <span className="whitespace-nowrap text-muted">{row.country}</span>
      ),
    },
    {
      key: 'customerType',
      header: 'Type',
      render: (row) => (
        <Badge tone={row.customerType === 'Business' ? 'brand' : 'info'}>
          {row.customerType}
        </Badge>
      ),
    },
    {
      key: 'subscriptions',
      header: 'Subscriptions',
      align: 'right',
      render: (row) => (
        <span className="whitespace-nowrap font-semibold text-foreground">
          {row.subscriptions?.active ?? 0}
          <span className="text-muted font-normal"> / {row.subscriptions?.total ?? 0}</span>
        </span>
      ),
    },
    {
      key: 'revenue',
      header: 'Total Revenue',
      align: 'right',
      render: (row) => (
        <span className="whitespace-nowrap font-semibold text-foreground">
          {row.financial ? formatCurrency(row.financial.totalPaid, row.financial.currency) : '—'}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'createdAt',
      header: 'Created',
      render: (row) => (
        <span className="whitespace-nowrap text-muted">{formatDate(row.createdAt)}</span>
      ),
    },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (row) => (
        <div className="flex items-center justify-end gap-1">
          <button
            type="button"
            onClick={() => onView(row)}
            aria-label={`View ${row.firstName} ${row.lastName}`}
            title="View customer"
            className={`${actionButtonClassName} hover:bg-brand/10 hover:text-brand`}
          >
            <Eye className="h-4 w-4" aria-hidden="true" />
          </button>
          <button
            type="button"
            onClick={() => onEdit(row)}
            aria-label={`Edit ${row.firstName} ${row.lastName}`}
            title="Edit customer"
            className={`${actionButtonClassName} hover:bg-sky-500/10 hover:text-sky-600 dark:hover:text-sky-300`}
          >
            <Pencil className="h-4 w-4" aria-hidden="true" />
          </button>
          <button
            type="button"
            onClick={() => onToggleStatus(row)}
            aria-label={`${row.status === 'Active' ? 'Deactivate' : 'Activate'} ${row.firstName} ${row.lastName}`}
            title={row.status === 'Active' ? 'Deactivate' : 'Activate'}
            className={`${actionButtonClassName} ${
              row.status === 'Active'
                ? 'hover:bg-amber-500/10 hover:text-amber-600 dark:hover:text-amber-300'
                : 'hover:bg-emerald-500/10 hover:text-emerald-600 dark:hover:text-emerald-300'
            }`}
          >
            <Power className="h-4 w-4" aria-hidden="true" />
          </button>
          <button
            type="button"
            onClick={() => onDelete(row)}
            aria-label={`Delete ${row.firstName} ${row.lastName}`}
            title="Delete customer"
            className={`${actionButtonClassName} hover:bg-rose-500/10 hover:text-rose-600 dark:hover:text-rose-300`}
          >
            <Trash2 className="h-4 w-4" aria-hidden="true" />
          </button>
        </div>
      ),
    },
  ]

  return (
    <SectionCard
      title="Customer Directory"
      description="Browse and manage the local demonstration customer records."
      actions={
        <span className="text-[13px] font-medium text-muted">
          {filteredCount} {filteredCount === 1 ? 'record' : 'records'}
        </span>
      }
    >
      {customers.length === 0 ? (
        <div className="flex flex-col items-center justify-center px-4 py-12 text-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand/10 text-brand">
            <UserSearch className="h-6 w-6" aria-hidden="true" />
          </span>
          <h3 className="mt-4 text-base font-semibold text-foreground">
            {hasActiveFilters ? 'No customers match your filters' : 'No customers yet'}
          </h3>
          <p className="mt-1.5 max-w-md text-sm text-muted">
            {hasActiveFilters
              ? 'Try a different search term or clear the current filters.'
              : 'Add your first demonstration customer to start building the directory.'}
          </p>
          {hasActiveFilters ? (
            <button
              type="button"
              onClick={onClearFilters}
              className="mt-5 inline-flex h-9 items-center justify-center rounded-lg border border-border bg-card px-4 text-sm font-semibold text-foreground transition-colors hover:border-brand/40 hover:text-brand"
            >
              Clear filters
            </button>
          ) : (
            <button
              type="button"
              onClick={onAdd}
              className="mt-5 inline-flex h-9 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white transition-colors hover:bg-brand-dark"
            >
              <Plus className="h-4 w-4" aria-hidden="true" />
              Add Customer
            </button>
          )}
        </div>
      ) : (
        <>
          <DataTable
            columns={columns}
            rows={customers}
            getRowKey={(row) => row.id}
            emptyMessage="No customers found."
            minWidthClassName="min-w-[1280px]"
            ariaLabel="Customer directory"
          />

          <div className="mt-5 flex flex-col gap-3 border-t border-border pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-[13px] text-muted" aria-live="polite">
              Showing {pageStart}–{pageEnd} of {filteredCount} customers
            </p>

            <div className="flex flex-wrap items-center gap-1.5">
              <button
                type="button"
                onClick={() => onPageChange(page - 1)}
                disabled={page === 1}
                aria-label="Previous page"
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-border bg-card text-muted transition-colors hover:border-brand/40 hover:text-foreground disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronLeft className="h-4 w-4" aria-hidden="true" />
              </button>

              {Array.from({ length: totalPages }, (_, index) => index + 1).map((pageNumber) => (
                <button
                  key={pageNumber}
                  type="button"
                  onClick={() => onPageChange(pageNumber)}
                  aria-label={`Go to page ${pageNumber}`}
                  aria-current={pageNumber === page ? 'page' : undefined}
                  className={`inline-flex h-9 min-w-9 items-center justify-center rounded-lg border px-2 text-sm font-semibold transition-colors ${
                    pageNumber === page
                      ? 'border-brand bg-brand text-white'
                      : 'border-border bg-card text-muted hover:border-brand/40 hover:text-foreground'
                  }`}
                >
                  {pageNumber}
                </button>
              ))}

              <button
                type="button"
                onClick={() => onPageChange(page + 1)}
                disabled={page === totalPages}
                aria-label="Next page"
                className="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-border bg-card text-muted transition-colors hover:border-brand/40 hover:text-foreground disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronRight className="h-4 w-4" aria-hidden="true" />
              </button>
            </div>
          </div>
        </>
      )}
    </SectionCard>
  )
}
