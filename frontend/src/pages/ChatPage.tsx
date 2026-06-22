import ChatInterface from '../components/ChatInterface'
import { MessageSquare, Shield, Zap } from 'lucide-react'

const features = [
  { icon: MessageSquare, text: 'Natural back-and-forth conversation' },
  { icon: Zap, text: 'Personalized neighborhood recommendations' },
  { icon: Shield, text: 'No signup required — start instantly' },
]

export default function ChatPage() {
  return (
    <div className="min-h-[calc(100vh-64px)] bg-gradient-to-br from-navy-950 via-navy-900 to-navy-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="grid lg:grid-cols-[1fr_2fr] gap-10 items-start">
          {/* Left sidebar */}
          <div className="text-white">
            <div className="mb-6">
              <div className="inline-block bg-gold-500/20 text-gold-300 text-xs font-semibold px-3 py-1 rounded-full mb-3">
                AI Home Advisor
              </div>
              <h1 className="font-serif text-3xl font-bold mb-3">
                Tell Us What Your Family Needs
              </h1>
              <p className="text-gray-400 leading-relaxed">
                Our AI advisor has deep knowledge of Pacific Northwest neighborhoods, school
                districts, commute patterns, and market conditions. Describe your situation and
                we'll guide you toward homes that genuinely fit your life.
              </p>
            </div>

            <ul className="space-y-3 mb-8">
              {features.map(({ icon: Icon, text }) => (
                <li key={text} className="flex items-center gap-3 text-sm text-gray-300">
                  <div className="w-7 h-7 rounded-lg bg-navy-800 flex items-center justify-center flex-shrink-0">
                    <Icon className="w-4 h-4 text-gold-400" />
                  </div>
                  {text}
                </li>
              ))}
            </ul>

            <div className="bg-navy-800/60 rounded-xl p-5 border border-navy-700">
              <p className="text-sm text-gray-400 leading-relaxed">
                <span className="text-gold-400 font-semibold">Prefer to talk directly?</span>
                <br />
                Reach Vinny at{' '}
                <a
                  href="mailto:vinny@fullstackrealtynw.com"
                  className="text-gold-300 hover:text-gold-200 underline"
                >
                  vinny@fullstackrealtynw.com
                </a>
              </p>
            </div>
          </div>

          {/* Chat interface */}
          <div className="h-[calc(100vh-140px)] min-h-[600px]">
            <ChatInterface />
          </div>
        </div>
      </div>
    </div>
  )
}
