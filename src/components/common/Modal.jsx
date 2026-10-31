import { useEffect, useRef } from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'

const focusableSelector = [
  'a[href]',
  'button:not([disabled])',
  'textarea:not([disabled])',
  'input:not([disabled])',
  'select:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(', ')

export default function Modal({
  open,
  onClose,
  titleId,
  children,
  maxWidthClassName = 'max-w-2xl',
}) {
  const panelRef = useRef(null)
  const onCloseRef = useRef(onClose)

  useEffect(() => {
    onCloseRef.current = onClose
  }, [onClose])

  useEffect(() => {
    if (!open) {
      return undefined
    }

    const previouslyFocused = document.activeElement
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const frame = window.requestAnimationFrame(() => {
      const preferred = panelRef.current?.querySelector('[data-autofocus]')
      const focusable = panelRef.current?.querySelectorAll(focusableSelector)
      const target = preferred ?? focusable?.[0] ?? panelRef.current
      target?.focus()
    })

    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        event.preventDefault()
        onCloseRef.current()
        return
      }

      if (event.key !== 'Tab' || !panelRef.current) {
        return
      }

      const focusable = Array.from(panelRef.current.querySelectorAll(focusableSelector))
      if (focusable.length === 0) {
        return
      }

      const first = focusable[0]
      const last = focusable[focusable.length - 1]

      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault()
        first.focus()
      }
    }

    document.addEventListener('keydown', handleKeyDown)

    return () => {
      window.cancelAnimationFrame(frame)
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = previousOverflow

      if (previouslyFocused?.isConnected) {
        previouslyFocused.focus()
      } else {
        document.querySelector('[data-modal-fallback-focus]')?.focus()
      }
    }
  }, [open])

  if (!open) {
    return null
  }

  return createPortal(
    <div
      className="fixed inset-0 z-[60] overflow-y-auto bg-slate-950/55 backdrop-blur-sm"
      role="presentation"
    >
      <div
        className="flex min-h-full items-start justify-center p-4 sm:items-center sm:p-6"
        role="presentation"
        onMouseDown={(event) => {
          if (event.target === event.currentTarget) {
            onCloseRef.current()
          }
        }}
      >
        <div
          ref={panelRef}
          role="dialog"
          aria-modal="true"
          aria-labelledby={titleId}
          tabIndex={-1}
          className={`relative w-full ${maxWidthClassName} overflow-hidden rounded-2xl border border-border bg-card shadow-2xl`}
        >
          <button
            type="button"
            onClick={() => onCloseRef.current()}
            aria-label="Close dialog"
            title="Close"
            className="absolute right-3 top-3 z-10 inline-flex h-9 w-9 items-center justify-center rounded-lg border border-border bg-card/90 text-muted transition-colors hover:text-foreground"
          >
            <X className="h-4 w-4" aria-hidden="true" />
          </button>
          {children}
        </div>
      </div>
    </div>,
    document.body,
  )
}
