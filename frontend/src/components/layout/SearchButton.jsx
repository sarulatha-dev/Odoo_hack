import { Search } from 'lucide-react'
import toast from 'react-hot-toast'

export default function SearchButton() {
  function handleClick() {
    toast('Search is a demo placeholder and does not query data.')
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      title="Search placeholder"
      aria-label="Search placeholder, not implemented"
      className="inline-flex h-10 items-center gap-2 rounded-lg border border-border bg-card px-2.5 text-sm text-muted transition-colors hover:border-brand/40 hover:text-foreground 2xl:px-3"
    >
      <Search className="h-[18px] w-[18px]" aria-hidden="true" />
      <span className="hidden 2xl:inline">Search</span>
    </button>
  )
}
