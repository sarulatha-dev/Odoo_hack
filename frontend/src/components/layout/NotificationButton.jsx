import { Bell } from 'lucide-react'
import toast from 'react-hot-toast'

export default function NotificationButton() {
  function handleClick() {
    toast('Notifications are a demo placeholder and are not connected.')
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      title="Notifications placeholder"
      aria-label="Notifications placeholder, not implemented"
      className="relative inline-flex h-10 w-10 items-center justify-center rounded-lg border border-border bg-card text-muted transition-colors hover:border-brand/40 hover:text-foreground"
    >
      <Bell className="h-[18px] w-[18px]" aria-hidden="true" />
      <span
        className="absolute right-2 top-2 h-2 w-2 rounded-full bg-rose-500 ring-2 ring-card"
        aria-hidden="true"
      />
    </button>
  )
}
