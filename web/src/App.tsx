import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { Sidebar } from './components/layout/Sidebar'
import { Topbar } from './components/layout/Topbar'
import { ToastContainer } from './components/layout/Toast'
import { Dashboard } from './pages/Dashboard'
import { Listings } from './pages/Listings'
import { ListingDetail } from './pages/ListingDetail'
import { ScoringModels } from './pages/ScoringModels'
import { Monitoring } from './pages/Monitoring'
import { Enrichment } from './pages/Enrichment'

export function App() {
  return (
    <BrowserRouter>
      <div
        style={{
          display: 'flex',
          minHeight: '100vh',
          background: 'var(--color-background-tertiary, #f5f4f0)',
        }}
      >
        <Sidebar />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
          <Topbar />
          <main style={{ flex: 1, overflow: 'auto' }}>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/listings" element={<Listings />} />
              <Route path="/listings/:id" element={<ListingDetail />} />
              <Route path="/scoring-models" element={<ScoringModels />} />
              <Route path="/monitoring" element={<Monitoring />} />
              <Route path="/enrichment" element={<Enrichment />} />
            </Routes>
          </main>
        </div>
        <ToastContainer />
      </div>
    </BrowserRouter>
  )
}
