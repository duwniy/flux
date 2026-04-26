import { useEffect, useState } from 'react'
import { monitoringApi } from '../api/scoring'
import { PipelineHealthCard } from '../components/monitoring/PipelineHealthCard'
import { QualityReportTable } from '../components/monitoring/QualityReportTable'
import type { HealthReport, QualityReport } from '../types'

export function Monitoring() {
  const [health, setHealth] = useState<HealthReport | null>(null)
  const [quality, setQuality] = useState<QualityReport | null>(null)
  const [isSyncing, setIsSyncing] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  useEffect(() => {
    loadData()
    const interval = setInterval(loadData, 30000)
    return () => clearInterval(interval)
  }, [])

  const loadData = async () => {
    try {
      const [h, q] = await Promise.all([monitoringApi.getHealth(), monitoringApi.getQualityReport()])
      setHealth(h)
      setQuality(q)
    } catch (error) {
      console.error('Error loading monitoring data:', error)
    }
  }

  const handleSync = async () => {
    setIsSyncing(true)
    try {
      await monitoringApi.syncReporting()
      setToast('Синхронизация запущена (асинхронно)')
      setTimeout(() => setToast(null), 3000)
    } catch (error) {
      setToast('Ошибка при запуске синхронизации')
      setTimeout(() => setToast(null), 3000)
    } finally {
      setIsSyncing(false)
    }
  }

  const isHealthy = health?.status === 'UP'
  const hasIssues = health && health.status !== 'UP'
  const problemPipelines = health?.pipelines.filter((p) => p.status !== 'UP') || []

  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
      {/* Overall status banner */}
      {health && (
        <div
          style={{
            padding: 16,
            background: isHealthy ? '#E8F5E9' : '#FFEBEE',
            border: `0.5px solid ${isHealthy ? '#1D9E75' : '#E24B4A'}`,
            borderRadius: 12,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <div>
            <h2
              style={{
                fontSize: 14,
                fontWeight: 500,
                margin: 0,
                color: isHealthy ? '#1D9E75' : '#E24B4A',
              }}
            >
              {isHealthy ? 'Все системы работают нормально' : 'Обнаружены проблемы в пайплайнах'}
            </h2>
            {hasIssues && problemPipelines.length > 0 && (
              <p style={{ fontSize: 12, margin: '4px 0 0 0', color: '#E24B4A' }}>
                Проблемы в: {problemPipelines.map((p) => p.name).join(', ')}
              </p>
            )}
          </div>
          <button
            onClick={handleSync}
            disabled={isSyncing}
            style={{
              padding: '10px 16px',
              background: '#1D9E75',
              color: 'white',
              border: 'none',
              borderRadius: 6,
              fontSize: 13,
              fontWeight: 500,
              cursor: isSyncing ? 'not-allowed' : 'pointer',
              opacity: isSyncing ? 0.6 : 1,
            }}
          >
            {isSyncing ? 'Синхронизирую...' : 'Запустить ETL синк'}
          </button>
        </div>
      )}

      {/* Pipeline Health Cards */}
      {health && (
        <div>
          <h3 style={{ fontSize: 14, fontWeight: 500, margin: '0 0 12px 0' }}>Здоровье пайплайнов</h3>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
              gap: 16,
            }}
          >
            {health.pipelines.map((pipeline) => (
              <PipelineHealthCard key={pipeline.name} pipeline={pipeline} />
            ))}
          </div>
        </div>
      )}

      {/* Data Quality Report */}
      {quality && (
        <div>
          <h3 style={{ fontSize: 14, fontWeight: 500, margin: '0 0 12px 0' }}>Отчёт качества данных</h3>

          {/* Summary Stats */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: 12,
              marginBottom: 16,
            }}
          >
            <div
              style={{
                background: 'var(--color-background-primary, #ffffff)',
                border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                borderRadius: 12,
                padding: 16,
                textAlign: 'center',
              }}
            >
              <div style={{ fontSize: 12, color: 'var(--color-text-secondary, #5f5a52)', marginBottom: 8 }}>
                Всего проверок
              </div>
              <div style={{ fontSize: 28, fontWeight: 500 }}>{quality.totalChecks}</div>
            </div>

            <div
              style={{
                background: 'var(--color-background-primary, #ffffff)',
                border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                borderRadius: 12,
                padding: 16,
                textAlign: 'center',
              }}
            >
              <div style={{ fontSize: 12, color: '#1D9E75', marginBottom: 8 }}>Прошли проверку</div>
              <div style={{ fontSize: 28, fontWeight: 500, color: '#1D9E75' }}>{quality.passedChecks}</div>
            </div>

            <div
              style={{
                background: 'var(--color-background-primary, #ffffff)',
                border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                borderRadius: 12,
                padding: 16,
                textAlign: 'center',
              }}
            >
              <div style={{ fontSize: 12, color: '#E24B4A', marginBottom: 8 }}>Не прошли проверку</div>
              <div style={{ fontSize: 28, fontWeight: 500, color: '#E24B4A' }}>{quality.failedChecks}</div>
            </div>

            <div
              style={{
                background: 'var(--color-background-primary, #ffffff)',
                border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                borderRadius: 12,
                padding: 16,
                textAlign: 'center',
              }}
            >
              <div style={{ fontSize: 12, color: 'var(--color-text-secondary, #5f5a52)', marginBottom: 8 }}>
                Процент успеха
              </div>
              <div style={{ fontSize: 28, fontWeight: 500 }}>{quality.successPercentage.toFixed(1)}%</div>
            </div>
          </div>

          {/* Failed Checks Table */}
          {quality.failedCheckDetails.length > 0 && (
            <div>
              <h4 style={{ fontSize: 13, fontWeight: 500, margin: '0 0 12px 0' }}>Упавшие проверки</h4>
              <QualityReportTable checks={quality.failedCheckDetails} />
            </div>
          )}
        </div>
      )}

      {/* Toast notification */}
      {toast && (
        <div
          style={{
            position: 'fixed',
            bottom: 20,
            right: 20,
            background: '#1D9E75',
            color: 'white',
            padding: '12px 16px',
            borderRadius: 6,
            fontSize: 13,
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
          }}
        >
          {toast}
        </div>
      )}
    </div>
  )
}
