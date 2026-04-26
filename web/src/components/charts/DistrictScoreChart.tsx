import { Bar, BarChart, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { DistrictScore } from '../../types'

interface Props {
  data: DistrictScore[]
}

const color = (score: number) =>
  score >= 70 ? '#1D9E75' : score >= 50 ? '#EF9F27' : '#E24B4A'

export function DistrictScoreChart({ data }: Props) {
  return (
    <ResponsiveContainer width="100%" height={160}>
      <BarChart
        data={data}
        layout="vertical"
        margin={{ top: 0, right: 8, bottom: 0, left: 60 }}
      >
        <XAxis
          type="number"
          domain={[0, 100]}
          tick={{ fontSize: 11, fill: 'var(--color-text-tertiary, #8f877d)' }}
          axisLine={false}
          tickLine={false}
        />
        <YAxis
          type="category"
          dataKey="districtName"
          tick={{ fontSize: 12, fill: 'var(--color-text-secondary, #5f5a52)' }}
          axisLine={false}
          tickLine={false}
          width={56}
        />
        <Tooltip
          contentStyle={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-secondary, #d7d2c8)',
            borderRadius: 8,
            fontSize: 13,
          }}
          formatter={(v: number) => [v, 'Средний скоринг']}
        />
        <Bar dataKey="avgScore" radius={[0, 4, 4, 0]} maxBarSize={14}>
          {data.map((d) => (
            <Cell key={d.districtId} fill={color(d.avgScore)} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
