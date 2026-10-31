// src/components/quotations/QuotationKpiCards.jsx

import { BarChart2, FilePen, Send, CheckCircle, XCircle } from 'lucide-react';
import { formatNumber } from '../../utils/formatters.js';
import SectionCard from '../common/SectionCard.jsx';

/**
 * KPI cards for the Quotations dashboard.
 * Expects a `stats` object with counts for each status.
 */
export default function QuotationKpiCards({ stats }) {
  const { total = 0, draft = 0, sent = 0, accepted = 0, rejected = 0 } = stats || {};

  const cards = [
    { label: 'Total Quotations', value: total, icon: BarChart2, featured: true },
    { label: 'Draft', value: draft, icon: FilePen, tone: 'bg-amber-500/10 text-amber-600 dark:text-amber-300' },
    { label: 'Sent', value: sent, icon: Send, tone: 'bg-sky-500/10 text-sky-600 dark:text-sky-300' },
    { label: 'Accepted', value: accepted, icon: CheckCircle, tone: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-300' },
    { label: 'Rejected', value: rejected, icon: XCircle, tone: 'bg-rose-500/10 text-rose-600 dark:text-rose-300' },
  ];

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <SectionCard key={card.label} className="p-5">
            <div className="flex items-center gap-4">
              <div
                className={`flex h-11 w-11 items-center justify-center rounded-xl ${
                  card.featured ? 'bg-white/10 text-white' : card.tone
                }`}
              >
                <Icon className="h-5 w-5" aria-hidden="true" />
              </div>
              <div className="flex flex-col">
                <p className={`text-sm font-medium ${card.featured ? 'text-white/70' : 'text-muted'}`}>
                  {card.label}
                </p>
                <p
                  className={`mt-1.5 text-3xl font-bold leading-none tracking-tight ${
                    card.featured ? 'text-white' : 'text-foreground'
                  }`}
                >
                  {formatNumber(card.value)}
                </p>
              </div>
            </div>
          </SectionCard>
        );
      })}
    </div>
  );
}
