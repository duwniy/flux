import { HealthStatus } from './HealthStatus'

export function Topbar() {
  return (
    <header
      style={{
        background: 'var(--color-background-primary, #ffffff)',
        borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
        padding: '0 20px',
        height: 48,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexShrink: 0,
      }}
    >
      <div style={{ fontSize: 14, fontWeight: 500, color: 'var(--color-text-primary, #1d1d1b)' }}>
        Flux Pipeline
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <HealthStatus />
        <a
          href="http://localhost:8080/swagger-ui/index.html"
          target="_blank"
          rel="noopener noreferrer"
          style={{
            fontSize: 12,
            fontWeight: 500,
            color: '#534AB7',
            textDecoration: 'none',
            padding: '5px 10px',
            borderRadius: 6,
            border: '1px solid #534AB7',
            transition: 'background 150ms',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = '#F3F1FE'
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = 'transparent'
          }}
        >
          Swagger UI ↗
        </a>
      </div>
    </header>
  )
}
