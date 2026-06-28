import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Menu, X, Home } from 'lucide-react'
import { useContactModal } from '../context/ContactModalContext'

export default function Navbar() {
  const [open, setOpen] = useState(false)
  const { pathname } = useLocation()
  const openContact = useContactModal()

  const links = [
    { to: '/', label: 'Home' },
    { to: '/chat', label: 'Find My Home' },
    { to: '/articles', label: 'Articles' },
  ]

  const isActive = (to: string) => (to === '/' ? pathname === '/' : pathname.startsWith(to))

  return (
    <header className="sticky top-0 z-50 bg-navy-900 shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 group">
            <div className="w-8 h-8 bg-gold-500 rounded-lg flex items-center justify-center">
              <Home className="w-5 h-5 text-white" />
            </div>
            <div>
              <span className="text-white font-serif font-bold text-lg leading-none block">
                Full Stack Realty
              </span>
              <span className="text-gold-400 text-xs font-medium tracking-widest uppercase">
                Northwest
              </span>
            </div>
          </Link>

          {/* Desktop nav */}
          <nav className="hidden md:flex items-center gap-1">
            {links.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                  isActive(link.to)
                    ? 'text-gold-400 bg-navy-800'
                    : 'text-gray-300 hover:text-white hover:bg-navy-800'
                }`}
              >
                {link.label}
              </Link>
            ))}
            <button
              onClick={openContact}
              className="ml-3 btn-primary text-sm py-2"
            >
              Contact Vinny
            </button>
          </nav>

          {/* Mobile hamburger */}
          <button
            onClick={() => setOpen(!open)}
            className="md:hidden text-gray-300 hover:text-white p-2"
            aria-label="Toggle menu"
          >
            {open ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {open && (
        <div className="md:hidden border-t border-navy-700 bg-navy-900">
          <div className="px-4 py-3 space-y-1">
            {links.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setOpen(false)}
                className={`block px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  isActive(link.to)
                    ? 'text-gold-400 bg-navy-800'
                    : 'text-gray-300 hover:text-white hover:bg-navy-800'
                }`}
              >
                {link.label}
              </Link>
            ))}
            <button
              onClick={() => { openContact(); setOpen(false) }}
              className="block mt-2 btn-primary text-sm text-center w-full"
            >
              Contact Vinny
            </button>
          </div>
        </div>
      )}
    </header>
  )
}
