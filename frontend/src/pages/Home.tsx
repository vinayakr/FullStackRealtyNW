import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, BookOpen } from 'lucide-react'
import Hero from '../components/Hero'
import About from '../components/About'
import CommissionSection from '../components/CommissionSection'
import ArticleCard from '../components/ArticleCard'
import { articlesApi } from '../api/client'
import type { ArticleSummary } from '../types'

export default function Home() {
  const [articles, setArticles] = useState<ArticleSummary[]>([])

  useEffect(() => {
    articlesApi.getAll().then((data) => setArticles(data.slice(0, 3))).catch(() => {})
  }, [])

  return (
    <>
      <Hero />
      <About />
      <CommissionSection />

      {/* Articles preview */}
      {articles.length > 0 && (
        <section className="py-20 bg-gray-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-end justify-between mb-10">
              <div>
                <div className="inline-flex items-center gap-2 text-gold-600 text-sm font-semibold mb-2">
                  <BookOpen className="w-4 h-4" />
                  Knowledge Base
                </div>
                <h2 className="section-title mb-0">Latest Articles</h2>
              </div>
              <Link
                to="/articles"
                className="hidden sm:flex items-center gap-2 text-navy-700 hover:text-gold-600 font-medium text-sm transition-colors"
              >
                View all articles
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>

            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {articles.map((article) => (
                <ArticleCard key={article.id} article={article} />
              ))}
            </div>

            <div className="mt-8 sm:hidden text-center">
              <Link to="/articles" className="btn-outline">
                View all articles
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>
        </section>
      )}

      {/* CTA section */}
      <section className="py-20 bg-white">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="section-title">Ready to Find Your Home?</h2>
          <p className="section-subtitle mx-auto mb-8">
            Our AI advisor learns about your family's needs and guides you toward neighborhoods and
            homes that actually fit your life — not just your budget.
          </p>
          <Link to="/chat" className="btn-primary text-base px-10 py-4">
            Start the Conversation
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>
    </>
  )
}
