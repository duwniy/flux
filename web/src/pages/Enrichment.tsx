import { useEffect, useState, useCallback } from 'react'
import { enrichmentApi } from '../api/enrichment'
import { InlineError } from '../components/layout/InlineError'
import { SkeletonBlock } from '../components/layout/SkeletonBlock'
import type { EnrichmentStats } from '../types'

export function Enrichment() {
  const [stats, setStats] = useState<EnrichmentStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  const loadStats = useCallback(async () => {
    setLoading(true)
    setError(false)
    try {
      const data = await enrichmentApi.getStats()
      setStats(data)
    } catch {
      setError(true)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadStats()
  }, [loadStats])

  const successRate = stats && stats.totalRuns > 0
    ? (stats.successCount / stats.totalRuns) * 100
    : 0

  const getBarColor = (rate: number) => {
    if (rate >= 95) return '#1D9E75'
    if (rate >= 80) return '#EF9F27'
    return '#E24B4A'
  }

  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h2 style={{ fontSize: 18, fontWeight: 500, margin: 0 }}>Обогащение данных</h2>

      {/* Loading Skeleton */}
      {loading && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12 }}>
          {[1, 2, 3].map((i) => (
            <div
              key={i}
              style={{
                background: 'var(--color-background-primary, #ffffff)',
                border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                borderRadius: 12,
                padding: 20,
              }}
            >
              <SkeletonBlock rows={2} height={20} gap={12} />
            </div>
          ))}
        </div>
      )}

      {/* Error */}
      {!loading && error && <InlineError onRetry={loadStats} />}

      {/* Stats Content */}
      {!loading && !error && stats && (
        <>
          {/* KPI Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12 }}>
            <KpiStatCard
              label="Всего запусков"
              value={stats.totalRuns.toLocaleString('ru-RU')}
              color="var(--color-text-primary, #1d1d1b)"
            />
            <KpiStatCard
              label="Успешных"
              value={stats.successCount.toLocaleString('ru-RU')}
              color="#1D9E75"
            />
            <KpiStatCard
              label="Ошибок"
              value={stats.failedCount.toLocaleString('ru-RU')}
              color="#E24B4A"
            />
          </div>

          {/* Success Rate Progress Bar */}
          <div
            style={{
              background: 'var(--color-background-primary, #ffffff)',
              border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
              borderRadius: 12,
              padding: 20,
            }}
          >
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 12,
              }}
            >
              <span style={{ fontSize: 13, fontWeight: 500, color: 'var(--color-text-secondary, #5f5a52)' }}>
                Процент успешности
              </span>
              <span
                style={{
                  fontSize: 20,
                  fontWeight: 600,
                  color: getBarColor(successRate),
                }}
              >
                {successRate.toFixed(1)}%
              </span>
            </div>
            <div
              style={{
                height: 10,
                background: 'var(--color-background-secondary, #ece9e2)',
                borderRadius: 5,
                overflow: 'hidden',
              }}
            >
              <div
                style={{
                  height: '100%',
                  width: `${Math.min(successRate, 100)}%`,
                  background: getBarColor(successRate),
                  borderRadius: 5,
                  transition: 'width 600ms ease-out',
                }}
              />
            </div>
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                marginTop: 8,
                fontSize: 11,
                color: 'var(--color-text-tertiary, #8f877d)',
              }}
            >
              <span>0%</span>
              <div style={{ display: 'flex', gap: 16 }}>
                <span style={{ color: '#E24B4A' }}>● &lt;80% — критично</span>
                <span style={{ color: '#EF9F27' }}>● 80–95% — внимание</span>
                <span style={{ color: '#1D9E75' }}>● ≥95% — норма</span>
              </div>
              <span>100%</span>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

function KpiStatCard({ label, value, color }: { label: string; value: string; color: string }) {
  return (
    <div
      style={{
        background: 'var(--color-background-primary, #ffffff)',
        border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
        borderRadius: 12,
        padding: 20,
        textAlign: 'center',
      }}
    >
      <div style={{ fontSize: 12, color: 'var(--color-text-secondary, #5f5a52)', marginBottom: 8 }}>
        {label}
      </div>
      <div style={{ fontSize: 32, fontWeight: 600, color }}>{value}</div>
    </div>
  )
}
