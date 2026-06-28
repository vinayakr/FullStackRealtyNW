import { createContext, useContext, useState, type ReactNode } from 'react'
import ContactModal from '../components/ContactModal'

const ContactModalContext = createContext<() => void>(() => {})

export function useContactModal() {
  return useContext(ContactModalContext)
}

export function ContactModalProvider({ children }: { children: ReactNode }) {
  const [open, setOpen] = useState(false)

  return (
    <ContactModalContext.Provider value={() => setOpen(true)}>
      {children}
      {open && <ContactModal onClose={() => setOpen(false)} />}
    </ContactModalContext.Provider>
  )
}
