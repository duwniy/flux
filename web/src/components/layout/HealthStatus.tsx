import { useEffect, useState } from 'react'
import { analyticsApi } from '../../api/analytics'

interface HealthResponse {
  status: string
  pipelines?: Array<{
    name: string
    status: string
  }>
}

export function HealthStatus() {
  const [health, setHealth] = useState<HealthResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    analyticsApi.getHealth().then((data) => {
      setHealth(data)
      setLoading(false)
    })
  }, [])

  if (loading || !health) {
    return null
  }

  const allUp = health.pipelines?.every((p) => p.status === 'UP') ?? true
  const isHealthy = health.status === 'UP' && allUp

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 8,
        cursor: 'pointer',
        padding: '6px 10px',
        borderRadius: 6,
        transition: 'background 200ms',
      }}
      onClick={() => {
        // Navigate to monitoring page when implemented
        console.log('Navigate to monitoring')
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.background = 'var(--color-background-secondary, #ece9e2)'
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.background = 'transparent'
      }}
    >
      <div
        style={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          background: isHealthy ? '#1D9E75' : '#F59E0B',
        }}
      />
      <span
        style={{
          fontSize: 12,
          color: 'var(--color-text-secondary, #5f5a52)',
          fontWeight: 500,
        }}
      >
        {isHealthy ? 'Система работает' : 'Есть проблемы в пайплайне'}
      </span>
    </div>
  )
}
