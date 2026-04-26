import { useEffect, useState } from 'react'
import { analyticsApi } from '../api/analytics'
import { ScatterPlot } from '../components/charts/ScatterPlot'
import { DistrictScoreChart } from '../components/charts/DistrictScoreChart'
import { FunnelChart } from '../components/charts/FunnelChart'
import { ScoreTrendChart } from '../components/charts/ScoreTrendChart'
import { ConversionFunnelTable } from '../components/charts/ConversionFunnelTable'
import { KpiCard } from '../components/kpi/KpiCard'
import { FilterBar } from '../components/layout/FilterBar'
import { HealthStatus } from '../components/layout/HealthStatus'
import { useFiltersStore } from '../store/filters'
import type {
  AnalyticsSummary,
  DashboardStats,
  DistrictScore,
  ScatterPoint,
  ScoreTrendPoint,
} from '../types'

export function Dashboard() {
  const { filters } = useFiltersStore()
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [trend, setTrend] = useState<ScoreTrendPoint[]>([])
  const [districts, setDistricts] = useState<DistrictScore[]>([])
  const [scatter, setScatter] = useState<ScatterPoint[]>([])
  const [funnel] = useState<AnalyticsSummary | null>({
    views: 993,
    phoneClicks: 120,
    favorites: 87,
  })

  useEffect(() => {
    const params = Object.fromEntries(Object.entries(filters).filter(([, v]) => v))
    Promise.all([
      analyticsApi.getDashboardStats(params),
      analyticsApi.getScoreTrend(params),
      analyticsApi.getDistrictScores(filters.city),
      analyticsApi.getScatterData(params),
    ]).then(([s, t, d, sc]) => {
      setStats(s)
      setTrend(t)
      setDistricts(d)
      setScatter(sc)
      setError(null)
    }).catch((e) => {
      console.error('Dashboard loading failed', e)
      setError('Не удалось загрузить данные дашборда')
    })
  }, [filters])

  if (error) {
    return <div style={{ padding: 32, color: '#A32D2D' }}>{error}</div>
  }

  if (!stats) {
    return <div style={{ padding: 32, color: 'var(--color-text-secondary)' }}>Загрузка...</div>
  }

  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: 10,
        }}
      >
        <h2 style={{ fontSize: 18, fontWeight: 500 }}>Аналитика платформы</h2>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <HealthStatus />
          <FilterBar />
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
        <KpiCard
          label="Средний скоринг"
          value={stats.avgScore.toFixed(1)}
          delta={stats.avgScoreDelta}
          deltaLabel="vs прошлый период"
        />
        <KpiCard
          label="Активных объявлений"
          value={stats.activeListings.toLocaleString('ru')}
          delta={stats.activeListingsDelta}
          deltaLabel="%"
        />
        <KpiCard
          label="Конверсия в звонок"
          value={`${stats.conversionRate.toFixed(1)}%`}
          delta={stats.conversionDelta}
          deltaLabel="%"
        />
        <KpiCard
          label="Обогащено"
          value={`${stats.enrichedPct.toFixed(1)}%`}
          delta={stats.enrichedDelta}
          deltaLabel="%"
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: 16 }}>
        <div
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 12,
            padding: 16,
          }}
        >
          <div
            style={{
              fontSize: 13,
              fontWeight: 500,
              color: 'var(--color-text-secondary, #5f5a52)',
              marginBottom: 14,
            }}
          >
            Динамика среднего скоринга
          </div>
          <ScoreTrendChart data={trend} />
        </div>
        <div
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 12,
            padding: 16,
          }}
        >
          <div
            style={{
              fontSize: 13,
              fontWeight: 500,
              color: 'var(--color-text-secondary, #5f5a52)',
              marginBottom: 14,
            }}
          >
            Скоринг по районам
          </div>
          <DistrictScoreChart data={districts} />
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <div
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 12,
            padding: 16,
          }}
        >
          <div
            style={{
              fontSize: 13,
              fontWeight: 500,
              color: 'var(--color-text-secondary, #5f5a52)',
              marginBottom: 14,
            }}
          >
            Воронка конверсии
          </div>
          {funnel && <FunnelChart totalListings={stats.activeListings} summary={funnel} />}
          <div style={{ marginTop: 16, borderTop: '0.5px solid var(--color-border-tertiary, #dad7cf)', paddingTop: 16 }}>
            <div
              style={{
                fontSize: 12,
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
                marginBottom: 10,
              }}
            >
              По районам
            </div>
            <ConversionFunnelTable data={districts} />
          </div>
        </div>
        <div
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 12,
            padding: 16,
          }}
        >
          <div
            style={{
              fontSize: 13,
              fontWeight: 500,
              color: 'var(--color-text-secondary, #5f5a52)',
              marginBottom: 14,
            }}
          >
            Скоринг vs конверсия
          </div>
          <ScatterPlot data={scatter} />
        </div>
      </div>
    </div>
  )
}
