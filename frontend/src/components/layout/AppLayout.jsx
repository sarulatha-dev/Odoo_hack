import { Outlet } from 'react-router-dom'
import { APP_NAME, APP_TAGLINE } from '../../utils/constants.js'
import Navbar from './Navbar.jsx'

export default function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-background text-foreground">
      <Navbar />
      <main className="flex-1">
        <Outlet />
      </main>
      <footer className="border-t border-border">
        <div className="mx-auto flex w-full max-w-[1400px] flex-col gap-1 px-4 py-6 text-sm text-muted sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
          <p>
            &copy; {new Date().getFullYear()} {APP_NAME}. All rights reserved.
          </p>
          <p>{APP_TAGLINE}</p>
        </div>
      </footer>
    </div>
  )
}
