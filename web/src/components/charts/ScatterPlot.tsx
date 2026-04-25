import {
  ResponsiveContainer,
  Scatter,
  ScatterChart,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { ScatterPoint } from '../../types'

interface Props {
  data: ScatterPoint[]
}

const dotColor = (score: number) =>
  score >= 80 ? '#26215C' : score >= 60 ? '#534AB7' : score >= 40 ? '#7F77DD' : '#AFA9EC'

export function ScatterPlot({ data }: Props) {
  return (
    <ResponsiveContainer width="100%" height={180}>
      <ScatterChart margin={{ top: 4, right: 8, bottom: 0, left: -20 }}>
        <XAxis
          type="number"
          dataKey="score"
          domain={[0, 100]}
          name="Скоринг"
          tick={{ fontSize: 11, fill: 'var(--color-text-tertiary, #8f877d)' }}
          axisLine={false}
          tickLine={false}
          label={{
            value: 'Скоринг',
            position: 'insideBottom',
            offset: -2,
            fontSize: 11,
            fill: 'var(--color-text-tertiary, #8f877d)',
          }}
        />
        <YAxis
          type="number"
          dataKey="conversionRate"
          name="Конверсия"
          unit="%"
          tick={{ fontSize: 11, fill: 'var(--color-text-tertiary, #8f877d)' }}
          axisLine={false}
          tickLine={false}
        />
        <Tooltip
          cursor={{ strokeDasharray: '3 3' }}
          contentStyle={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-secondary, #d7d2c8)',
            borderRadius: 8,
            fontSize: 13,
          }}
          formatter={(v: number, name: string) => [
            name === 'conversionRate' ? `${v.toFixed(1)}%` : v,
            name === 'conversionRate' ? 'Конверсия' : 'Скоринг',
          ]}
        />
        <Scatter
          data={data}
          shape={(props: any) => (
            <circle cx={props.cx} cy={props.cy} r={5} fill={dotColor(props.payload.score)} opacity={0.75} />
          )}
        />
      </ScatterChart>
    </ResponsiveContainer>
  )
}
