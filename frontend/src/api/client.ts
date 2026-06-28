import axios from 'axios'
import type { Article, ArticleSummary, ChatSession, ChatMessage } from '../types'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

export const contactApi = {
  submit: (data: { name: string; email: string; message: string }) =>
    api.post('/contact', data).then((r) => r.data),
}

export const articlesApi = {
  getAll: (category?: string) =>
    api.get<ArticleSummary[]>('/articles', { params: category ? { category } : {} }).then((r) => r.data),
  getBySlug: (slug: string) => api.get<Article>(`/articles/${slug}`).then((r) => r.data),
}

export const chatApi = {
  createSession: () => api.post<ChatSession>('/chat/sessions').then((r) => r.data),
  getHistory: (sessionId: string) =>
    api.get<ChatMessage[]>(`/chat/sessions/${sessionId}/messages`).then((r) => r.data),

  streamMessage: async (
    sessionId: string,
    content: string,
    onChunk: (text: string) => void,
    onDone: () => void,
    onError: (err: string) => void
  ) => {
    try {
      const response = await fetch(`/api/chat/sessions/${sessionId}/messages`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content }),
      })

      if (!response.ok) {
        onError(`Server error: ${response.status}`)
        return
      }

      const reader = response.body!.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() ?? ''

        for (const line of lines) {
          if (!line.startsWith('data: ')) continue
          const data = line.slice(6).trim()
          if (data === '[DONE]') {
            onDone()
            return
          }
          try {
            const parsed = JSON.parse(data)
            if (parsed.text) onChunk(parsed.text)
            if (parsed.error) onError(parsed.error)
          } catch {
            // skip
          }
        }
      }

      onDone()
    } catch (err) {
      onError(err instanceof Error ? err.message : 'Connection error')
    }
  },
}
