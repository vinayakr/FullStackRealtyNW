import { TrendingUp, Home, Users, Star } from 'lucide-react'

const highlights = [
  {
    icon: TrendingUp,
    title: 'Active Real Estate Investor',
    desc: 'I don\'t just sell homes — I invest in them. That means I analyze every property through an investor\'s lens, helping you understand true value, not just list price.',
  },
  {
    icon: Home,
    title: 'Full-Service Representation',
    desc: 'From pricing strategy and professional photography to negotiation and closing coordination — you get everything a traditional agent offers at a fraction of the cost.',
  },
  {
    icon: Users,
    title: 'Family-First Approach',
    desc: 'Every family has a unique story. I take the time to understand your goals, your lifestyle, and your timeline before recommending a single neighborhood.',
  },
  {
    icon: Star,
    title: 'Pacific Northwest Expert',
    desc: 'Born and built in the PNW — I know the neighborhoods, the school districts, the commute patterns, and the market dynamics that matter to your decision.',
  },
]

export default function About() {
  return (
    <section id="about" className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid lg:grid-cols-2 gap-16 items-center">
          {/* Left: bio */}
          <div>
            <div className="inline-block bg-gold-100 text-gold-700 text-sm font-semibold px-3 py-1 rounded-full mb-4">
              About Vinny Rao
            </div>
            <h2 className="section-title">
              An Agent Who Thinks Like an Investor
            </h2>
            <p className="text-gray-600 leading-relaxed mb-5">
              Most real estate agents have never owned an investment property. I've owned dozens. That
              difference shapes everything about how I approach a transaction — from how I price a
              listing to how I evaluate an offer to how I negotiate repairs.
            </p>
            <p className="text-gray-600 leading-relaxed mb-5">
              My background in full-stack technology also means I market properties strategically in
              the digital world where buyers actually search. Your listing doesn't just go on the MLS
              — it gets targeted, data-driven exposure to the buyers most likely to love your home.
            </p>
            <p className="text-gray-600 leading-relaxed mb-8">
              And because I believe in transparent, fair pricing, I charge <strong>2% listing
              commission</strong> — saving the average Pacific Northwest seller $6,000–$12,000
              compared to traditional brokerages.
            </p>
            <a
              href="mailto:vinny@fullstackrealtynw.com"
              className="btn-primary"
            >
              Reach Out Directly
            </a>
          </div>

          {/* Right: highlights grid */}
          <div className="grid sm:grid-cols-2 gap-6">
            {highlights.map(({ icon: Icon, title, desc }) => (
              <div key={title} className="p-6 rounded-2xl bg-gray-50 hover:bg-navy-50 transition-colors">
                <div className="w-10 h-10 bg-navy-900 rounded-xl flex items-center justify-center mb-4">
                  <Icon className="w-5 h-5 text-gold-400" />
                </div>
                <h3 className="font-semibold text-navy-900 mb-2">{title}</h3>
                <p className="text-sm text-gray-600 leading-relaxed">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}
