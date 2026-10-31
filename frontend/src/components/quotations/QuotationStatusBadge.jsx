// src/components/quotations/QuotationStatusBadge.jsx

import { Badge } from '../common/Badge.jsx';

/**
 * Small badge reflecting quotation status.
 * Colours follow the existing theme palette.
 */
export default function QuotationStatusBadge({ status }) {
  const statusMap = {
    draft: { label: 'Draft', bg: 'bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200' },
    sent: { label: 'Sent', bg: 'bg-brand/10 text-brand' },
    accepted: { label: 'Accepted', bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-300' },
    rejected: { label: 'Rejected', bg: 'bg-rose-500/10 text-rose-600 dark:text-rose-300' },
  };
  const cfg = statusMap[status] || statusMap.draft;
  return <Badge className={`${cfg.bg} rounded-full px-2.5 py-0.5 text-xs font-medium`}>{cfg.label}</Badge>;
}
