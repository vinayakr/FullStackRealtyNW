import { Link } from 'react-router-dom'
import { Clock, Tag } from 'lucide-react'
import type { ArticleSummary } from '../types'

const categoryColors: Record<string, string> = {
  Selling: 'bg-blue-100 text-blue-700',
  Buying: 'bg-green-100 text-green-700',
  Investing: 'bg-purple-100 text-purple-700',
  'Market Insights': 'bg-orange-100 text-orange-700',
}

function formatDate(dateStr: string) {
  try {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  } catch {
    return dateStr
  }
}

export default function ArticleCard({ article }: { article: ArticleSummary }) {
  const colorClass = article.category ? categoryColors[article.category] ?? 'bg-gray-100 text-gray-700' : 'bg-gray-100 text-gray-700'

  return (
    <Link to={`/articles/${article.slug}`} className="card group block">
      {/* Placeholder image area */}
      <div className="h-48 bg-gradient-to-br from-navy-800 to-navy-600 relative overflow-hidden">
        <div className="absolute inset-0 flex items-center justify-center opacity-20">
          <svg className="w-24 h-24 text-white" fill="currentColor" viewBox="0 0 24 24">
            <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" />
          </svg>
        </div>
        {article.category && (
          <div className="absolute top-3 left-3">
            <span className={`inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full ${colorClass}`}>
              <Tag className="w-3 h-3" />
              {article.category}
            </span>
          </div>
        )}
      </div>

      <div className="p-6">
        <h3 className="font-serif font-semibold text-lg text-navy-900 group-hover:text-gold-600 transition-colors mb-2 line-clamp-2">
          {article.title}
        </h3>
        {article.excerpt && (
          <p className="text-gray-600 text-sm leading-relaxed line-clamp-3 mb-4">
            {article.excerpt}
          </p>
        )}
        <div className="flex items-center justify-between text-xs text-gray-400">
          <div className="flex items-center gap-1">
            <Clock className="w-3.5 h-3.5" />
            {article.readTimeMinutes} min read
          </div>
          <span>{formatDate(article.publishedAt)}</span>
        </div>
      </div>
    </Link>
  )
}
