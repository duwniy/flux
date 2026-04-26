import { format, parseISO } from 'date-fns'
import { ru } from 'date-fns/locale'
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { ScoreTrendPoint } from '../../types'

interface Props {
  data: ScoreTrendPoint[]
}

const safeFormatDate = (value: string, pattern: string) => {
  try {
    if (!value) return '-'
    return format(parseISO(value), pattern, { locale: ru })
  } catch {
    return value || '-'
  }
}

export function ScoreTrendChart({ data }: Props) {
  return (
    <ResponsiveContainer width="100%" height={120}>
      <LineChart data={data} margin={{ top: 4, right: 8, bottom: 0, left: -20 }}>
        <CartesianGrid
          stroke="var(--color-border-tertiary, #dad7cf)"
          strokeDasharray="3 3"
          vertical={false}
        />
        <XAxis
          dataKey="date"
          tickFormatter={(v) => safeFormatDate(String(v), 'd MMM')}
          tick={{ fontSize: 11, fill: 'var(--color-text-tertiary, #8f877d)' }}
          axisLine={false}
          tickLine={false}
        />
        <YAxis
          domain={['auto', 'auto']}
          tick={{ fontSize: 11, fill: 'var(--color-text-tertiary, #8f877d)' }}
          axisLine={false}
          tickLine={false}
        />
        <Tooltip
          contentStyle={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-secondary, #d7d2c8)',
            borderRadius: 8,
            fontSize: 13,
          }}
          formatter={(v: number) => [v.toFixed(1), 'Средний скоринг']}
          labelFormatter={(l) => safeFormatDate(String(l), 'd MMMM')}
        />
        <Line
          type="monotone"
          dataKey="avgScore"
          stroke="#1D9E75"
          strokeWidth={2}
          dot={false}
          activeDot={{ r: 4, fill: '#1D9E75' }}
        />
      </LineChart>
    </ResponsiveContainer>
  )
}
