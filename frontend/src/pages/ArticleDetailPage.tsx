import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, Clock, Tag, Loader, User } from 'lucide-react'
import ReactMarkdown from 'react-markdown'
import { articlesApi } from '../api/client'
import type { Article } from '../types'

function formatDate(dateStr: string) {
  try {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric',
    })
  } catch {
    return dateStr
  }
}

export default function ArticleDetailPage() {
  const { slug } = useParams<{ slug: string }>()
  const [article, setArticle] = useState<Article | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!slug) return
    setLoading(true)
    articlesApi
      .getBySlug(slug)
      .then(setArticle)
      .catch(() => setError('Article not found.'))
      .finally(() => setLoading(false))
  }, [slug])

  if (loading) {
    return (
      <div className="flex justify-center py-32">
        <Loader className="w-8 h-8 text-navy-400 animate-spin" />
      </div>
    )
  }

  if (error || !article) {
    return (
      <div className="text-center py-32">
        <p className="text-gray-500 mb-6">{error || 'Article not found.'}</p>
        <Link to="/articles" className="btn-outline">
          <ArrowLeft className="w-4 h-4" />
          Back to Articles
        </Link>
      </div>
    )
  }

  return (
    <div className="min-h-screen">
      {/* Hero */}
      <div className="bg-gradient-to-br from-navy-900 to-navy-800 text-white py-16">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <Link
            to="/articles"
            className="inline-flex items-center gap-2 text-gray-400 hover:text-white text-sm mb-6 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Articles
          </Link>

          {article.category && (
            <div className="flex items-center gap-2 text-gold-400 text-sm font-semibold mb-3">
              <Tag className="w-4 h-4" />
              {article.category}
            </div>
          )}

          <h1 className="font-serif text-3xl md:text-4xl lg:text-5xl font-bold leading-tight mb-6">
            {article.title}
          </h1>

          <div className="flex flex-wrap items-center gap-4 text-sm text-gray-400">
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-full bg-gold-500 flex items-center justify-center">
                <User className="w-4 h-4 text-white" />
              </div>
              {article.author}
            </div>
            <div className="flex items-center gap-1">
              <Clock className="w-4 h-4" />
              {article.readTimeMinutes} min read
            </div>
            <div>{formatDate(article.publishedAt)}</div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="prose-realty">
          <ReactMarkdown>{article.content}</ReactMarkdown>
        </div>

        {/* CTA */}
        <div className="mt-14 bg-navy-900 text-white rounded-2xl p-8 text-center">
          <h3 className="font-serif text-2xl font-bold mb-3">Ready to Take the Next Step?</h3>
          <p className="text-gray-400 mb-6">
            Talk to our AI advisor or reach out to Vinny directly — no pressure, just real answers.
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-4">
            <Link to="/chat" className="btn-primary">
              Try the AI Home Advisor
            </Link>
            <a
              href="mailto:vinny@fullstackrealtynw.com"
              className="inline-flex items-center justify-center gap-2 border-2 border-white/30 text-white hover:bg-white/10 font-semibold px-6 py-3 rounded-lg transition-colors"
            >
              Email Vinny
            </a>
          </div>
        </div>
      </div>
    </div>
  )
}
