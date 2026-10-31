import { useEffect, useState } from 'react'
import { Layers, Menu, X } from 'lucide-react'
import { Link, NavLink, useLocation } from 'react-router-dom'
import { mainNavigation } from '../../data/navigation.js'
import { ROUTES } from '../../routes/paths.js'
import { APP_NAME } from '../../utils/constants.js'
import ThemeToggle from '../common/ThemeToggle.jsx'
import NotificationButton from './NotificationButton.jsx'
import ProfileMenu from './ProfileMenu.jsx'
import SearchButton from './SearchButton.jsx'

const brandBase = APP_NAME.slice(0, -2)
const brandAccent = APP_NAME.slice(-2)

function navLinkClassName({ isActive }) {
  return [
    'relative whitespace-nowrap rounded-lg px-2.5 py-2 text-[13px] font-medium transition-colors after:absolute after:inset-x-2 after:bottom-1 after:h-0.5 after:rounded-full after:bg-brand after:transition-opacity',
    isActive
      ? 'bg-brand/5 font-semibold text-brand after:opacity-100'
      : 'text-muted after:opacity-0 hover:bg-foreground/5 hover:text-foreground',
  ].join(' ')
}

export default function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const location = useLocation()

  useEffect(() => {
    setIsMenuOpen(false)
  }, [location.pathname])

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-background/85 backdrop-blur">
      <div className="mx-auto flex h-[72px] w-full max-w-[1400px] items-center gap-4 px-4 sm:px-6 lg:px-8">
        <Link to={ROUTES.home} className="flex shrink-0 items-center gap-2 sm:gap-2.5">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand text-white shadow-sm shadow-brand/20 sm:h-10 sm:w-10">
            <Layers className="h-5 w-5" aria-hidden="true" />
          </span>
          <span className="text-base font-semibold tracking-tight text-foreground sm:text-[17px]">
            {brandBase}
            <span className="text-brand">{brandAccent}</span>
          </span>
        </Link>

        <nav
          className="ml-3 hidden flex-1 items-center gap-0.5 xl:flex 2xl:gap-1"
          aria-label="Main navigation"
        >
          {mainNavigation.map((item) => (
            <NavLink key={item.to} to={item.to} className={navLinkClassName}>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="ml-auto flex items-center gap-1.5 xl:ml-0 2xl:gap-2">
          <div className="hidden sm:block">
            <SearchButton />
          </div>
          <div className="hidden sm:block">
            <NotificationButton />
          </div>
          <ThemeToggle />
          <ProfileMenu />
          <button
            type="button"
            onClick={() => setIsMenuOpen((open) => !open)}
            aria-label={isMenuOpen ? 'Close navigation menu' : 'Open navigation menu'}
            aria-expanded={isMenuOpen}
            className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-border bg-card text-muted transition-colors hover:text-foreground xl:hidden"
          >
            {isMenuOpen ? (
              <X className="h-4 w-4" aria-hidden="true" />
            ) : (
              <Menu className="h-4 w-4" aria-hidden="true" />
            )}
          </button>
        </div>
      </div>

      {isMenuOpen && (
        <div className="border-t border-border bg-card xl:hidden">
          <nav
            className="mx-auto grid w-full max-w-[1400px] gap-1.5 px-4 py-4 sm:grid-cols-2 sm:px-6 lg:px-8"
            aria-label="Mobile navigation"
          >
            {mainNavigation.map((item) => (
              <NavLink key={item.to} to={item.to} className={navLinkClassName}>
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      )}
    </header>
  )
}
