import { useState } from 'react'
import toast from 'react-hot-toast'
import { Info, Package, Plus } from 'lucide-react'
import Modal from '../../components/common/Modal.jsx'
import Badge from '../../components/common/Badge.jsx'
import ProductDeleteDialog from '../../components/products/ProductDeleteDialog.jsx'
import ProductDetails from '../../components/products/ProductDetails.jsx'
import ProductFilters from '../../components/products/ProductFilters.jsx'
import ProductForm from '../../components/products/ProductForm.jsx'
import ProductSummaryCards from '../../components/products/ProductSummaryCards.jsx'
import ProductTable from '../../components/products/ProductTable.jsx'
import {
  productCategories,
  productDemoData,
} from '../../data/productData.js'

const pageSize = 6

function getInitialProducts() {
  return productDemoData.map((product) => ({
    ...product,
    variants: product.variants.map((variant) => ({ ...variant })),
  }))
}

function getToday() {
  const today = new Date()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${today.getFullYear()}-${month}-${day}`
}

export default function ProductsPage() {
  const [products, setProducts] = useState(getInitialProducts)
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('all')
  const [status, setStatus] = useState('all')
  const [page, setPage] = useState(1)
  const [formProductId, setFormProductId] = useState(null)
  const [viewingProductId, setViewingProductId] = useState(null)
  const [deletingProductId, setDeletingProductId] = useState(null)

  const normalizedSearch = search.trim().toLowerCase()
  const hasActiveFilters =
    Boolean(normalizedSearch) || category !== 'all' || status !== 'all'

  const filteredProducts = products.filter((product) => {
    const matchesSearch =
      !normalizedSearch ||
      product.name.toLowerCase().includes(normalizedSearch) ||
      product.code.toLowerCase().includes(normalizedSearch)
    const matchesCategory = category === 'all' || product.category === category
    const matchesStatus = status === 'all' || product.status === status

    return matchesSearch && matchesCategory && matchesStatus
  })

  const totalPages = Math.max(1, Math.ceil(filteredProducts.length / pageSize))
  const safePage = Math.min(page, totalPages)
  const pageProducts = filteredProducts.slice(
    (safePage - 1) * pageSize,
    safePage * pageSize,
  )

  const editingProduct = products.find((product) => product.id === formProductId) ?? null
  const viewingProduct =
    products.find((product) => product.id === viewingProductId) ?? null
  const deletingProduct =
    products.find((product) => product.id === deletingProductId) ?? null

  function handleClearFilters() {
    setSearch('')
    setCategory('all')
    setStatus('all')
    setPage(1)
  }

  function handleFormSubmit(values) {
    if (editingProduct) {
      setProducts((current) =>
        current.map((product) =>
          product.id === editingProduct.id
            ? { ...product, ...values, updatedAt: getToday() }
            : product,
        ),
      )
      toast.success('Product updated successfully.')
    } else {
      const newProduct = {
        id: `prod-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
        ...values,
        createdAt: getToday(),
        updatedAt: getToday(),
        variants: [],
      }
      setProducts((current) => [newProduct, ...current])
      handleClearFilters()
      toast.success('Product added successfully.')
    }

    setFormProductId(null)
  }

  function handleDeleteConfirm() {
    if (!deletingProduct) {
      return
    }

    setProducts((current) =>
      current.filter((product) => product.id !== deletingProduct.id),
    )
    setDeletingProductId(null)
    toast.success(`${deletingProduct.name} deleted successfully.`)
  }

  function handleVariantsChange(nextVariants) {
    if (!viewingProductId) {
      return
    }

    setProducts((current) =>
      current.map((product) =>
        product.id === viewingProductId
          ? { ...product, variants: nextVariants, updatedAt: getToday() }
          : product,
      ),
    )
  }

  return (
    <div className="mx-auto w-full max-w-[1400px] px-4 py-8 sm:px-6 lg:px-8 lg:py-10">
      <div className="relative overflow-hidden rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-6 lg:p-7">
        <div
          className="pointer-events-none absolute -right-16 -top-24 h-64 w-64 rounded-full bg-brand/10 blur-3xl"
          aria-hidden="true"
        />

        <div className="relative flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="flex flex-wrap items-center gap-2.5">
              <span className="inline-flex items-center gap-2 rounded-full border border-brand/20 bg-brand/10 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.16em] text-brand">
                <Package className="h-3.5 w-3.5" aria-hidden="true" />
                Catalogue
              </span>
              <Badge tone="neutral">Demo data</Badge>
            </div>

            <h1 className="mt-4 text-2xl font-bold tracking-tight text-foreground sm:text-3xl">
              Products
            </h1>
            <p className="mt-1.5 max-w-2xl text-sm text-muted sm:text-base">
              Manage the products and services offered through SubscriptIQ.
            </p>
          </div>

          <button
            type="button"
            onClick={() => setFormProductId('new')}
            data-modal-fallback-focus
            className="inline-flex h-10 shrink-0 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white shadow-sm shadow-brand/20 transition-colors hover:bg-brand-dark"
          >
            <Plus className="h-4 w-4" aria-hidden="true" />
            Add Product
          </button>
        </div>
      </div>

      <div className="mt-6 flex items-start gap-2.5 rounded-xl border border-brand/15 bg-brand/5 px-4 py-3 text-sm text-muted">
        <Info className="mt-0.5 h-4 w-4 shrink-0 text-brand" aria-hidden="true" />
        <p>
          This module uses local demonstration data. Add, edit, and delete actions update this
          page only and reset when you refresh the browser.
        </p>
      </div>

      <div className="mt-6">
        <ProductSummaryCards products={products} />
      </div>

      <div className="mt-6">
        <ProductFilters
          search={search}
          onSearchChange={(value) => {
            setSearch(value)
            setPage(1)
          }}
          category={category}
          onCategoryChange={(value) => {
            setCategory(value)
            setPage(1)
          }}
          status={status}
          onStatusChange={(value) => {
            setStatus(value)
            setPage(1)
          }}
          categories={productCategories}
          resultCount={filteredProducts.length}
          totalCount={products.length}
          onClear={handleClearFilters}
          hasActiveFilters={hasActiveFilters}
        />
      </div>

      <div className="mt-6">
        <ProductTable
          products={pageProducts}
          filteredCount={filteredProducts.length}
          page={safePage}
          pageSize={pageSize}
          totalPages={totalPages}
          onPageChange={setPage}
          onView={(product) => setViewingProductId(product.id)}
          onEdit={(product) => setFormProductId(product.id)}
          onDelete={(product) => setDeletingProductId(product.id)}
          onClearFilters={handleClearFilters}
          onAdd={() => setFormProductId('new')}
          hasActiveFilters={hasActiveFilters}
        />
      </div>

      <p className="mt-8 text-center text-xs text-muted">
        Product records on this page are demonstration data and are not connected to a backend.
      </p>

      <Modal
        open={formProductId !== null}
        onClose={() => setFormProductId(null)}
        titleId="product-form-title"
        maxWidthClassName="max-w-2xl"
      >
        {formProductId !== null && (
          <ProductForm
            key={formProductId}
            titleId="product-form-title"
            product={editingProduct}
            products={products}
            onSubmit={handleFormSubmit}
            onCancel={() => setFormProductId(null)}
          />
        )}
      </Modal>

      <Modal
        open={Boolean(viewingProduct)}
        onClose={() => setViewingProductId(null)}
        titleId="product-details-title"
        maxWidthClassName="max-w-3xl"
      >
        {viewingProduct && (
          <ProductDetails
            key={viewingProduct.id}
            titleId="product-details-title"
            product={viewingProduct}
            onEdit={() => {
              setFormProductId(viewingProduct.id)
              setViewingProductId(null)
            }}
            onCancel={() => setViewingProductId(null)}
            onVariantsChange={handleVariantsChange}
          />
        )}
      </Modal>

      <Modal
        open={Boolean(deletingProduct)}
        onClose={() => setDeletingProductId(null)}
        titleId="product-delete-title"
        maxWidthClassName="max-w-lg"
      >
        {deletingProduct && (
          <ProductDeleteDialog
            titleId="product-delete-title"
            product={deletingProduct}
            onCancel={() => setDeletingProductId(null)}
            onConfirm={handleDeleteConfirm}
          />
        )}
      </Modal>
    </div>
  )
}
