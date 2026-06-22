import { useState, useEffect, useRef, useCallback } from 'react'
import { Send, Bot, User, RefreshCw, MessageSquare } from 'lucide-react'
import { chatApi } from '../api/client'
import type { StreamingMessage } from '../types'
import ReactMarkdown from 'react-markdown'

const WELCOME_MESSAGE: StreamingMessage = {
  role: 'assistant',
  content: "Hi! I'm your Full Stack Realty NW home advisor. I'm here to help you find the perfect Pacific Northwest home based on your family's unique needs and goals.\n\nTo get started — could you tell me a bit about your situation? Are you looking to **buy a home**, **sell a property**, or **explore real estate investing** in the PNW?",
}

export default function ChatInterface() {
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [messages, setMessages] = useState<StreamingMessage[]>([WELCOME_MESSAGE])
  const [input, setInput] = useState('')
  const [isStreaming, setIsStreaming] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const bottomRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)

  const initSession = useCallback(async () => {
    try {
      const session = await chatApi.createSession()
      setSessionId(session.id)
      setMessages([WELCOME_MESSAGE])
      setError(null)
    } catch {
      setError('Could not connect to the chat server. Please try again.')
    }
  }, [])

  useEffect(() => {
    initSession()
  }, [initSession])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const sendMessage = async () => {
    if (!input.trim() || isStreaming || !sessionId) return

    const userText = input.trim()
    setInput('')
    setError(null)
    setIsStreaming(true)

    // Add user message
    setMessages((prev) => [...prev, { role: 'user', content: userText }])

    // Add empty assistant message that will be streamed into
    setMessages((prev) => [...prev, { role: 'assistant', content: '', isStreaming: true }])

    await chatApi.streamMessage(
      sessionId,
      userText,
      (chunk) => {
        setMessages((prev) => {
          const updated = [...prev]
          const last = updated[updated.length - 1]
          if (last.role === 'assistant') {
            updated[updated.length - 1] = { ...last, content: last.content + chunk }
          }
          return updated
        })
      },
      () => {
        setMessages((prev) => {
          const updated = [...prev]
          const last = updated[updated.length - 1]
          if (last.role === 'assistant') {
            updated[updated.length - 1] = { ...last, isStreaming: false }
          }
          return updated
        })
        setIsStreaming(false)
        inputRef.current?.focus()
      },
      (err) => {
        setError(err)
        setMessages((prev) => {
          const updated = [...prev]
          const last = updated[updated.length - 1]
          if (last.role === 'assistant' && last.content === '') {
            updated.pop()
          }
          return updated
        })
        setIsStreaming(false)
      }
    )
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  const resetChat = async () => {
    await initSession()
  }

  return (
    <div className="flex flex-col h-full bg-gray-50 rounded-2xl overflow-hidden shadow-xl border border-gray-200">
      {/* Header */}
      <div className="bg-navy-900 text-white px-6 py-4 flex items-center justify-between flex-shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gold-500 rounded-xl flex items-center justify-center">
            <MessageSquare className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="font-semibold">Full Stack Realty NW Advisor</div>
            <div className="text-xs text-gray-400">Powered by AI · Guided by Vinny</div>
          </div>
        </div>
        <button
          onClick={resetChat}
          title="Start new conversation"
          className="p-2 text-gray-400 hover:text-white hover:bg-navy-800 rounded-lg transition-colors"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto chat-scroll p-6 space-y-6">
        {messages.map((msg, i) => (
          <div key={i} className={`flex gap-3 ${msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'}`}>
            {/* Avatar */}
            <div
              className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                msg.role === 'assistant' ? 'bg-navy-900' : 'bg-gold-500'
              }`}
            >
              {msg.role === 'assistant' ? (
                <Bot className="w-4 h-4 text-gold-400" />
              ) : (
                <User className="w-4 h-4 text-white" />
              )}
            </div>

            {/* Bubble */}
            <div
              className={`max-w-[78%] rounded-2xl px-4 py-3 ${
                msg.role === 'user'
                  ? 'bg-navy-900 text-white rounded-tr-sm'
                  : 'bg-white text-gray-800 shadow-sm rounded-tl-sm border border-gray-100'
              } ${msg.isStreaming ? 'typing-cursor' : ''}`}
            >
              {msg.role === 'assistant' ? (
                <div className="prose-realty text-sm">
                  <ReactMarkdown>{msg.content || ' '}</ReactMarkdown>
                </div>
              ) : (
                <p className="text-sm leading-relaxed whitespace-pre-wrap">{msg.content}</p>
              )}
            </div>
          </div>
        ))}

        {error && (
          <div className="text-center">
            <div className="inline-block bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-2 rounded-lg">
              {error}
            </div>
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="p-4 bg-white border-t border-gray-200 flex-shrink-0">
        <div className="flex gap-3 items-end">
          <textarea
            ref={inputRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Tell me about your family and what you're looking for…"
            rows={1}
            disabled={isStreaming || !sessionId}
            className="flex-1 resize-none rounded-xl border border-gray-300 focus:border-navy-500 focus:ring-2 focus:ring-navy-200 px-4 py-3 text-sm text-gray-900 placeholder-gray-400 outline-none transition-all disabled:opacity-50 max-h-32"
            style={{ height: 'auto' }}
            onInput={(e) => {
              const t = e.currentTarget
              t.style.height = 'auto'
              t.style.height = Math.min(t.scrollHeight, 128) + 'px'
            }}
          />
          <button
            onClick={sendMessage}
            disabled={!input.trim() || isStreaming || !sessionId}
            className="w-11 h-11 bg-navy-900 hover:bg-navy-700 disabled:bg-gray-300 text-white rounded-xl flex items-center justify-center transition-colors flex-shrink-0"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
        <p className="text-xs text-gray-400 mt-2 text-center">
          Press Enter to send · Shift+Enter for new line
        </p>
      </div>
    </div>
  )
}
