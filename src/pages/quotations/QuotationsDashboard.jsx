// src/pages/quotations/QuotationsDashboard.jsx

import { useEffect, useState } from 'react';
import QuotationKpiCards from '../../components/quotations/QuotationKpiCards.jsx';
import QuotationTable from '../../components/quotations/QuotationTable.jsx';
import QuotationFilters from '../../components/quotations/QuotationFilters.jsx';
import { fetchQuotations } from '../../services/quotationService.js';
import { canAccess, Roles } from '../../services/permissionService.js';
import { useAuth } from '../../context/AuthContext.jsx'; // assume exists; if not, fallback to demo role
import SectionCard from '../../components/common/SectionCard.jsx';
import { Spinner } from '../../components/common/Spinner.jsx';
import { toast } from 'react-hot-toast';

export default function QuotationsDashboard() {
  const [stats, setStats] = useState({});
  const [quotations, setQuotations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({});
  const { user } = useAuth ? useAuth() : { role: Roles.ADMIN };

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await fetchQuotations(filters);
      setQuotations(data.items || []);
      // compute simple stats
      const totals = data.items?.reduce(
        (acc, q) => {
          acc.total = (acc.total || 0) + 1;
          acc[q.status] = (acc[q.status] || 0) + 1;
          return acc;
        },
        {},
      );
      setStats(totals || {});
    } catch (e) {
      toast.error('Failed to load quotations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  return (
    <div className="p-4">
      <QuotationKpiCards stats={stats} />
      <div className="mt-6 flex flex-col gap-4">
        <QuotationFilters onChange={setFilters} />
        {loading ? (
          <Spinner />
        ) : (
          <QuotationTable quotations={quotations} canEdit={canAccess(user?.role, 'quotations', 'edit')} />
        )}
      </div>
    </div>
  );
}
