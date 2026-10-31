# Brightloop Subscription System — Build Checklist & Log

> Source of truth for implementation status. Updated after every work session.
> PRD: [PRD Subscription Management System (Hackathon Prototype).md](./PRD%20Subscription%20Management%20System%20(Hackathon%20Prototype).md)
> Methodology: Agile, thin vertical slices. One module at a time. Compile + verify before moving on.

## Legend

- [ ] Not started
- [~] In progress
- [x] Done and verified (build + smoke test)

---

## Phase 0 — Project Foundation

- [x] Read and analyse PRD
- [x] Scaffold Spring Boot project (Java 21, Maven, Boot 4.1.2)
- [x] Add dependencies: Web, JPA, Security, Validation, Mail, Actuator, Lombok, H2, PostgreSQL, JWT, Apache POI
- [x] Create this checklist + append-only work log
- [x] Application config (`application.yml`), profiles, base package layout
- [x] Common API error envelope + global exception handler
- [x] `mvn compile` passes

## Phase 1 — Auth & Roles (Must #1)

- [x] `User` entity, `Role`, Indian state enum, repository
- [x] Password rule validation (>8, upper, lower, special) on server
- [x] Register (customer self-signup), duplicate email rejection
- [x] Login -> JWT issuance
- [x] JWT authentication filter + stateless security config
- [x] Role-based access (`ADMIN`, `STAFF`, `CUSTOMER`)
- [x] Admin-only staff creation
- [x] Forgot/reset password with one-time token (30 min expiry) + mail abstraction
- [x] Auth smoke test

## Phase 2 — Catalog: Products, Variants, Plans (Must #2, #3)

- [x] Product CRUD + variants (extra price)
- [x] Plan CRUD + billing period, min qty, validity, 4 switches, tier, features
- [x] Tax CRUD (percent, gst_type, HSN/SAC)
- [x] Discount CRUD (fixed/percent, min amount/qty, dates, usage limit, scope)
- [x] Admin/Staff authorization rules on catalog writes
- [x] Catalog smoke test

## Phase 3 — Quotation Templates & Subscriptions (Must #4, #5)

- [x] Quotation template CRUD + lines
- [x] Create subscription from template (copy lines, quote validity)
- [x] Subscription numbering `SUB-0001`
- [x] Subscription lines (product, variant, qty, unit price, tax, amount)
- [x] Lifecycle transitions Draft -> Quotation -> Confirmed -> Active -> Closed
- [x] Pause/renew/close guarded by plan switches
- [x] Subscription smoke test

## Phase 4 — Invoicing, GST, Billing Job (Must #6, #7; #4.4a)

- [x] Invoice + invoice line entities, numbering `INV-0001`
- [x] Invoice generation from subscription (tax + discounts applied)
- [x] GST split: CGST+SGST same state, IGST other state, export = no GST
- [x] Freeze currency + exchange rate + total in base on invoice
- [x] Idempotent billing job (`@Scheduled` + manual run) with unique `(subscription_id, billing_period_start)`
- [x] Loop to catch missed billing periods
- [x] Invoice actions: confirm, cancel, send, print payload
- [x] Billing smoke test

## Phase 5 — Payments & Gateway (Must #9)

- [x] Payment entity + manual recording (method, amount, date, note)
- [x] Partial payments, auto Paid when sum >= total
- [x] Overdue logic (due date = invoice date + payment terms)
- [x] Razorpay test-mode gateway abstraction (create order + verify signature)
- [x] Payment smoke test

## Phase 6 — Finance Dashboard (Must #10)

- [x] Dashboard cards: active subs, revenue, payments, outstanding, overdue
- [x] Overdue invoice table with days overdue + send reminder
- [x] Server-side filters (date range, customer, plan, status, currency) + pagination
- [x] DB indexes on status, next_billing_date, customer_id, due_date

## Phase 7 — Should-Have Extras (in PRD priority order)

- [x] Multi-currency: rates table, admin edit, invoice freeze, base-currency reporting
- [x] Autopay: toggle + saved method, charge on billing, retry_count
- [x] Notifications: table, bell/unread, event hooks, email for key events
- [x] Membership metrics + middle-tier upsell (plan_views analytics)
- [x] Excel export (invoices, payments, subscriptions, profit, GST sheet)
- [x] Customer expense tracker
- [x] Profit / loss tracker (revenue ex-GST, cost, margin, MRR, leaderboards)

## Phase 8 — Frontend (React + Vite + Tailwind)

- [ ] Scaffold frontend and API client
- [ ] Auth screens + route guards
- [ ] Catalog admin screens
- [ ] Subscription lifecycle screens
- [ ] Invoice + payment screens (Razorpay checkout)
- [ ] Dashboard, metrics, profit, expense tracker, pricing page
- [ ] Print view

## Phase 9 — Seed, Polish, Demo

- [x] Seed: users (admin/staff/customer), 3 tiers of plans, products/variants, taxes, discounts
- [x] Seed 2,000+ fake subscriptions/invoices
- [x] Demo script walkthrough (PRD section 8) verified end-to-end
- [x] Performance check (<2s list pages)

---

## Work Log

> Append-only. Newest entries at the bottom. Format: `YYYY-MM-DD — what happened / decisions / blockers / next`.

- **2026-10-31** — Read PRD. Scaffolded Spring Boot 4.1.2 (Java 21) from Spring Initializr into repo root. Added JWT (jjwt 0.12.6) and Apache POI 5.4.1. Created this checklist. Next: config + common error handling, then Auth module.
- **2026-10-31** — Phase 0 done: `application.yml` (H2 file DB, JPA, mail, Razorpay keys, app props), `AppProperties`, `ApiException`, `ApiError`, `PageResponse`, `GlobalExceptionHandler`. `mvn compile` green. Next: Auth module.
- **2026-10-31** — Phase 1 done. `User`/`Role`/`IndianState`, JWT service + filter, stateless `SecurityConfig` with method security + JSON 401/403, `AuthService`/`AuthController` (register, login, forgot/reset), admin-only staff creation, `MailService` (logs when SMTP disabled). Fixed H2 URL incompatibility and Boot 4 Jackson property binding. Live smoke test passed: weak password 400, valid signup, duplicate 409, login/me OK, bad login 401, forgot->reset->login OK. Next: Catalog.
- **2026-10-31** — Phase 2 + 3 done. Catalog modules (products+variants, plans with tiers/switches/features, taxes with GST type/HSN, discounts with scope/rules). Added `SequenceService` for atomic document numbers. Quotation templates + subscription module with `SUB-0001` numbering, line snapshots, template copy, lifecycle actions (quote/confirm/activate/close/pause/resume/renew) guarded by plan switches, ownership checks, pagination. Added `BootstrapData` dev accounts (admin/staff/customer). Fixed reserved word `value` in discounts DDL. Live smoke test passed: admin created product+variant(+560), tax 18%, MEDIUM plan, 10% discount; staff created template and subscription; Draft->Quotation->Confirmed->Active; staff blocked from discount create (403); customer sees only own subscription. Next: invoicing/GST/billing job.
- **2026-10-31** — Phase 4 done. Currency rate module (INR base, seeded USD/EUR/GBP/AED/SGD, admin edit). Invoice + invoice lines with frozen currency/exchange rate/total-in-base. GST calculator: same state -> CGST+SGST split, other state -> IGST, foreign currency -> export (no GST), place of supply + GSTINs stored. Discount engine evaluates date/usage/min rules and allocates discounts across lines. Idempotent `@Scheduled` billing job + manual `POST /api/billing/run`, loops missed periods, advances `next_billing_date`, auto-closes at expiry. Invoice list/filters/pagination, detail, print payload, confirm/cancel/send. Live test: Karnataka invoice 1404 net + 126.36 CGST + 126.36 SGST = 1656.72; second run created 0 (idempotent); Tamil Nadu invoice IGST 252.72; USD invoice no GST, total 16.92 USD = 1404.36 INR at frozen 83. Next: payments/gateway.
- **2026-10-31** — Phase 5 done. Payment entity (method/amount/date/note/gateway ref) + staff-only manual recording, customer payment history, partial payments and automatic `PAID` when settled, overpayment/replay guards. Invoice responses now expose `paidAmount`, `outstanding`, `overdue`. Razorpay abstraction creates real orders when keys are set, otherwise runs an explicit simulated mode; HMAC-SHA256 signature verification, idempotent by gateway payment id. Live test on INV-0001 (1656.72): 500 cash -> outstanding 1156.72; overpay blocked 400; customer manual pay blocked 403; gateway order for outstanding 1156.72 (simulated), verified -> PAID; replay blocked 400. Overdue flag wired; future-due invoice returns false (past-due branch not time-traveled). Next: finance dashboard.
- **2026-10-31** — Phase 6 done. Admin/staff dashboard: cards for active/total subscriptions, revenue ex-GST, payments received, outstanding (count + amount), overdue (count + amount), all reported in INR base with display-currency conversion (`displayCurrency`). Overdue page with days overdue, plan/customer, outstanding in invoice + base + display currency, and "send reminder" mail hook. Server-side filters (date range, customer, plan, status, invoice currency) + pagination. Added indexes on `invoices.issue_date`, `payments.invoice_id/paid_on`, `subscriptions.plan_id` (status/next_billing_date/customer_id/due_date already present). Live test: active 3/3, revenue 4212.36 INR, payments 1656.72, outstanding 3061.08 (2 invoices), USD switcher 50.75/19.96/36.88; customer blocked 403. Set INV-0002 due date 5 days back via H2 shell -> overdue count 1, amount 1656.72, days 5, reminder logged. Next: extras in PRD priority order (multi-currency reporting, autopay, notifications, upsell metrics, Excel, expense, profit).
- **2026-10-31** — Phase 7 done. Multi-currency: customer `preferredCurrency` drives display conversion for dashboard, invoices, expenses and profit; invoice currency/rate frozen at issue; rates editable by admin. Autopay: per-customer toggle + saved method, billing job charges saved method (simulated when gateway keys blank) and records retry attempts. Notifications: persisted per-user notifications with unread/bell endpoints, event hooks on invoice sent/paid/overdue, payment, subscription lifecycle and reminders; email hook via `MailService`. Metrics/upsell: plan churn + underperforming flag (`app.metrics.underperforming-churn-threshold`), pricing views with upgrade/nudge messaging and `plan_views` analytics. Exports: invoices (incl. GST Report), payments, subscriptions and profit (Summary/Subscriptions/Plans/Products/Monthly). Expense tracker: `GET /api/expenses` (total + current-month spend, monthly/subscription/currency breakdowns, upcoming renewals, budget warning) + `PUT /api/expenses/budget`; budget is in the customer's preferred currency. Profit tracker: `GET /api/profit?from&to&planId` revenue ex-GST on paid invoices, cost from product cost price x qty, margin, MRR (daily x30/weekly x4.33/monthly/yearly /12), outstanding, GST collected, subscription/plan/product/month rollups and top-5/bottom-5 leaderboards. Live tests: expense summary USD 76.80 total, 2 renewals, budget 10 -> warning true; profit revenue 5616.36, cost 1600.00, gross 4016.36 (71.51%), MRR 6240.00, outstanding 3061.08, GST 1010.88, export 5 sheets; customer gets 403 on profit. Next: Phase 8 frontend.
- **2026-10-31** — Phase 9 done. Seeded catalog plus 2,000 subscriptions / 10,000+ invoices; bulk seeder uses a block sequence cursor to avoid per-document auto-flush. Ran the PRD §8 demo script end-to-end: weak/valid/duplicate signup, catalog creation, staff template + Draft-to-Active lifecycle, idempotent billing, USD + simulated Razorpay payment + notification, autopay charged on the next billing run, pricing tiers, dashboard filters, metrics/profit leaderboards, Excel exports, GST CGST/SGST vs IGST, and print HSN/GSTIN checks. Server response times: subscriptions 77ms, invoices 99ms, overdue 119ms, dashboard 122ms, profit 1.87s. Full 10k-invoice export 6.2s (streaming workbook), payments 2.0s. Optimized profit with fetch joins/aggregates and exports with fetch joins + streaming. Repo split into `backend/` and `frontend/`; backend recompiled and live smoke test passed after the move (2,008 subscriptions / 10,125 invoices). Phase 8 frontend remains unverified.

