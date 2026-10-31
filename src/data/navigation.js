import { ROUTES } from '../routes/paths.js'

export const mainNavigation = [
  { label: 'Dashboard', to: ROUTES.admin.dashboard },
  { label: 'Products', to: ROUTES.admin.products },
  { label: 'Plans', to: ROUTES.admin.plans },
  { label: 'Customers', to: ROUTES.admin.customers },
  { label: 'Quotations', to: ROUTES.admin.quotations },
  { label: 'Subscriptions', to: ROUTES.admin.subscriptions },
  { label: 'Invoices', to: ROUTES.admin.invoices },
  { label: 'Payments', to: ROUTES.admin.payments },
  { label: 'Reports', to: ROUTES.admin.reports },
]
