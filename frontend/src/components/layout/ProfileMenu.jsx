import { useEffect, useRef, useState } from 'react'
import { ChevronDown, LogOut, UserRound } from 'lucide-react'
import toast from 'react-hot-toast'
import { Link } from 'react-router-dom'
import { ROUTES } from '../../routes/paths.js'

export default function ProfileMenu() {
  const [isOpen, setIsOpen] = useState(false)
  const menuRef = useRef(null)
  const triggerRef = useRef(null)

  useEffect(() => {
    if (!isOpen) {
      return undefined
    }

    menuRef.current?.querySelector('[role="menuitem"]')?.focus()

    function handlePointerDown(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsOpen(false)
      }
    }

    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        setIsOpen(false)
        triggerRef.current?.focus()
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpen])

  function handleMenuKeyDown(event) {
    if (!isOpen || (event.key !== 'ArrowDown' && event.key !== 'ArrowUp')) {
      return
    }

    const items = Array.from(menuRef.current?.querySelectorAll('[role="menuitem"]') ?? [])
    if (items.length === 0) {
      return
    }

    event.preventDefault()
    const currentIndex = items.indexOf(document.activeElement)
    const direction = event.key === 'ArrowDown' ? 1 : -1
    let nextIndex = 0

    if (currentIndex === -1) {
      nextIndex = direction === 1 ? 0 : items.length - 1
    } else {
      nextIndex = (currentIndex + direction + items.length) % items.length
    }

    items[nextIndex]?.focus()
  }

  return (
    <div className="relative" ref={menuRef} onKeyDown={handleMenuKeyDown}>
      <button
        ref={triggerRef}
        type="button"
        onClick={() => setIsOpen((open) => !open)}
        aria-haspopup="menu"
        aria-expanded={isOpen}
        aria-controls="admin-profile-menu"
        aria-label={isOpen ? 'Close admin profile menu' : 'Open admin profile menu'}
        className="flex h-10 items-center gap-2 rounded-lg border border-border bg-card pl-1 pr-1.5 text-sm transition-colors hover:border-brand/40 2xl:pr-2"
      >
        <span className="flex h-8 w-8 items-center justify-center rounded-md bg-brand text-[11px] font-bold text-white">
          AD
        </span>
        <span className="hidden text-left 2xl:block">
          <span className="block text-[13px] font-semibold leading-4 text-foreground">
            Admin User
          </span>
          <span className="block text-[11px] leading-4 text-muted">Administrator</span>
        </span>
        <ChevronDown
          className={`hidden h-4 w-4 text-muted transition-transform 2xl:block ${
            isOpen ? 'rotate-180' : ''
          }`}
          aria-hidden="true"
        />
      </button>

      {isOpen && (
        <div
          id="admin-profile-menu"
          role="menu"
          className="absolute right-0 top-12 z-50 w-60 overflow-hidden rounded-xl border border-border bg-card shadow-xl"
        >
          <div className="border-b border-border px-4 py-3">
            <p className="text-sm font-semibold text-foreground">Admin User</p>
            <p className="mt-0.5 text-xs text-muted">admin@subscriptiq.app</p>
          </div>
          <div className="p-1.5">
            <button
              type="button"
              role="menuitem"
              onClick={() => {
                setIsOpen(false)
                triggerRef.current?.focus()
                toast('Profile settings are a demo placeholder.')
              }}
              className="flex w-full items-center justify-between gap-2 rounded-lg px-3 py-2 text-sm text-muted transition-colors hover:bg-foreground/5 hover:text-foreground"
            >
              <span className="flex items-center gap-2.5">
                <UserRound className="h-4 w-4" aria-hidden="true" />
                Profile settings
              </span>
              <span className="text-[10px] font-semibold uppercase tracking-wider text-muted">
                Soon
              </span>
            </button>
            <Link
              to={ROUTES.login}
              role="menuitem"
              onClick={() => setIsOpen(false)}
              className="flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm text-muted transition-colors hover:bg-foreground/5 hover:text-foreground"
            >
              <LogOut className="h-4 w-4" aria-hidden="true" />
              Sign out
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}
