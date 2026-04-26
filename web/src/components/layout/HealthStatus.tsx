import { useEffect, useState, useCallback } from 'react'
import { monitoringApi } from '../../api/scoring'
import type { HealthReport } from '../../types'

export function HealthStatus() {
  const [health, setHealth] = useState<HealthReport | null>(null)

  const loadHealth = useCallback(async () => {
    try {
      const data = await monitoringApi.getHealth()
      setHealth(data)
    } catch {
      setHealth(null)
    }
  }, [])

  useEffect(() => {
    loadHealth()
    const interval = setInterval(loadHealth, 60_000)
    return () => clearInterval(interval)
  }, [loadHealth])

  if (!health) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <div
          style={{
            width: 8,
            height: 8,
            borderRadius: '50%',
            background: '#dad7cf',
          }}
        />
        <span style={{ fontSize: 12, color: 'var(--color-text-tertiary, #8f877d)' }}>
          Загрузка…
        </span>
      </div>
    )
  }

  const pipelines = Array.isArray(health.pipelines) ? health.pipelines : []
  const isHealthy = health.status === 'UP' && pipelines.every((p) => p.status === 'UP')

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 6,
        padding: '4px 8px',
        borderRadius: 6,
        cursor: 'default',
      }}
      title={
        isHealthy
          ? 'Все системы работают'
          : `Проблемы: ${pipelines.filter((p) => p.status !== 'UP').map((p) => p.name).join(', ')}`
      }
    >
      <div
        style={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          background: isHealthy ? '#1D9E75' : '#F59E0B',
          boxShadow: isHealthy ? '0 0 6px rgba(29,158,117,0.4)' : '0 0 6px rgba(245,158,11,0.4)',
        }}
      />
      <span
        style={{
          fontSize: 12,
          color: 'var(--color-text-secondary, #5f5a52)',
          fontWeight: 500,
        }}
      >
        {isHealthy ? 'Система OK' : 'Есть проблемы'}
      </span>
    </div>
  )
}
