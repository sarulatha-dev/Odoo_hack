export default function PagePlaceholder({ title, description }) {
  return (
    <section className="mx-auto w-full max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="rounded-2xl border border-border bg-card p-6 shadow-sm sm:p-8">
        <span className="inline-flex items-center rounded-full bg-brand/10 px-3 py-1 text-xs font-semibold uppercase tracking-widest text-brand">
          Placeholder
        </span>
        <h1 className="mt-4 text-2xl font-bold tracking-tight text-foreground sm:text-3xl">{title}</h1>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-muted">{description}</p>
        <div className="mt-6 rounded-xl border border-dashed border-border bg-background p-5">
          <p className="text-sm text-muted">
            This module is part of the SubscriptIQ frontend foundation. Feature implementation will follow in a later step.
          </p>
        </div>
      </div>
    </section>
  )
}
