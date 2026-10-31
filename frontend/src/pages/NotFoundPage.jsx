import { Link } from 'react-router-dom'
import { ROUTES } from '../routes/paths.js'

export default function NotFoundPage() {
  return (
    <section className="mx-auto flex w-full max-w-2xl flex-col items-center px-4 py-20 text-center sm:px-6">
      <p className="text-sm font-semibold uppercase tracking-widest text-brand">404</p>
      <h1 className="mt-3 text-3xl font-bold tracking-tight text-foreground">Page not found</h1>
      <p className="mt-2 text-sm text-muted">
        The page you are looking for does not exist in the SubscriptIQ frontend.
      </p>
      <Link
        to={ROUTES.home}
        className="mt-6 inline-flex items-center rounded-lg bg-brand px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-dark"
      >
        Return home
      </Link>
    </section>
  )
}
