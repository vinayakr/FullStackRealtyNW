import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import ChatPage from './pages/ChatPage'
import ArticlesPage from './pages/ArticlesPage'
import ArticleDetailPage from './pages/ArticleDetailPage'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import { ContactModalProvider } from './context/ContactModalContext'

export default function App() {
  return (
    <BrowserRouter>
      <ContactModalProvider>
        <div className="min-h-screen flex flex-col">
          <Navbar />
          <main className="flex-1">
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/chat" element={<ChatPage />} />
              <Route path="/articles" element={<ArticlesPage />} />
              <Route path="/articles/:slug" element={<ArticleDetailPage />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </ContactModalProvider>
    </BrowserRouter>
  )
}
