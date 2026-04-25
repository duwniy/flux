import { useFiltersStore } from '../../store/filters'

const CITIES = ['Все города', 'Москва', 'Санкт-Петербург']
const SELLERS = ['Все продавцы', 'Собственник', 'Агентство', 'Застройщик']
const SEGMENTS = ['Все сегменты', 'до 5 млн', '5-15 млн', '15+ млн']
const PERIODS = ['7 дней', '30 дней', '90 дней']

export function FilterBar() {
  const { filters, setFilter } = useFiltersStore()

  return (
    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
      {([
        ['city', CITIES],
        ['sellerType', SELLERS],
        ['priceSegment', SEGMENTS],
        ['period', PERIODS],
      ] as [keyof typeof filters, string[]][]).map(([key, opts]) => (
        <select
          key={key}
          value={filters[key]}
          onChange={(e) => setFilter(key, e.target.value)}
          style={{ fontSize: 13, padding: '5px 10px', borderRadius: 6 }}
        >
          {opts.map((o) => (
            <option key={o} value={o}>
              {o}
            </option>
          ))}
        </select>
      ))}
    </div>
  )
}
