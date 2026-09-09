/* eslint-disable react-hooks/set-state-in-effect */
import { useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import { getAllBrowserHistory, getAllBrowserHistoryTags, getAllChromeUrls, getAllVisits, getUser } from '../api/browserHistoryApi';
import { getAllTags } from '../api/tagApi';
import { getSummaryHistory } from '../api/analyticsApi';
import { formatDateTime, formatSeconds } from '../utils/format';
import TablePagination from '../components/TablePagination';

const TABS = [
  ['browser-history', 'Browser History'], ['chrome-urls', 'Chrome URLs'], ['visits', 'Visits'],
  ['history-tags', 'History Tags'], ['tags', 'Tags'], ['analytics', 'Analytics Summary'], ['users', 'Users'],
];

export default function DataExplorerPage() {
  const [active, setActive] = useState('browser-history');
  const [datasets, setDatasets] = useState({});
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(25);

  const loaders = useMemo(() => ({
    'browser-history': getAllBrowserHistory,
    'chrome-urls': getAllChromeUrls,
    'visits': getAllVisits,
    'history-tags': getAllBrowserHistoryTags,
    tags: getAllTags,
    analytics: getSummaryHistory,
    users: async () => [await getUser()],
  }), []);

  useEffect(() => {
    if (datasets[active]) return;
    setLoading(true);
    loaders[active]().then((rows) => setDatasets((prev) => ({ ...prev, [active]: Array.isArray(rows) ? rows : [] })))
      .catch((err) => toast.error(err.response?.data?.error || err.message || `Unable to load ${active}.`))
      .finally(() => setLoading(false));
  }, [active, datasets, loaders]);

  const rows = datasets[active] || [];
  const normalizedSize = pageSize === 'ALL' ? Math.max(rows.length, 1) : pageSize;
  const pageCount = Math.max(1, Math.ceil(rows.length / normalizedSize));
  const currentPage = Math.min(page, pageCount);
  const visible = rows.slice((currentPage - 1) * normalizedSize, currentPage * normalizedSize);

  return <div className="app-page app-page-wide">
    <div className="page-heading mb-4"><div className="eyebrow">BACKEND DATA MODEL</div><h1 className="h3 mb-1">Data Explorer</h1><p className="text-muted mb-0">Review the operational tables exposed by the Spring Boot APIs. Read-only views keep the relational source data visible without coupling it to form screens.</p></div>
    <div className="card dashboard-card"><div className="card-header p-0"><div className="nav nav-tabs data-tabs px-2 pt-2 overflow-auto flex-nowrap">{TABS.map(([value, label]) => <button key={value} type="button" className={`nav-link ${active === value ? 'active' : ''}`} onClick={() => { setActive(value); setPage(1); }}>{label}</button>)}</div></div><div className="card-body p-0">
      {loading ? <div className="p-4 text-muted">Loading {TABS.find(([v]) => v === active)?.[1]}…</div> : <ExplorerTable type={active} rows={visible} />}
      {!loading && <TablePagination page={currentPage} pageSize={pageSize} total={rows.length} onPageChange={setPage} onPageSizeChange={(size) => { setPageSize(size); setPage(1); }} />}
    </div></div>
  </div>;
}

function ExplorerTable({ type, rows }) {
  let head = [];
  let body = [];
  if (type === 'browser-history') { head = ['ID', 'Domain', 'Title', 'Visits', 'Last visited', 'Browser']; body = rows.map((r) => [r.browser_history_Id, r.domain, r.title || r.url, r.visitCount ?? 0, formatDateTime(r.visitedAt), r.browserName]); }
  if (type === 'chrome-urls') { head = ['ID', 'Chrome ID', 'Domain', 'Category', 'Visits', 'Total time', 'Last visit']; body = rows.map((r) => [r.id, r.chromeUrlId, r.domain, r.category, r.visitCount ?? 0, formatSeconds(r.totalTimeSpentSeconds), formatDateTime(r.lastVisitTime)]); }
  if (type === 'visits') { head = ['ID', 'Chrome visit ID', 'Domain', 'Visit time', 'Duration', 'Day', 'Hour', 'Typed']; body = rows.map((r) => [r.id, r.chromeVisitId, r.domain, formatDateTime(r.visitTime), formatSeconds(r.visitDurationSeconds), r.dayOfWeek, r.hourOfDay, r.typedNavigation ? 'Yes' : 'No']); }
  if (type === 'history-tags') { head = ['ID', 'History ID', 'Tag', 'Confidence', 'High confidence', 'Created']; body = rows.map((r) => [r.browser_history_tag_Id, r.browserHistoryId ?? '—', r.tag?.tag_Name || 'N/A', Number(r.confidenceScore ?? 0).toFixed(2), r.highConfidence ? 'Yes' : 'No', formatDateTime(r.createdAt)]); }
  if (type === 'tags') { head = ['ID', 'Name', 'Category', 'Keywords', 'Description', 'Created']; body = rows.map((r) => [r.tag_Id, r.tag_Name, r.category || '—', r.matchKeywords, r.description || '—', formatDateTime(r.createdAt)]); }
  if (type === 'analytics') { head = ['ID', 'Period', 'Range', 'Visits', 'Domains', 'Top domain', 'Top tag', 'Min/Avg/Max time', 'Min/Avg/Max visits']; body = rows.map((r) => [r.analytics_summary_Id, r.periodType, `${formatDateTime(r.periodStart)} → ${formatDateTime(r.periodEnd)}`, r.totalVisits, r.uniqueDomains, r.topDomain, r.topTag, `${formatSeconds(r.minTimeSpent)} / ${formatSeconds(r.averageTimeSpent)} / ${formatSeconds(r.maxTimeSpent)}`, `${r.minVisitCount ?? 0} / ${Number(r.averageVisitCount ?? 0).toFixed(1)} / ${r.maxVisitCount ?? 0}`]); }
  if (type === 'users') { head = ['ID', 'Name', 'Email', 'Created', 'Updated']; body = rows.map((r) => [r.users_Id, r.name, r.email, formatDateTime(r.createdAt), formatDateTime(r.updatedAt)]); }

  return <>
    <div className="table-responsive"><table className="table table-hover align-middle mb-0"><thead><tr>{head.map((h) => <th key={h}>{h}</th>)}</tr></thead><tbody>{!body.length && <tr><td colSpan={head.length} className="p-4 text-muted">No records found.</td></tr>}{body.map((cells, index) => <tr key={`${type}-${index}`}>{cells.map((cell, i) => <td key={`${i}`}>{String(cell ?? '—')}</td>)}</tr>)}</tbody></table></div>

  </>;
}
