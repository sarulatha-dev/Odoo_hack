export const productCategories = [
  'Software',
  'Cloud Services',
  'Analytics',
  'CRM',
  'Security',
  'Hosting',
]

export const productCurrencies = ['INR', 'USD', 'SGD', 'EUR', 'GBP', 'AED']

export const productStatuses = ['Active', 'Inactive']

export const productDemoData = [
  {
    id: 'prod-1001',
    code: 'PRD-SFT-001',
    name: 'SubscriptIQ Core',
    category: 'Software',
    description:
      'Subscription management suite for recurring billing, invoicing and revenue reporting.',
    basePrice: 24999,
    currency: 'INR',
    status: 'Active',
    createdAt: '2025-11-12',
    updatedAt: '2026-09-18',
    variants: [
      { id: 'var-1001-1', name: 'Starter', code: 'SFT-STR', price: 9999, status: 'Active' },
      { id: 'var-1001-2', name: 'Growth', code: 'SFT-GRW', price: 24999, status: 'Active' },
      { id: 'var-1001-3', name: 'Enterprise', code: 'SFT-ENT', price: 49999, status: 'Active' },
    ],
  },
  {
    id: 'prod-1002',
    code: 'PRD-CLD-002',
    name: 'CloudVault 1TB',
    category: 'Cloud Services',
    description:
      'Secure cloud storage with encrypted backups, file versioning and team access controls.',
    basePrice: 8999,
    currency: 'INR',
    status: 'Active',
    createdAt: '2026-01-08',
    updatedAt: '2026-08-22',
    variants: [
      { id: 'var-1002-1', name: '1TB Storage', code: 'CLD-1TB', price: 8999, status: 'Active' },
      { id: 'var-1002-2', name: '5TB Storage', code: 'CLD-5TB', price: 19999, status: 'Active' },
    ],
  },
  {
    id: 'prod-1003',
    code: 'PRD-ANL-003',
    name: 'InsightPro Analytics',
    category: 'Analytics',
    description:
      'Business intelligence workspace with interactive dashboards and scheduled performance reports.',
    basePrice: 349,
    currency: 'USD',
    status: 'Active',
    createdAt: '2025-12-03',
    updatedAt: '2026-09-02',
    variants: [
      { id: 'var-1003-1', name: 'Starter', code: 'ANL-STR', price: 349, status: 'Active' },
      { id: 'var-1003-2', name: 'Growth', code: 'ANL-GRW', price: 699, status: 'Active' },
    ],
  },
  {
    id: 'prod-1004',
    code: 'PRD-CRM-004',
    name: 'SalesPilot CRM',
    category: 'CRM',
    description:
      'Sales pipeline management with lead scoring, follow-up automation and team performance views.',
    basePrice: 199,
    currency: 'USD',
    status: 'Active',
    createdAt: '2026-02-17',
    updatedAt: '2026-08-30',
    variants: [
      { id: 'var-1004-1', name: 'Team', code: 'CRM-TEAM', price: 199, status: 'Active' },
      { id: 'var-1004-2', name: 'Business', code: 'CRM-BIZ', price: 399, status: 'Active' },
    ],
  },
  {
    id: 'prod-1005',
    code: 'PRD-SEC-005',
    name: 'ShieldGuard Endpoint',
    category: 'Security',
    description:
      'Endpoint protection with malware monitoring, device policies and centralised threat alerts.',
    basePrice: 1499,
    currency: 'INR',
    status: 'Inactive',
    createdAt: '2025-09-25',
    updatedAt: '2026-03-14',
    variants: [],
  },
  {
    id: 'prod-1006',
    code: 'PRD-HST-006',
    name: 'Business Hosting Plus',
    category: 'Hosting',
    description:
      'Managed business hosting with SSL, daily backups, staging environments and priority support.',
    basePrice: 4999,
    currency: 'INR',
    status: 'Active',
    createdAt: '2026-03-09',
    updatedAt: '2026-09-12',
    variants: [
      { id: 'var-1006-1', name: 'Standard', code: 'HST-STD', price: 4999, status: 'Active' },
      { id: 'var-1006-2', name: 'Pro', code: 'HST-PRO', price: 8999, status: 'Active' },
    ],
  },
  {
    id: 'prod-1007',
    code: 'PRD-SFT-007',
    name: 'LedgerCloud Accounting',
    category: 'Software',
    description:
      'Cloud accounting workspace for GST-ready invoicing, expense tracking and monthly close.',
    basePrice: 7499,
    currency: 'INR',
    status: 'Active',
    createdAt: '2026-04-21',
    updatedAt: '2026-08-19',
    variants: [],
  },
  {
    id: 'prod-1008',
    code: 'PRD-CLD-008',
    name: 'EdgeCDN Global',
    category: 'Cloud Services',
    description:
      'Global content delivery network with edge caching, image optimisation and traffic analytics.',
    basePrice: 89,
    currency: 'SGD',
    status: 'Active',
    createdAt: '2026-05-06',
    updatedAt: '2026-09-23',
    variants: [
      { id: 'var-1008-1', name: '100GB', code: 'CDN-100', price: 89, status: 'Active' },
      { id: 'var-1008-2', name: '1TB', code: 'CDN-1TB', price: 249, status: 'Active' },
    ],
  },
  {
    id: 'prod-1009',
    code: 'PRD-ANL-009',
    name: 'RetailIQ Forecasting',
    category: 'Analytics',
    description:
      'Demand forecasting and inventory insights for multi-store retail and e-commerce teams.',
    basePrice: 249,
    currency: 'SGD',
    status: 'Inactive',
    createdAt: '2025-10-30',
    updatedAt: '2026-02-11',
    variants: [],
  },
  {
    id: 'prod-1010',
    code: 'PRD-CRM-010',
    name: 'SupportDesk Pro',
    category: 'CRM',
    description:
      'Customer support workspace with shared inboxes, SLA tracking and knowledge base tools.',
    basePrice: 129,
    currency: 'GBP',
    status: 'Active',
    createdAt: '2026-06-18',
    updatedAt: '2026-09-28',
    variants: [
      { id: 'var-1010-1', name: 'Agent', code: 'SUP-AGT', price: 129, status: 'Active' },
      { id: 'var-1010-2', name: 'Team', code: 'SUP-TEM', price: 299, status: 'Active' },
    ],
  },
  {
    id: 'prod-1011',
    code: 'PRD-SEC-011',
    name: 'ThreatWatch SOC',
    category: 'Security',
    description:
      'Managed security monitoring with 24x7 alert triage, incident reports and compliance reviews.',
    basePrice: 899,
    currency: 'AED',
    status: 'Active',
    createdAt: '2026-07-02',
    updatedAt: '2026-09-30',
    variants: [],
  },
  {
    id: 'prod-1012',
    code: 'PRD-HST-012',
    name: 'CommerceHost India',
    category: 'Hosting',
    description:
      'India-focused commerce hosting with payment gateway readiness, CDN and uptime monitoring.',
    basePrice: 2999,
    currency: 'INR',
    status: 'Inactive',
    createdAt: '2025-08-15',
    updatedAt: '2026-01-27',
    variants: [
      { id: 'var-1012-1', name: 'Lite', code: 'COM-LIT', price: 2999, status: 'Inactive' },
      { id: 'var-1012-2', name: 'Business', code: 'COM-BIZ', price: 5999, status: 'Inactive' },
    ],
  },
]
