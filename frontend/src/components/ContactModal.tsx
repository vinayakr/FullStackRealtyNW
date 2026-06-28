import { useState } from 'react'
import { X, Mail, Phone, CheckCircle } from 'lucide-react'
import { contactApi } from '../api/client'

interface Props {
  onClose: () => void
}

export default function ContactModal({ onClose }: Props) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState('')
  const [showPhone, setShowPhone] = useState(false)
  const [status, setStatus] = useState<'idle' | 'sending' | 'sent' | 'error'>('idle')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setStatus('sending')
    try {
      await contactApi.submit({ name, email, message })
      setStatus('sent')
    } catch {
      setStatus('error')
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg p-8">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
          aria-label="Close"
        >
          <X className="w-5 h-5" />
        </button>

        {status === 'sent' ? (
          <div className="text-center py-6">
            <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <CheckCircle className="w-7 h-7 text-green-600" />
            </div>
            <h3 className="font-serif text-xl font-bold text-navy-900 mb-2">Message sent!</h3>
            <p className="text-gray-500 text-sm mb-6">Vinny will be in touch soon.</p>
            <button onClick={onClose} className="btn-primary">Close</button>
          </div>
        ) : (
          <>
            <h2 className="font-serif text-2xl font-bold text-navy-900 mb-1">Get in Touch</h2>
            <p className="text-gray-500 text-sm mb-6">Vinny typically responds within a few hours.</p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                <input
                  required
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-transparent"
                  placeholder="Your name"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  required
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-transparent"
                  placeholder="you@example.com"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Message</label>
                <textarea
                  required
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  rows={4}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-transparent resize-none"
                  placeholder="What can Vinny help you with?"
                />
              </div>

              {status === 'error' && (
                <p className="text-red-500 text-sm">Something went wrong — please try again.</p>
              )}

              <button
                type="submit"
                disabled={status === 'sending'}
                className="btn-primary w-full justify-center"
              >
                {status === 'sending' ? 'Sending…' : 'Send Message'}
              </button>
            </form>

            <div className="mt-6 pt-5 border-t border-gray-100 space-y-2.5">
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Mail className="w-4 h-4 flex-shrink-0" />
                <span>vinny@fullstackrealtynw.com</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Phone className="w-4 h-4 flex-shrink-0" />
                {showPhone ? (
                  <span>425-686-9156</span>
                ) : (
                  <button
                    type="button"
                    onClick={() => setShowPhone(true)}
                    className="text-navy-600 hover:text-navy-800 font-medium transition-colors"
                  >
                    Show phone number
                  </button>
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
