import { NavLink } from 'react-router-dom'

const navItems = [
  { to: '/', label: 'Дашборд', icon: '📊' },
  { to: '/listings', label: 'Объявления', icon: '🏠' },
  { to: '/scoring-models', label: 'Скоринговые модели', icon: '⚖️' },
  { to: '/monitoring', label: 'Мониторинг', icon: '📡' },
  { to: '/enrichment', label: 'Обогащение', icon: '🔬' },
]

export function Sidebar() {
  return (
    <aside
      style={{
        width: 220,
        minHeight: '100vh',
        background: 'var(--color-background-primary, #ffffff)',
        borderRight: '0.5px solid var(--color-border-tertiary, #dad7cf)',
        padding: '20px 0',
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        flexShrink: 0,
      }}
    >
      <div
        style={{
          padding: '0 16px 16px',
          marginBottom: 8,
          borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
        }}
      >
        <div style={{ fontSize: 16, fontWeight: 600, color: 'var(--color-text-primary, #1d1d1b)' }}>
          Flux Pipeline
        </div>
        <div style={{ fontSize: 11, color: 'var(--color-text-tertiary, #8f877d)', marginTop: 2 }}>
          Analytics Platform
        </div>
      </div>

      <nav style={{ display: 'flex', flexDirection: 'column', gap: 2, padding: '0 8px' }}>
        {navItems.map(({ to, label, icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            style={({ isActive }) => ({
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '10px 12px',
              borderRadius: 8,
              fontSize: 13,
              fontWeight: isActive ? 500 : 400,
              color: isActive
                ? 'var(--color-text-primary, #1d1d1b)'
                : 'var(--color-text-secondary, #5f5a52)',
              background: isActive
                ? 'var(--color-background-secondary, #ece9e2)'
                : 'transparent',
              textDecoration: 'none',
              transition: 'background 150ms, color 150ms',
            })}
          >
            <span style={{ fontSize: 15 }}>{icon}</span>
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
