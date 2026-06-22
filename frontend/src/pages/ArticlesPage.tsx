import { useEffect, useState } from 'react'
import { BookOpen, Loader } from 'lucide-react'
import ArticleCard from '../components/ArticleCard'
import { articlesApi } from '../api/client'
import type { ArticleSummary } from '../types'

const CATEGORIES = ['All', 'Buying', 'Selling', 'Investing', 'Market Insights']

export default function ArticlesPage() {
  const [articles, setArticles] = useState<ArticleSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeCategory, setActiveCategory] = useState('All')

  useEffect(() => {
    setLoading(true)
    const req = activeCategory === 'All'
      ? articlesApi.getAll()
      : articlesApi.getAll(activeCategory)

    req
      .then(setArticles)
      .catch(() => setError('Failed to load articles. Please try again.'))
      .finally(() => setLoading(false))
  }, [activeCategory])

  return (
    <div className="min-h-screen">
      {/* Header */}
      <div className="bg-navy-900 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3 mb-3">
            <BookOpen className="w-5 h-5 text-gold-400" />
            <span className="text-gold-400 text-sm font-semibold tracking-wide uppercase">
              Knowledge Base
            </span>
          </div>
          <h1 className="font-serif text-4xl md:text-5xl font-bold mb-4">
            Real Estate Insights
          </h1>
          <p className="text-gray-400 text-lg max-w-2xl">
            Practical guides for Pacific Northwest buyers, sellers, and investors — written by
            someone who has been on both sides of every transaction.
          </p>
        </div>
      </div>

      {/* Category filter */}
      <div className="bg-white border-b border-gray-200 sticky top-16 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex gap-1 overflow-x-auto py-3 scrollbar-none">
            {CATEGORIES.map((cat) => (
              <button
                key={cat}
                onClick={() => setActiveCategory(cat)}
                className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
                  activeCategory === cat
                    ? 'bg-navy-900 text-white'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Articles */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {loading && (
          <div className="flex justify-center py-20">
            <Loader className="w-8 h-8 text-navy-400 animate-spin" />
          </div>
        )}

        {error && !loading && (
          <div className="text-center py-20">
            <p className="text-red-600 mb-4">{error}</p>
            <button
              onClick={() => setActiveCategory(activeCategory)}
              className="btn-outline"
            >
              Retry
            </button>
          </div>
        )}

        {!loading && !error && articles.length === 0 && (
          <div className="text-center py-20 text-gray-400">
            No articles in this category yet.
          </div>
        )}

        {!loading && !error && articles.length > 0 && (
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {articles.map((article) => (
              <ArticleCard key={article.id} article={article} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
