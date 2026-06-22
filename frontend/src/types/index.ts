export interface Article {
  id: number
  title: string
  slug: string
  excerpt: string | null
  content: string
  author: string
  category: string | null
  imageUrl: string | null
  readTimeMinutes: number
  publishedAt: string
}

export interface ArticleSummary {
  id: number
  title: string
  slug: string
  excerpt: string | null
  author: string
  category: string | null
  imageUrl: string | null
  readTimeMinutes: number
  publishedAt: string
}

export interface ChatSession {
  id: string
  createdAt: string
}

export interface ChatMessage {
  id: number
  sessionId: string
  role: 'user' | 'assistant'
  content: string
  createdAt: string
}

export interface StreamingMessage {
  role: 'user' | 'assistant'
  content: string
  isStreaming?: boolean
}
