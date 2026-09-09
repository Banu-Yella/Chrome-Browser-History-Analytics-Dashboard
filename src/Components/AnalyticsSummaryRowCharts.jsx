import { useEffect, useState } from 'react';
import {
  Bar,
  Cell,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  Radar,
  RadarChart,
  PolarAngleAxis,
  PolarGrid,
  PolarRadiusAxis,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { getAnalyticsCharts } from '../api/analyticsApi';
import { formatSeconds } from '../utils/format';

const PIE_COLORS = ['#1b2a4a', '#d9822b', '#3f7d5c', '#a3374a', '#6a5acd', '#2f9e9e', '#c98a2c', '#516b84'];

export default function AnalyticsSummaryRowCharts({ summary, onClose }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const start = String(summary.periodStart || '').slice(0, 10);
        const end = String(summary.periodEnd || '').slice(0, 10);
        const response = await getAnalyticsCharts(start, end);
        if (!cancelled) setData(response || {});
      } catch (err) {
        if (!cancelled) setError(err.response?.data?.error || err.message || 'Unable to load row charts.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [summary]);

  const timeData = [
    { name: 'Min', seconds: Number(summary.minTimeSpent || 0) },
    { name: 'Average', seconds: Number(summary.averageTimeSpent || 0) },
    { name: 'Max', seconds: Number(summary.maxTimeSpent || 0) },
  ];

  const visitData = [
    { name: 'Min', visits: Number(summary.minVisitCount || 0) },
    { name: 'Average', visits: Number(summary.averageVisitCount || 0) },
    { name: 'Max', visits: Number(summary.maxVisitCount || 0) },
  ];

  const topDomains = (Array.isArray(data?.topDomains) ? data.topDomains : []).slice(0, 8).map((item) => ({
    domain: item.domain,
    visits: Number(item.totalVisits || 0),
  }));

  const tags = Object.entries(data?.tagDistribution || {})
    .map(([name, value]) => ({ name, value: Number(value || 0) }))
    .filter((item) => item.value > 0)
    .sort((a, b) => b.value - a.value);

  const categories = (Array.isArray(data?.categories) ? data.categories : [])
    .map((item) => ({ name: item.category || 'Other', value: Number(item.totalVisits || 0) }))
    .filter((item) => item.value > 0)
    .sort((a, b) => b.value - a.value)
    .slice(0, 12);

  return (
    <div className="summary-row-charts border-top">
      <div className="summary-row-charts-header d-flex flex-wrap justify-content-between align-items-center gap-2 px-3 py-2">
        <div>
          <strong>Charts for {summary.periodType || 'Period'}</strong>
          <span className="small text-muted ms-2">{String(summary.periodStart || '').slice(0, 10)} → {String(summary.periodEnd || '').slice(0, 10)}</span>
        </div>
        <button type="button" className="btn btn-sm btn-outline-secondary" onClick={onClose}>Close charts</button>
      </div>

      {loading && <div className="p-4 text-muted">Loading period-specific charts…</div>}
      {error && <div className="p-4 text-danger">{error}</div>}

      {!loading && !error && (
        <div className="row g-3 p-3">
          <ChartPanel title="Min / average / max time per visit">
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={timeData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis tickFormatter={(value) => formatSeconds(value)} />
                <Tooltip formatter={(value) => [formatSeconds(value), 'Time']} />
                <Line type="monotone" dataKey="seconds" name="Time" stroke="#1b2a4a" strokeWidth={3} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </ChartPanel>

          <ChartPanel title="Min / average / max visits per domain">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={visitData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="visits" name="Visits" fill="#d9822b" radius={[5, 5, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartPanel>

          <ChartPanel title="Top domains in this period">
            {topDomains.length ? (
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={topDomains} margin={{ left: 5, right: 10, bottom: 50 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="domain" angle={-30} textAnchor="end" interval={0} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="visits" name="Visits" fill="#1b2a4a" radius={[5, 5, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : <ChartEmpty />}
          </ChartPanel>

          <ChartPanel title="Tag distribution in this period">
            {tags.length ? (
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={tags} dataKey="value" nameKey="name" innerRadius={54} outerRadius={92} paddingAngle={1} label={false}>
                    {tags.map((item, index) => <Cell key={item.name} fill={PIE_COLORS[index % PIE_COLORS.length]} />)}
                  </Pie>
                  <Tooltip />
                  <Legend layout="vertical" align="right" verticalAlign="middle" wrapperStyle={{ maxHeight: 230, overflowY: 'auto', fontSize: 11 }} />
                </PieChart>
              </ResponsiveContainer>
            ) : <ChartEmpty />}
          </ChartPanel>

          <ChartPanel title="Browsing categories">
            {categories.length ? (
              <ResponsiveContainer width="100%" height={290}>
                <RadarChart data={categories} outerRadius="72%">
                  <PolarGrid />
                  <PolarAngleAxis dataKey="name" tick={{ fontSize: 10 }} />
                  <PolarRadiusAxis />
                  <Radar dataKey="value" name="Visits" fill="#3f7d5c" fillOpacity={0.45} stroke="#3f7d5c" />
                  <Tooltip />
                </RadarChart>
              </ResponsiveContainer>
            ) : <ChartEmpty />}
          </ChartPanel>

          <ChartPanel title="Summary snapshot">
            <div className="summary-snapshot-grid">
              <Metric label="Total visits" value={summary.totalVisits ?? 0} />
              <Metric label="Unique domains" value={summary.uniqueDomains ?? 0} />
              <Metric label="Top domain" value={summary.topDomain || 'N/A'} />
              <Metric label="Top tag" value={summary.topTag || 'N/A'} />
              <Metric label="Active hour" value={summary.mostActiveHour == null ? 'N/A' : `${summary.mostActiveHour}:00`} />
              <Metric label="Total time" value={formatSeconds(summary.totalTimeSpent)} />
            </div>
          </ChartPanel>
        </div>
      )}
    </div>
  );
}

function ChartPanel({ title, children }) {
  return <div className="col-xl-6"><div className="chart-panel"><div className="chart-panel-title">{title}</div><div className="chart-panel-body">{children}</div></div></div>;
}

function ChartEmpty() {
  return <div className="chart-empty">No period-specific data available.</div>;
}

function Metric({ label, value }) {
  return <div className="summary-mini-metric"><span>{label}</span><strong>{value}</strong></div>;
}
