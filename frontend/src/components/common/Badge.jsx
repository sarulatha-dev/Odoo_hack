const toneStyles = {
  brand: 'bg-brand/10 text-brand ring-brand/20',
  neutral: 'bg-slate-500/10 text-slate-600 ring-slate-500/20 dark:text-slate-300',
  success: 'bg-emerald-500/10 text-emerald-700 ring-emerald-500/20 dark:text-emerald-300',
  warning: 'bg-amber-500/10 text-amber-700 ring-amber-500/20 dark:text-amber-300',
  danger: 'bg-rose-500/10 text-rose-700 ring-rose-500/20 dark:text-rose-300',
  info: 'bg-sky-500/10 text-sky-700 ring-sky-500/20 dark:text-sky-300',
}

export default function Badge({ tone = 'neutral', dot = false, children, className = '' }) {
  return (
    <span
      className={[
        'inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-[13px] font-semibold leading-5 ring-1 ring-inset',
        toneStyles[tone] ?? toneStyles.neutral,
        className,
      ].join(' ')}
    >
      {dot && <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden="true" />}
      {children}
    </span>
  )
}
