import {
  ChevronLeft,
  ChevronRight,
  Eye,
  PackageSearch,
  Pencil,
  Plus,
  Trash2,
} from 'lucide-react'
import { formatCurrency, formatDate } from '../../utils/formatters.js'
import Badge from '../common/Badge.jsx'
import DataTable from '../common/DataTable.jsx'
import SectionCard from '../common/SectionCard.jsx'
import StatusBadge from '../common/StatusBadge.jsx'

const actionButtonClassName =
  'inline-flex h-8 w-8 items-center justify-center rounded-lg text-muted transition-colors'

export default function ProductTable({
  products,
  filteredCount,
  page,
  pageSize,
  totalPages,
  onPageChange,
  onView,
  onEdit,
  onDelete,
  onClearFilters,
  onAdd,
  hasActiveFilters,
}) {
  const pageStart = filteredCount === 0 ? 0 : (page - 1) * pageSize + 1
  const pageEnd = Math.min(page * pageSize, filteredCount)

  const columns = [
    {
      key: 'code',
      header: 'Product Code',
      render: (row) => (
        <span className="whitespace-nowrap font-mono text-xs font-semibold text-muted">
          {row.code}
        </span>
      ),
    },
    {
      key: 'name',
      header: 'Product Name',
      render: (row) => (
        <span
          className="block max-w-[220px] min-w-[150px] truncate font-semibold text-foreground"
          title={row.name}
        >
          {row.name}
        </span>
      ),
    },
    {
      key: 'category',
      header: 'Category',
      render: (row) => <Badge tone="neutral">{row.category}</Badge>,
    },
    {
      key: 'description',
      header: 'Description',
      render: (row) => (
        <span className="block max-w-[260px] truncate text-muted" title={row.description}>
          {row.description || '—'}
        </span>
      ),
    },
    {
      key: 'basePrice',
      header: 'Base Price',
      align: 'right',
      render: (row) => (
        <span className="whitespace-nowrap font-semibold text-foreground">
          {formatCurrency(row.basePrice, row.currency)}
        </span>
      ),
    },
    {
      key: 'currency',
      header: 'Currency',
      render: (row) => (
        <span className="inline-flex rounded-md bg-background px-2 py-0.5 text-xs font-semibold text-muted">
          {row.currency}
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
      header: 'Created Date',
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
            aria-label={`View ${row.name}`}
            title="View product"
            className={`${actionButtonClassName} hover:bg-brand/10 hover:text-brand`}
          >
            <Eye className="h-4 w-4" aria-hidden="true" />
          </button>
          <button
            type="button"
            onClick={() => onEdit(row)}
            aria-label={`Edit ${row.name}`}
            title="Edit product"
            className={`${actionButtonClassName} hover:bg-sky-500/10 hover:text-sky-600 dark:hover:text-sky-300`}
          >
            <Pencil className="h-4 w-4" aria-hidden="true" />
          </button>
          <button
            type="button"
            onClick={() => onDelete(row)}
            aria-label={`Delete ${row.name}`}
            title="Delete product"
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
      title="Product Catalogue"
      description="Browse and manage the local demonstration product records."
      actions={
        <span className="text-[13px] font-medium text-muted">
          {filteredCount} {filteredCount === 1 ? 'record' : 'records'}
        </span>
      }
    >
      {products.length === 0 ? (
        <div className="flex flex-col items-center justify-center px-4 py-12 text-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand/10 text-brand">
            <PackageSearch className="h-6 w-6" aria-hidden="true" />
          </span>
          <h3 className="mt-4 text-base font-semibold text-foreground">
            {hasActiveFilters ? 'No products match your filters' : 'No products yet'}
          </h3>
          <p className="mt-1.5 max-w-md text-sm text-muted">
            {hasActiveFilters
              ? 'Try a different search term or clear the current category and status filters.'
              : 'Add your first demonstration product to start building the catalogue.'}
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
              Add Product
            </button>
          )}
        </div>
      ) : (
        <>
          <DataTable
            columns={columns}
            rows={products}
            getRowKey={(row) => row.id}
            emptyMessage="No products found."
            minWidthClassName="min-w-[1120px]"
            ariaLabel="Product catalogue"
          />

          <div className="mt-5 flex flex-col gap-3 border-t border-border pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-[13px] text-muted" aria-live="polite">
              Showing {pageStart}–{pageEnd} of {filteredCount} products
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
