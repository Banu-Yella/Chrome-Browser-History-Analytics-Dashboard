import { formatSeconds } from '../utils/format';
export default function AnalyticsAggregateCards({ summary }) {
  if (!summary) return null;
  const cards = [
    ['Min time / visit', formatSeconds(summary.minTimeSpent)],
    ['Avg time / visit', formatSeconds(summary.averageTimeSpent)],
    ['Max time / visit', formatSeconds(summary.maxTimeSpent)],
    ['Min visits / domain', summary.minVisitCount ?? 0],
    ['Avg visits / domain', Number(summary.averageVisitCount || 0).toFixed(1)],
    ['Max visits / domain', summary.maxVisitCount ?? 0],
  ];
  return <div className="row g-3 mb-4">{cards.map(([label, value]) => <div className="col-6 col-lg-4" key={label}><div className="metric-card"><div className="small text-muted">{label}</div><div className="metric-value">{value}</div></div></div>)}</div>;
}
