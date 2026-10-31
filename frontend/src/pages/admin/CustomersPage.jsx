import { useState } from 'react'
import toast from 'react-hot-toast'
import { Info, Plus, Users } from 'lucide-react'
import Modal from '../../components/common/Modal.jsx'
import Badge from '../../components/common/Badge.jsx'
import CustomerDeleteDialog from '../../components/customers/CustomerDeleteDialog.jsx'
import CustomerDetails from '../../components/customers/CustomerDetails.jsx'
import CustomerFilters from '../../components/customers/CustomerFilters.jsx'
import CustomerFormModal from '../../components/customers/CustomerFormModal.jsx'
import CustomerKpiCards from '../../components/customers/CustomerKpiCards.jsx'
import CustomerTable from '../../components/customers/CustomerTable.jsx'
import { customerDemoData } from '../../data/customersData.js'

const pageSize = 8

function getInitialCustomers() {
  return customerDemoData.map((customer) => ({ ...customer }))
}

function getToday() {
  const today = new Date()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${today.getFullYear()}-${month}-${day}`
}

export default function CustomersPage() {
  const [customers, setCustomers] = useState(getInitialCustomers)
  const [search, setSearch] = useState('')
  const [country, setCountry] = useState('all')
  const [customerType, setCustomerType] = useState('all')
  const [status, setStatus] = useState('all')
  const [page, setPage] = useState(1)
  const [formCustomerId, setFormCustomerId] = useState(null)
  const [viewingCustomerId, setViewingCustomerId] = useState(null)
  const [deletingCustomerId, setDeletingCustomerId] = useState(null)

  const normalizedSearch = search.trim().toLowerCase()
  const hasActiveFilters =
    Boolean(normalizedSearch) || country !== 'all' || customerType !== 'all' || status !== 'all'

  const filteredCustomers = customers.filter((customer) => {
    const fullName = `${customer.firstName} ${customer.lastName}`.toLowerCase()
    const matchesSearch =
      !normalizedSearch ||
      fullName.includes(normalizedSearch) ||
      customer.id.toLowerCase().includes(normalizedSearch) ||
      (customer.companyName && customer.companyName.toLowerCase().includes(normalizedSearch)) ||
      customer.email.toLowerCase().includes(normalizedSearch)
    const matchesCountry = country === 'all' || customer.country === country
    const matchesType = customerType === 'all' || customer.customerType === customerType
    const matchesStatus = status === 'all' || customer.status === status

    return matchesSearch && matchesCountry && matchesType && matchesStatus
  })

  const totalPages = Math.max(1, Math.ceil(filteredCustomers.length / pageSize))
  const safePage = Math.min(page, totalPages)
  const pageCustomers = filteredCustomers.slice(
    (safePage - 1) * pageSize,
    safePage * pageSize,
  )

  const editingCustomer = customers.find((c) => c.id === formCustomerId) ?? null
  const viewingCustomer = customers.find((c) => c.id === viewingCustomerId) ?? null
  const deletingCustomer = customers.find((c) => c.id === deletingCustomerId) ?? null

  function handleClearFilters() {
    setSearch('')
    setCountry('all')
    setCustomerType('all')
    setStatus('all')
    setPage(1)
  }

  function handleFormSubmit(values) {
    if (editingCustomer) {
      setCustomers((current) =>
        current.map((c) =>
          c.id === editingCustomer.id
            ? { ...c, ...values, updatedAt: getToday() }
            : c,
        ),
      )
      toast.success(`${values.firstName} ${values.lastName} updated successfully.`)
    } else {
      const newCustomer = {
        id: `cust-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
        ...values,
        createdAt: getToday(),
        updatedAt: getToday(),
        subscriptions: {
          total: 0,
          active: 0,
          cancelled: 0,
          plans: [],
        },
        financial: {
          totalInvoiced: 0,
          totalPaid: 0,
          outstanding: 0,
          overdue: 0,
          currency: 'INR',
        },
        activity: [
          {
            type: 'customer_created',
            description: 'Customer account created',
            date: getToday(),
          },
        ],
      }
      setCustomers((current) => [newCustomer, ...current])
      handleClearFilters()
      toast.success(`${values.firstName} ${values.lastName} created successfully.`)
    }

    setFormCustomerId(null)
  }

  function handleDeleteConfirm() {
    if (!deletingCustomer) {
      return
    }

    const fullName = `${deletingCustomer.firstName} ${deletingCustomer.lastName}`
    setCustomers((current) =>
      current.filter((c) => c.id !== deletingCustomer.id),
    )
    setDeletingCustomerId(null)
    toast.success(`${fullName} deleted successfully.`)
  }

  function handleToggleStatus(customer) {
    const nextStatus = customer.status === 'Active' ? 'Inactive' : 'Active'
    const fullName = `${customer.firstName} ${customer.lastName}`
    setCustomers((current) =>
      current.map((c) =>
        c.id === customer.id
          ? { ...c, status: nextStatus, updatedAt: getToday() }
          : c,
      ),
    )
    toast.success(`${fullName} ${nextStatus === 'Active' ? 'activated' : 'deactivated'} successfully.`)
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
                <Users className="h-3.5 w-3.5" aria-hidden="true" />
                Directory
              </span>
              <Badge tone="neutral">Demo data</Badge>
            </div>

            <h1 className="mt-4 text-2xl font-bold tracking-tight text-foreground sm:text-3xl">
              Customers
            </h1>
            <p className="mt-1.5 max-w-2xl text-sm text-muted sm:text-base">
              Manage customer profiles, billing information and subscription relationships.
            </p>
          </div>

          <button
            type="button"
            onClick={() => setFormCustomerId('new')}
            data-modal-fallback-focus
            className="inline-flex h-10 shrink-0 items-center justify-center gap-2 rounded-lg bg-brand px-4 text-sm font-semibold text-white shadow-sm shadow-brand/20 transition-colors hover:bg-brand-dark"
          >
            <Plus className="h-4 w-4" aria-hidden="true" />
            Create Customer
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
        <CustomerKpiCards customers={customers} />
      </div>

      <div className="mt-6">
        <CustomerFilters
          search={search}
          onSearchChange={(value) => {
            setSearch(value)
            setPage(1)
          }}
          country={country}
          onCountryChange={(value) => {
            setCountry(value)
            setPage(1)
          }}
          customerType={customerType}
          onCustomerTypeChange={(value) => {
            setCustomerType(value)
            setPage(1)
          }}
          status={status}
          onStatusChange={(value) => {
            setStatus(value)
            setPage(1)
          }}
          resultCount={filteredCustomers.length}
          totalCount={customers.length}
          onClear={handleClearFilters}
          hasActiveFilters={hasActiveFilters}
        />
      </div>

      <div className="mt-6">
        <CustomerTable
          customers={pageCustomers}
          filteredCount={filteredCustomers.length}
          page={safePage}
          pageSize={pageSize}
          totalPages={totalPages}
          onPageChange={setPage}
          onView={(customer) => setViewingCustomerId(customer.id)}
          onEdit={(customer) => setFormCustomerId(customer.id)}
          onToggleStatus={handleToggleStatus}
          onDelete={(customer) => setDeletingCustomerId(customer.id)}
          onClearFilters={handleClearFilters}
          onAdd={() => setFormCustomerId('new')}
          hasActiveFilters={hasActiveFilters}
        />
      </div>

      <p className="mt-8 text-center text-xs text-muted">
        Customer records on this page are demonstration data and are not connected to a backend.
      </p>

      {/* Create / Edit Customer Modal */}
      <Modal
        open={formCustomerId !== null}
        onClose={() => setFormCustomerId(null)}
        titleId="customer-form-title"
        maxWidthClassName="max-w-2xl"
      >
        {formCustomerId !== null && (
          <CustomerFormModal
            key={formCustomerId}
            titleId="customer-form-title"
            customer={editingCustomer}
            onSubmit={handleFormSubmit}
            onCancel={() => setFormCustomerId(null)}
          />
        )}
      </Modal>

      {/* View Customer Details Modal */}
      <Modal
        open={Boolean(viewingCustomer)}
        onClose={() => setViewingCustomerId(null)}
        titleId="customer-details-title"
        maxWidthClassName="max-w-3xl"
      >
        {viewingCustomer && (
          <CustomerDetails
            key={viewingCustomer.id}
            titleId="customer-details-title"
            customer={viewingCustomer}
            onEdit={() => {
              setFormCustomerId(viewingCustomer.id)
              setViewingCustomerId(null)
            }}
            onCancel={() => setViewingCustomerId(null)}
          />
        )}
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        open={Boolean(deletingCustomer)}
        onClose={() => setDeletingCustomerId(null)}
        titleId="customer-delete-title"
        maxWidthClassName="max-w-lg"
      >
        {deletingCustomer && (
          <CustomerDeleteDialog
            titleId="customer-delete-title"
            customer={deletingCustomer}
            onCancel={() => setDeletingCustomerId(null)}
            onConfirm={handleDeleteConfirm}
          />
        )}
      </Modal>
    </div>
  )
}
