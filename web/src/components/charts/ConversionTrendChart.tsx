import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import type { ConversionAnalytics } from '../../types'

interface Props {
  data: ConversionAnalytics[]
}

export function ConversionTrendChart({ data }: Props) {
  return (
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
        Тренд конверсии по версиям
      </div>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={data} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border-tertiary, #dad7cf)" />
          <XAxis
            dataKey="date"
            tick={{ fontSize: 11, fill: 'var(--color-text-secondary, #5f5a52)' }}
          />
          <YAxis
            yAxisId="left"
            tick={{ fontSize: 11, fill: 'var(--color-text-secondary, #5f5a52)' }}
          />
          <YAxis
            yAxisId="right"
            orientation="right"
            tick={{ fontSize: 11, fill: 'var(--color-text-secondary, #5f5a52)' }}
          />
          <Tooltip
            contentStyle={{
              background: 'var(--color-background-primary, #ffffff)',
              border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
              borderRadius: 6,
              fontSize: 12,
            }}
          />
          <Legend />
          <Line
            yAxisId="left"
            type="monotone"
            dataKey="views"
            stroke="#7F77DD"
            name="Просмотры"
            dot={false}
            strokeWidth={2}
          />
          <Line
            yAxisId="left"
            type="monotone"
            dataKey="clicks"
            stroke="#534AB7"
            name="Клики"
            dot={false}
            strokeWidth={2}
          />
          <Line
            yAxisId="right"
            type="monotone"
            dataKey="conversionRate"
            stroke="#1D9E75"
            name="Конверсия %"
            dot={false}
            strokeWidth={2}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
