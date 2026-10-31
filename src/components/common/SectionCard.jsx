export default function SectionCard({
  title,
  description,
  actions,
  children,
  className = '',
  contentClassName = '',
}) {
  return (
    <section className={`rounded-2xl border border-border bg-card shadow-sm ${className}`}>
      <div className="flex flex-col gap-3 border-b border-border px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6 sm:py-5">
        <div>
          <h2 className="text-base font-semibold tracking-tight text-foreground sm:text-lg">{title}</h2>
          {description && <p className="mt-1 text-sm text-muted">{description}</p>}
        </div>
        {actions}
      </div>
      <div className={`px-5 py-5 sm:px-6 sm:py-6 ${contentClassName}`}>{children}</div>
    </section>
  )
}
