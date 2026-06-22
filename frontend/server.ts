import express from 'express'
import compression from 'compression'
import helmet from 'helmet'
import path from 'path'
import { createProxyMiddleware } from 'http-proxy-middleware'

const app = express()
const PORT = process.env.PORT || 3000
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080'
const distPath = path.join(__dirname, '../dist')

app.use(compression())
app.use(
  helmet({
    contentSecurityPolicy: false,
  })
)

// Proxy API calls to Kotlin backend
app.use(
  '/api',
  createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
  })
)

// Serve static files from Vite build
app.use(express.static(distPath))

// SPA fallback — serve index.html for all non-API routes
app.get('*', (_req, res) => {
  res.sendFile(path.join(distPath, 'index.html'))
})

app.listen(PORT, () => {
  console.log(`Frontend server running on http://localhost:${PORT}`)
  console.log(`Proxying API requests to ${BACKEND_URL}`)
})
