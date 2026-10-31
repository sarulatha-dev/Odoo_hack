import { Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from '../components/layout/AppLayout.jsx'
import HomePage from '../pages/HomePage.jsx'
import NotFoundPage from '../pages/NotFoundPage.jsx'
import CustomersPage from '../pages/admin/CustomersPage.jsx'
import DashboardPage from '../pages/admin/DashboardPage.jsx'
import InvoicesPage from '../pages/admin/InvoicesPage.jsx'
import PaymentsPage from '../pages/admin/PaymentsPage.jsx'
import PlansPage from '../pages/admin/PlansPage.jsx'
import ProductsPage from '../pages/admin/ProductsPage.jsx'
import QuotationsRoutes from '../pages/quotations/routes.jsx'
import ReportsPage from '../pages/admin/ReportsPage.jsx'
import SubscriptionsPage from '../pages/admin/SubscriptionsPage.jsx'
import LoginPage from '../pages/auth/LoginPage.jsx'
import { ROUTES } from './paths.js'

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path={ROUTES.home} element={<HomePage />} />
        <Route path={ROUTES.login} element={<LoginPage />} />
        <Route path="/admin">
          <Route index element={<Navigate to={ROUTES.admin.dashboard} replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="products" element={<ProductsPage />} />
          <Route path="plans" element={<PlansPage />} />
          <Route path="customers" element={<CustomersPage />} />
          <Route path="quotations" element={<QuotationsRoutes />} />
          <Route path="subscriptions" element={<SubscriptionsPage />} />
          <Route path="invoices" element={<InvoicesPage />} />
          <Route path="payments" element={<PaymentsPage />} />
          <Route path="reports" element={<ReportsPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
