// src/pages/quotations/routes.jsx

import { Route, Routes } from 'react-router-dom';
import QuotationsDashboard from './QuotationsDashboard.jsx';
import QuotationForm from './QuotationForm.jsx';
import QuotationDetails from './QuotationDetails.jsx';

export default function QuotationsRoutes() {
  return (
    <Routes>
      {/* Dashboard listing */}
      <Route index element={<QuotationsDashboard />} />
      {/* Create new quotation */}
      <Route path="new" element={<QuotationForm />} />
      {/* View details */}
      <Route path=":id" element={<QuotationDetails />} />
      {/* Edit draft */}
      <Route path=":id/edit" element={<QuotationForm editMode={true} />} />
    </Routes>
  );
}
