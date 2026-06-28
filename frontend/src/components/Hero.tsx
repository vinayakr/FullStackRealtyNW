import { Link } from 'react-router-dom'
import { MessageSquare, TrendingDown, Award } from 'lucide-react'
import { useContactModal } from '../context/ContactModalContext'

export default function Hero() {
  const openContact = useContactModal()
  return (
    <section className="relative bg-navy-900 text-white overflow-hidden">
      {/* Background pattern */}
      <div className="absolute inset-0 opacity-10">
        <div
          className="absolute inset-0"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`,
          }}
        />
      </div>

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 lg:py-32">
        <div className="max-w-3xl">
          <div className="inline-flex items-center gap-2 bg-gold-500/20 border border-gold-500/40 text-gold-300 px-4 py-1.5 rounded-full text-sm font-medium mb-6">
            <TrendingDown className="w-4 h-4" />
            Only 2% Listing Commission — Save Thousands
          </div>

          <h1 className="font-serif text-5xl md:text-6xl lg:text-7xl font-bold leading-tight mb-6">
            Your Pacific Northwest
            <span className="text-gold-400"> Home Journey</span> Starts Here
          </h1>

          <p className="text-xl text-gray-300 leading-relaxed mb-8 max-w-2xl">
            Whether you're buying your first home, selling for top dollar, or building a real estate
            portfolio — Full Stack Realty NW brings investor-grade expertise and transparent
            pricing to every transaction.
          </p>

          <div className="flex flex-col sm:flex-row gap-4">
            <Link to="/chat" className="btn-primary text-base px-8 py-4">
              <MessageSquare className="w-5 h-5" />
              Find My Perfect Home
            </Link>
            <button
              onClick={openContact}
              className="inline-flex items-center gap-2 border-2 border-white/30 text-white hover:bg-white/10 font-semibold px-8 py-4 rounded-lg transition-colors duration-200 text-base"
            >
              <Award className="w-5 h-5" />
              Talk to Vinny
            </button>
          </div>

          {/* Stats */}
          <div className="mt-16 grid grid-cols-3 gap-8 border-t border-white/10 pt-10">
            <div>
              <div className="text-4xl font-serif font-bold text-gold-400">2%</div>
              <div className="text-sm text-gray-400 mt-1">Listing Commission</div>
            </div>
            <div>
              <div className="text-4xl font-serif font-bold text-gold-400">10+</div>
              <div className="text-sm text-gray-400 mt-1">Years Investing in PNW</div>
            </div>
            <div>
              <div className="text-4xl font-serif font-bold text-gold-400">$0</div>
              <div className="text-sm text-gray-400 mt-1">Hidden Fees</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
