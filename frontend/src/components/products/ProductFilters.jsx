import { ChevronDown, Search, SlidersHorizontal, X } from 'lucide-react'

const fieldClassName =
  'h-11 w-full rounded-lg border border-border bg-card text-sm text-foreground shadow-sm transition-colors hover:border-brand/40 focus:border-brand focus:outline-none'

export default function ProductFilters({
  search,
  onSearchChange,
  category,
  onCategoryChange,
  status,
  onStatusChange,
  categories,
  resultCount,
  totalCount,
  onClear,
  hasActiveFilters,
}) {
  return (
    <section className="rounded-2xl border border-border bg-card p-4 shadow-sm">
      <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_190px_170px_auto]">
        <div>
          <label htmlFor="product-search" className="sr-only">
            Search products
          </label>
          <div className="relative">
            <Search
              className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted"
              aria-hidden="true"
            />
            <input
              id="product-search"
              type="search"
              value={search}
              onChange={(event) => onSearchChange(event.target.value)}
              placeholder="Search by product name or code"
              className={`${fieldClassName} pl-10 pr-9`}
            />
          </div>
        </div>

        <div>
          <label htmlFor="product-category-filter" className="sr-only">
            Filter by category
          </label>
          <div className="relative">
            <select
              id="product-category-filter"
              value={category}
              onChange={(event) => onCategoryChange(event.target.value)}
              className={`${fieldClassName} appearance-none pl-3 pr-9`}
            >
              <option value="all">All categories</option>
              {categories.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
            <ChevronDown
              className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted"
              aria-hidden="true"
            />
          </div>
        </div>

        <div>
          <label htmlFor="product-status-filter" className="sr-only">
            Filter by status
          </label>
          <div className="relative">
            <select
              id="product-status-filter"
              value={status}
              onChange={(event) => onStatusChange(event.target.value)}
              className={`${fieldClassName} appearance-none pl-3 pr-9`}
            >
              <option value="all">All statuses</option>
              <option value="Active">Active</option>
              <option value="Inactive">Inactive</option>
            </select>
            <ChevronDown
              className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted"
              aria-hidden="true"
            />
          </div>
        </div>

        <button
          type="button"
          onClick={onClear}
          disabled={!hasActiveFilters}
          className="inline-flex h-11 items-center justify-center gap-2 whitespace-nowrap rounded-lg border border-border bg-card px-4 text-sm font-semibold text-muted transition-colors hover:border-brand/40 hover:text-foreground disabled:cursor-not-allowed disabled:opacity-50"
        >
          <X className="h-4 w-4" aria-hidden="true" />
          Clear filters
        </button>
      </div>

      <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-xs text-muted">
        <span aria-live="polite">
          Showing <span className="font-semibold text-foreground">{resultCount}</span> of{' '}
          {totalCount} products
        </span>
        <span className="inline-flex items-center gap-1.5">
          <SlidersHorizontal className="h-3.5 w-3.5" aria-hidden="true" />
          {hasActiveFilters ? 'Filters applied' : 'No filters applied'}
        </span>
      </div>
    </section>
  )
}
