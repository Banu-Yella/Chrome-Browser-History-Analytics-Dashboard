import { formatSeconds, formatDateTime } from '../utils/format';
import { getPeriodLabel } from '../utils/period';

export default function AnalyticsSummaryCards({ summary }) {
  const cards = [
    { label: 'Total Visits', value: summary.totalVisits },
    { label: 'Unique Domains', value: summary.uniqueDomains },
    { label: 'Top Domain', value: summary.topDomain },
    { label: 'Top Tag', value: summary.topTag },
    { label: 'Most Active Hour', value: summary.mostActiveHour != null ? `${summary.mostActiveHour}:00` : 'N/A' },
    { label: 'Total Time Spent', value: formatSeconds(summary.totalTimeSpent) },
    { label: 'Avg Time / Visit', value: formatSeconds(summary.averageTimeSpent) },
  ];

  return (
    <div className="row g-3">
      {cards.map((c) => (
        <div key={c.label} className="col-6 col-md-4 col-lg-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="text-muted small">{c.label}</div>
              <div className="fs-5 fw-semibold">{c.value ?? 'N/A'}</div>
            </div>
          </div>
        </div>
      ))}
      <div className="col-12">
        <div className="text-muted small">
          Period: {getPeriodLabel(summary.periodType)} ({formatDateTime(summary.periodStart)} – {formatDateTime(summary.periodEnd)})
        </div>
      </div>
    </div>
  );
}
