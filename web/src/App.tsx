import { BrowserRouter, NavLink, Route, Routes } from 'react-router-dom'
import { Dashboard } from './pages/Dashboard'
import { ListingDetail } from './pages/ListingDetail'

export function App() {
  return (
    <BrowserRouter>
      <div
        style={{
          minHeight: '100vh',
          background: 'var(--color-background-tertiary, #f5f4f0)',
        }}
      >
        <nav
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            padding: '12px 20px',
            display: 'flex',
            gap: 24,
          }}
        >
          <NavLink
            to="/"
            style={({ isActive }) => ({
              fontSize: 14,
              fontWeight: isActive ? 500 : 400,
              color: isActive
                ? 'var(--color-text-primary, #1d1d1b)'
                : 'var(--color-text-secondary, #5f5a52)',
              textDecoration: 'none',
            })}
          >
            Дашборд
          </NavLink>
        </nav>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/listings/:id" element={<ListingDetail />} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}
