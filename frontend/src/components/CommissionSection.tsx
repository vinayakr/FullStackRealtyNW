import { useState } from 'react'
import { DollarSign, CheckCircle } from 'lucide-react'
import { useContactModal } from '../context/ContactModalContext'

const savings = [
  { price: 400000, traditional: 12000, ours: 8000 },
  { price: 600000, traditional: 18000, ours: 12000 },
  { price: 800000, traditional: 24000, ours: 16000 },
  { price: 1000000, traditional: 30000, ours: 20000 },
]

const perks = [
  'Professional HDR photography & video walkthrough',
  'MLS listing + Zillow, Redfin, Realtor.com syndication',
  'Targeted social media marketing campaigns',
  'Investor-grade pricing strategy & market analysis',
  'Expert offer negotiation & contract review',
  'Full transaction coordination through closing',
]

export default function CommissionSection() {
  const [homePrice, setHomePrice] = useState(650000)
  const openContact = useContactModal()

  const traditional = Math.round(homePrice * 0.03)
  const ours = Math.round(homePrice * 0.02)
  const saved = traditional - ours

  return (
    <section className="py-20 bg-navy-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-14">
          <div className="inline-block bg-gold-500/20 text-gold-300 text-sm font-semibold px-3 py-1 rounded-full mb-4">
            Transparent Pricing
          </div>
          <h2 className="font-serif text-4xl md:text-5xl font-bold mb-4">
            2% Commission.<br />
            <span className="text-gold-400">Full-Service Experience.</span>
          </h2>
          <p className="text-gray-400 text-lg max-w-2xl mx-auto">
            Traditional listing agents charge 3%. We charge 2%. On a $700,000 home, that's{' '}
            <span className="text-gold-300 font-semibold">$7,000 back in your pocket</span> — with
            zero reduction in service quality.
          </p>
        </div>

        <div className="grid lg:grid-cols-2 gap-12 items-start">
          {/* Calculator */}
          <div className="bg-navy-800 rounded-2xl p-8">
            <h3 className="font-serif text-2xl font-semibold mb-6 flex items-center gap-2">
              <DollarSign className="w-6 h-6 text-gold-400" />
              Savings Calculator
            </h3>
            <div className="mb-6">
              <label className="block text-sm text-gray-400 mb-2">
                Estimated Home Price: <span className="text-white font-semibold">${homePrice.toLocaleString()}</span>
              </label>
              <input
                type="range"
                min={200000}
                max={2000000}
                step={25000}
                value={homePrice}
                onChange={(e) => setHomePrice(Number(e.target.value))}
                className="w-full accent-gold-500 cursor-pointer"
              />
              <div className="flex justify-between text-xs text-gray-500 mt-1">
                <span>$200K</span>
                <span>$2M</span>
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 rounded-xl bg-red-900/30 border border-red-700/30">
                <div>
                  <div className="text-xs text-gray-400 mb-0.5">Traditional Agent (3%)</div>
                  <div className="text-red-300 font-semibold text-xl">${traditional.toLocaleString()}</div>
                </div>
                <div className="text-red-400 text-sm">Commission paid</div>
              </div>

              <div className="flex items-center justify-between p-4 rounded-xl bg-green-900/30 border border-green-700/30">
                <div>
                  <div className="text-xs text-gray-400 mb-0.5">Full Stack Realty NW (2%)</div>
                  <div className="text-green-300 font-semibold text-xl">${ours.toLocaleString()}</div>
                </div>
                <div className="text-green-400 text-sm">Commission paid</div>
              </div>

              <div className="flex items-center justify-between p-4 rounded-xl bg-gold-500/20 border border-gold-500/40">
                <div>
                  <div className="text-xs text-gray-400 mb-0.5">You Save</div>
                  <div className="text-gold-300 font-bold text-3xl">${saved.toLocaleString()}</div>
                </div>
                <div className="text-gold-400 text-sm font-medium">Back in your pocket</div>
              </div>
            </div>
          </div>

          {/* What's included */}
          <div>
            <h3 className="font-serif text-2xl font-semibold mb-6 text-gold-400">
              Everything's Included
            </h3>
            <p className="text-gray-400 mb-6">
              A lower commission percentage doesn't mean a smaller check of service. Here's exactly
              what you get with every listing:
            </p>
            <ul className="space-y-3">
              {perks.map((perk) => (
                <li key={perk} className="flex items-start gap-3">
                  <CheckCircle className="w-5 h-5 text-gold-400 flex-shrink-0 mt-0.5" />
                  <span className="text-gray-300">{perk}</span>
                </li>
              ))}
            </ul>
            <div className="mt-8">
              <button onClick={openContact} className="btn-primary">
                Get a Free Home Valuation
              </button>
            </div>
          </div>
        </div>

        {/* Quick table */}
        <div className="mt-14 overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-navy-700">
                <th className="text-left py-3 px-4 text-gray-400 font-medium">Home Price</th>
                <th className="text-right py-3 px-4 text-red-400 font-medium">Traditional (3%)</th>
                <th className="text-right py-3 px-4 text-green-400 font-medium">Full Stack Realty NW (2%)</th>
                <th className="text-right py-3 px-4 text-gold-400 font-medium">Your Savings</th>
              </tr>
            </thead>
            <tbody>
              {savings.map((row) => (
                <tr key={row.price} className="border-b border-navy-800">
                  <td className="py-3 px-4 text-white font-medium">${row.price.toLocaleString()}</td>
                  <td className="py-3 px-4 text-red-300 text-right">${row.traditional.toLocaleString()}</td>
                  <td className="py-3 px-4 text-green-300 text-right">${row.ours.toLocaleString()}</td>
                  <td className="py-3 px-4 text-gold-300 text-right font-semibold">${(row.traditional - row.ours).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  )
}
