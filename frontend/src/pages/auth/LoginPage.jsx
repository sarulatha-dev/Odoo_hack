import { ArrowLeft, LockKeyhole } from 'lucide-react'
import { Link } from 'react-router-dom'
import { ROUTES } from '../../routes/paths.js'

export default function LoginPage() {
  return (
    <section className="mx-auto flex w-full max-w-md flex-col px-4 py-12 sm:px-6 lg:py-20">
      <Link
        to={ROUTES.home}
        className="inline-flex items-center gap-2 text-sm font-medium text-muted transition-colors hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" aria-hidden="true" />
        Back to overview
      </Link>

      <div className="mt-6 rounded-2xl border border-border bg-card p-6 shadow-sm sm:p-8">
        <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand/10 text-brand">
          <LockKeyhole className="h-5 w-5" aria-hidden="true" />
        </span>
        <h1 className="mt-5 text-2xl font-bold tracking-tight text-foreground">Sign in to SubscriptIQ</h1>
        <p className="mt-2 text-sm leading-6 text-muted">
          Authentication is not connected yet. The Spring Boot API will provide the login flow in a later step.
        </p>
        <div className="mt-6 rounded-xl border border-dashed border-border bg-background p-4 text-sm text-muted">
          Login form placeholder
        </div>
      </div>
    </section>
  )
}
