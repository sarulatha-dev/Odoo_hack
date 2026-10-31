import { ArrowRight, CheckCircle2, Layers, ShieldCheck } from 'lucide-react'
import { Link } from 'react-router-dom'
import { ROUTES } from '../routes/paths.js'

const setupChecks = [
  'Vite and React foundation',
  'Tailwind CSS v4 with the official Vite plugin',
  'React Router with placeholder routes',
  'Light and dark theme support',
]

const primaryButtonClassName =
  'inline-flex items-center gap-2 rounded-lg bg-brand px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-dark'

const secondaryButtonClassName =
  'inline-flex items-center gap-2 rounded-lg border border-border bg-card px-4 py-2.5 text-sm font-semibold text-foreground transition-colors hover:border-brand/40 hover:text-brand'

export default function HomePage() {
  return (
    <section className="mx-auto flex w-full max-w-5xl flex-col gap-10 px-4 py-12 sm:px-6 lg:px-8 lg:py-20">
      <div className="max-w-3xl">
        <span className="inline-flex items-center gap-2 rounded-full border border-brand/20 bg-brand/10 px-3 py-1 text-xs font-semibold uppercase tracking-widest text-brand">
          <Layers className="h-3.5 w-3.5" aria-hidden="true" />
          Frontend foundation
        </span>
        <h1 className="mt-5 text-4xl font-bold tracking-tight text-foreground sm:text-5xl">
          SubscriptIQ Frontend Setup Successful
        </h1>
        <p className="mt-4 text-base leading-7 text-muted sm:text-lg">
          The frontend foundation for SubscriptIQ is ready. Routing, theming, and the shared layout are in place so
          feature modules can be added next.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link to={ROUTES.login} className={primaryButtonClassName}>
            Open sign in
            <ArrowRight className="h-4 w-4" aria-hidden="true" />
          </Link>
          <Link to={ROUTES.admin.dashboard} className={secondaryButtonClassName}>
            View dashboard
          </Link>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        {setupChecks.map((item) => (
          <div key={item} className="flex items-center gap-3 rounded-xl border border-border bg-card p-4 shadow-sm">
            <CheckCircle2 className="h-5 w-5 shrink-0 text-brand" aria-hidden="true" />
            <span className="text-sm font-medium text-foreground">{item}</span>
          </div>
        ))}
      </div>

      <div className="flex flex-col gap-4 rounded-2xl border border-border bg-card p-6 shadow-sm sm:flex-row sm:items-center">
        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand/10 text-brand">
          <ShieldCheck className="h-5 w-5" aria-hidden="true" />
        </span>
        <div>
          <h2 className="text-sm font-semibold text-foreground">Backend integration is intentionally not connected yet</h2>
          <p className="mt-1 text-sm text-muted">
            The Spring Boot API and database layer will be connected in a later step. No endpoints are assumed in this
            setup.
          </p>
        </div>
      </div>
    </section>
  )
}
