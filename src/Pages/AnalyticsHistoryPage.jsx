import { useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { getSummaryHistory } from '../api/analyticsApi';
import AnalyticsAggregateCards from '../components/AnalyticsAggregateCards';
import AnalyticsTrendChart from '../components/AnalyticsTrendChart';
import AnalyticsSummaryRowCharts from '../components/AnalyticsSummaryRowCharts';
import TablePagination from '../components/TablePagination';
import { formatDate, formatSeconds, formatDateTime } from '../utils/format';
import { getPeriodLabel } from '../utils/period';

const FILTERS = [
  ['ALL', 'All periods'], ['DAILY', 'Day'], ['WEEKLY', 'Week'], ['MONTHLY', 'Month'],
  ['QUARTERLY', 'Quarter'], ['HALF_YEARLY', 'Half Year'], ['YEARLY', 'Year'],
];

export default function AnalyticsHistoryPage() {
  const [summaries, setSummaries] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(25);
  const [openSummaryId, setOpenSummaryId] = useState(null);

  const load = async () => {
    setLoading(true);
    try { setSummaries(await getSummaryHistory()); }
    catch (err) { toast.error(err.response?.data?.error || err.message || 'Unable to load analytics history.'); }
    finally { setLoading(false); }
  };

  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => { load(); }, []);

  const filtered = useMemo(
    () => filter === 'ALL' ? summaries : summaries.filter((s) => s.periodType === filter),
    [filter, summaries],
  );
  const normalizedSize = pageSize === 'ALL' ? Math.max(filtered.length, 1) : pageSize;
  const pageCount = Math.max(1, Math.ceil(filtered.length / normalizedSize));
  const currentPage = Math.min(page, pageCount);
  const visible = filtered.slice((currentPage - 1) * normalizedSize, currentPage * normalizedSize);
  const latest = filtered[0] || null;
  const selectedSummary = filtered.find((item) => item.analytics_summary_Id === openSummaryId) || null;


  return <div className="app-page app-page-wide">
    <div className="page-heading d-flex flex-wrap justify-content-between gap-3 mb-4">
      <div><div className="eyebrow">PERSISTED ANALYTICS</div><h1 className="h3 mb-1">Analytics Summary</h1><p className="text-muted mb-0">Every generated period is preserved with the backend aggregate values for later review.</p></div>
      <button className="btn btn-outline-secondary" onClick={load} disabled={loading}>{loading ? 'Refreshing…' : 'Refresh'}</button>
    </div>

    {latest && <AnalyticsAggregateCards summary={latest} />}

    <div className="card dashboard-card mb-4">
      <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span>Saved period trend</span>
        <select className="form-select form-select-sm history-filter" value={filter} onChange={(e) => { setFilter(e.target.value); setPage(1); setOpenSummaryId(null); }}>
          {FILTERS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
      </div>
      <div className="card-body chart-card-body"><AnalyticsTrendChart summaries={filtered} /></div>
    </div>

    {selectedSummary && (
      <div className="card dashboard-card mb-4 analytics-row-chart-card">
        <AnalyticsSummaryRowCharts summary={selectedSummary} onClose={() => setOpenSummaryId(null)} />
      </div>
    )}

    <div className="card dashboard-card analytics-history-table-card">
      <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span>Analytics Summary Entity table</span>
        <span className="small text-muted">Click “View charts” to inspect one saved period.</span>
      </div>
      <div className="table-scroll-shell">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0 table-analytics">
            <thead><tr>
              <th>Period</th><th>Range</th><th>Total Visits</th><th>Unique Domains</th><th>Top Domain</th><th>Top Tag</th>
              <th>Min Time</th><th>Avg Time</th><th>Max Time</th><th>Min Visits</th><th>Avg Visits</th><th>Max Visits</th>
              <th>Active Hour</th><th>Created</th><th>Updated</th><th className="text-end">Charts</th>
            </tr></thead>
            <tbody>
              {!visible.length && <tr><td colSpan={16} className="text-muted p-4">No analytics summaries have been generated yet.</td></tr>}
              {visible.map((s) => (
                <SummaryTableRow
                  key={s.analytics_summary_Id}
                  summary={s}
                  isOpen={openSummaryId === s.analytics_summary_Id}
                  onToggle={() => setOpenSummaryId((current) => current === s.analytics_summary_Id ? null : s.analytics_summary_Id)}
                />
              ))}
            </tbody>
          </table>
        </div>
      </div>
      <TablePagination
        page={currentPage}
        pageSize={pageSize}
        total={filtered.length}
        onPageChange={setPage}
        onPageSizeChange={(size) => { setPageSize(size); setPage(1); setOpenSummaryId(null); }}
      />
    </div>
  </div>;
}

function SummaryTableRow({ summary, isOpen, onToggle }) {
  return <>
    <tr>
      <td>{getPeriodLabel(summary.periodType)}</td>
      <td>{formatDate(summary.periodStart)} – {formatDate(summary.periodEnd)}</td>
      <td>{summary.totalVisits ?? 0}</td>
      <td>{summary.uniqueDomains ?? 0}</td>
      <td>{summary.topDomain || 'N/A'}</td>
      <td>{summary.topTag || 'N/A'}</td>
      <td>{formatSeconds(summary.minTimeSpent)}</td>
      <td>{formatSeconds(summary.averageTimeSpent)}</td>
      <td>{formatSeconds(summary.maxTimeSpent)}</td>
      <td>{summary.minVisitCount ?? 0}</td>
      <td>{Number(summary.averageVisitCount ?? 0).toFixed(1)}</td>
      <td>{summary.maxVisitCount ?? 0}</td>
      <td>{summary.mostActiveHour == null ? 'N/A' : `${summary.mostActiveHour}:00`}</td>
      <td>{formatDateTime(summary.createdAt)}</td>
      <td>{formatDateTime(summary.updatedAt)}</td>
      <td className="text-end">
        <button type="button" className={`btn btn-sm ${isOpen ? 'btn-primary' : 'btn-outline-primary'}`} onClick={onToggle}>
          {isOpen ? 'Hide charts' : 'View charts'}
        </button>
      </td>
    </tr>
  </>;
}
