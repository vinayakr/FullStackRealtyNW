import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Mail, Phone, Home } from 'lucide-react'
import { useContactModal } from '../context/ContactModalContext'

export default function Footer() {
  const openContact = useContactModal()
  const [showPhone, setShowPhone] = useState(false)
  return (
    <footer className="bg-navy-950 text-gray-400">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid md:grid-cols-3 gap-10">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 bg-gold-500 rounded-lg flex items-center justify-center">
                <Home className="w-5 h-5 text-white" />
              </div>
              <div>
                <span className="text-white font-serif font-bold text-lg leading-none block">
                  Full Stack Realty NW
                </span>
              </div>
            </div>
            <p className="text-sm leading-relaxed">
              Pacific Northwest real estate done right — investor-grade expertise, transparent 2%
              commission, and a genuine commitment to your goals.
            </p>
          </div>

          {/* Links */}
          <div>
            <h4 className="text-white font-semibold mb-4">Navigation</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/" className="hover:text-white transition-colors">Home</Link></li>
              <li><Link to="/chat" className="hover:text-white transition-colors">Find My Home (AI Advisor)</Link></li>
              <li><Link to="/articles" className="hover:text-white transition-colors">Articles & Resources</Link></li>
              <li>
                <button onClick={openContact} className="hover:text-white transition-colors">
                  Contact Vinny
                </button>
              </li>
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h4 className="text-white font-semibold mb-4">Get In Touch</h4>
            <div className="flex items-center gap-2 text-sm mb-2">
              <Mail className="w-4 h-4 flex-shrink-0" />
              <span>vinny@fullstackrealtynw.com</span>
            </div>
            <div className="flex items-center gap-2 text-sm mb-4">
              <Phone className="w-4 h-4 flex-shrink-0" />
              {showPhone ? (
                <span>425-686-9156</span>
              ) : (
                <button
                  onClick={() => setShowPhone(true)}
                  className="text-gold-400 hover:text-gold-300 transition-colors"
                >
                  Show phone number
                </button>
              )}
            </div>
            <button onClick={openContact} className="btn-primary text-sm py-2">
              Send a Message
            </button>
          </div>
        </div>

        <div className="mt-10 pt-8 border-t border-navy-800 flex flex-col sm:flex-row justify-between items-center gap-4 text-xs">
          <p>© {new Date().getFullYear()} Full Stack Realty NW. All rights reserved.</p>
          <p className="text-gray-600">Licensed Real Estate Agent · Pacific Northwest</p>
        </div>
      </div>
    </footer>
  )
}
