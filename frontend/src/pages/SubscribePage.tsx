import { useState } from 'react'
import { CheckCircle, Mail } from 'lucide-react'
import { marketingApi } from '../api/client'

const INTERESTS = [
  { value: 'buying', label: 'Buying a Home' },
  { value: 'selling', label: 'Selling a Home' },
  { value: 'investing', label: 'Real Estate Investing' },
  { value: 'market-updates', label: 'Market Updates & Trends' },
]

export default function SubscribePage() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [interests, setInterests] = useState<string[]>([])
  const [status, setStatus] = useState<'idle' | 'submitting' | 'success' | 'already' | 'error'>('idle')

  function toggleInterest(value: string) {
    setInterests((prev) =>
      prev.includes(value) ? prev.filter((i) => i !== value) : [...prev, value]
    )
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setStatus('submitting')
    try {
      await marketingApi.optIn({ email, name: name || undefined, interests })
      setStatus('success')
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status
      if (status === 409) {
        setStatus('already')
      } else {
        setStatus('error')
      }
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-16 px-4">
      <div className="w-full max-w-lg">
        {status === 'success' || status === 'already' ? (
          <div className="bg-white rounded-2xl shadow-md p-10 text-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-5">
              <CheckCircle className="w-8 h-8 text-green-600" />
            </div>
            <h1 className="font-serif text-2xl font-bold text-navy-900 mb-2">
              {status === 'already' ? "You're already on the list!" : "You're subscribed!"}
            </h1>
            <p className="text-gray-500 text-sm">
              {status === 'already'
                ? 'Your email is already signed up. Look out for updates from Vinny.'
                : "Thanks for subscribing. You'll hear from Vinny with PNW real estate insights, market updates, and more."}
            </p>
          </div>
        ) : (
          <div className="bg-white rounded-2xl shadow-md p-8 sm:p-10">
            <div className="mb-7">
              <div className="w-10 h-10 bg-gold-500 rounded-lg flex items-center justify-center mb-4">
                <Mail className="w-5 h-5 text-white" />
              </div>
              <h1 className="font-serif text-2xl sm:text-3xl font-bold text-navy-900 mb-2">
                Stay Ahead of the Market
              </h1>
              <p className="text-gray-500 text-sm leading-relaxed">
                Get Pacific Northwest real estate insights, market updates, and investor tips delivered
                straight to your inbox — no spam, unsubscribe any time.
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Name <span className="text-gray-400 font-normal">(optional)</span>
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-transparent"
                  placeholder="Your name"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Email <span className="text-red-400">*</span>
                </label>
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
                <p className="block text-sm font-medium text-gray-700 mb-2">
                  I'm interested in <span className="text-gray-400 font-normal">(select all that apply)</span>
                </p>
                <div className="grid grid-cols-2 gap-2">
                  {INTERESTS.map(({ value, label }) => {
                    const checked = interests.includes(value)
                    return (
                      <button
                        key={value}
                        type="button"
                        onClick={() => toggleInterest(value)}
                        className={`text-left text-sm px-3 py-2.5 rounded-lg border transition-colors ${
                          checked
                            ? 'border-navy-600 bg-navy-50 text-navy-800 font-medium'
                            : 'border-gray-200 text-gray-600 hover:border-gray-300 hover:bg-gray-50'
                        }`}
                      >
                        {checked && <span className="mr-1">✓</span>}
                        {label}
                      </button>
                    )
                  })}
                </div>
              </div>

              {status === 'error' && (
                <p className="text-red-500 text-sm">Something went wrong — please try again.</p>
              )}

              <button
                type="submit"
                disabled={status === 'submitting'}
                className="btn-primary w-full justify-center"
              >
                {status === 'submitting' ? 'Subscribing…' : 'Subscribe'}
              </button>

              <p className="text-xs text-gray-400 text-center">
                By subscribing you agree to receive marketing emails from Full Stack Realty NW.
                You can unsubscribe at any time.
              </p>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}
