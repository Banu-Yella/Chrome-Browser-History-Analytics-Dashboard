/* eslint-disable react-hooks/set-state-in-effect */
import { useEffect, useMemo, useRef, useState } from 'react';
import { toast } from 'react-toastify';
import { syncBrowserHistory, getSyncStatus, getTopDomains, getVisitCountStats } from '../api/browserHistoryApi';
import { generateSummary, getAnalyticsCharts, getSummaryHistory } from '../api/analyticsApi';
import { getTagDistribution } from '../api/browserHistoryTagApi';
import { getAllTags } from '../api/tagApi';
import AnalyticsSummaryCards from '../components/AnalyticsSummaryCards';
import AnalyticsAggregateCards from '../components/AnalyticsAggregateCards';
import AnalyticsTrendChart from '../components/AnalyticsTrendChart';
import CategoryRadarChart from '../components/CategoryRadarChart';
import TagDistributionChart from '../components/TagDistributionChart';
import TopDomainsBarChart from '../components/TopDomainBarChart';
import PeriodSelector from '../components/PeriodSelector';
import { buildPeriodSelection, defaultPeriodValue, formatInputDate } from '../utils/period';
import { formatElapsed, formatDateTime } from '../utils/format';

export default function Dashboard() {
  const [period, setPeriod] = useState({ periodType: 'DAILY', selection: defaultPeriodValue('DAILY'), resolved: buildPeriodSelection('DAILY', defaultPeriodValue('DAILY')) });
  const [isGenerating, setIsGenerating] = useState(false);
  const [progress, setProgress] = useState(0);
  const [progressMessage, setProgressMessage] = useState('');
  const [progressStatus, setProgressStatus] = useState(null);
  const [elapsedMs, setElapsedMs] = useState(0);
  const [summary, setSummary] = useState(null);
  const [summaryHistory, setSummaryHistory] = useState([]);
  const [topDomains, setTopDomains] = useState([]);
  const [tagDistribution, setTagDistribution] = useState({});
  const [categoryDistribution, setCategoryDistribution] = useState([]);
  const [tags, setTags] = useState([]);
  const [visitStats, setVisitStats] = useState(null);
  const [syncInfo, setSyncInfo] = useState(null);
  const statusTimerRef = useRef(null);
  const elapsedTimerRef = useRef(null);
  const startedAtRef = useRef(null);

  const clearTimers = () => {
    if (statusTimerRef.current) clearInterval(statusTimerRef.current);
    if (elapsedTimerRef.current) clearInterval(elapsedTimerRef.current);
    statusTimerRef.current = null;
    elapsedTimerRef.current = null;
  };

  useEffect(() => () => clearTimers(), []);

  const loadSnapshotData = async () => {
    const [domains, distribution, summaries, allTags, stats] = await Promise.all([
      getTopDomains(), getTagDistribution(), getSummaryHistory(), getAllTags(), getVisitCountStats(),
    ]);
    const safeSummaries = Array.isArray(summaries) ? summaries : [];
    setTopDomains(Array.isArray(domains) ? domains : []);
    setTagDistribution(distribution || {});
    setSummaryHistory(safeSummaries);
    setTags(Array.isArray(allTags) ? allTags : []);
    setVisitStats(stats || null);
    return safeSummaries;
  };

  const loadPeriodCharts = async (resolved) => {
    if (!resolved) return;
    const data = await getAnalyticsCharts(resolved.startDate, resolved.endDate);
    setTopDomains(Array.isArray(data.topDomains) ? data.topDomains : []);
    setTagDistribution(data.tagDistribution || {});
    setCategoryDistribution(Array.isArray(data.categories) ? data.categories : []);
  };

  useEffect(() => {
    loadSnapshotData().then((summaries) => {
      const latest = summaries[0];
      if (latest) {
        setSummary(latest);
        loadPeriodCharts({ startDate: String(latest.periodStart).slice(0, 10), endDate: String(latest.periodEnd).slice(0, 10) }).catch(() => {});
      } else {
        getAnalyticsCharts().then((data) => {
          setTopDomains(Array.isArray(data.topDomains) ? data.topDomains : []);
          setTagDistribution(data.tagDistribution || {});
          setCategoryDistribution(Array.isArray(data.categories) ? data.categories : []);
        }).catch(() => {});
      }
    }).catch((err) => toast.error(err.response?.data?.error || err.message || 'Unable to load dashboard data.'));
  }, []);

  const findExistingSummary = () => {
    const target = period.resolved;
    if (!target) return null;
    const start = formatInputDate(target.start);
    const end = formatInputDate(target.end);
    return summaryHistory.find((item) => item.periodType === period.periodType && String(item.periodStart || '').slice(0, 10) === start && String(item.periodEnd || '').slice(0, 10) === end) || null;
  };

  const beginProgress = () => {
    startedAtRef.current = Date.now();
    setElapsedMs(0);
    setProgress(3);
    setProgressMessage('Starting validation pipeline...');
    setProgressStatus({ phase: 'STARTING', processed: 0, total: 0 });
    elapsedTimerRef.current = setInterval(() => setElapsedMs(Date.now() - startedAtRef.current), 250);
    statusTimerRef.current = setInterval(async () => {
      try {
        const status = await getSyncStatus();
        if (!status) return;
        setProgressStatus(status);
        setProgress(status.percent ?? 3);
        setProgressMessage(status.message || 'Processing...');
        if (status.elapsedMillis != null) setElapsedMs(Math.max(Date.now() - startedAtRef.current, Number(status.elapsedMillis)));
      } catch { /* keep local timer alive */ }
    }, 700);
  };

  const handleGenerate = async () => {
    if (!period.resolved) return toast.warning('Choose a valid period before generating analytics.');
    const existing = findExistingSummary();
    if (existing) {
      setSummary(existing);
      await loadPeriodCharts({ startDate: String(existing.periodStart).slice(0, 10), endDate: String(existing.periodEnd).slice(0, 10) }).catch(() => {});
      toast.info(`This ${period.resolved.label} record already exists. The saved result was loaded instead of regenerating it.`);
      return;
    }

    setIsGenerating(true);
    beginProgress();
    try {
      const syncResult = await syncBrowserHistory();
      setSyncInfo(syncResult);
      setProgress(78);
      setProgressMessage('History synchronized. Calculating the selected period...');

      const generated = await generateSummary(period.periodType, period.resolved);
      setSummary(generated);
      setProgress(92);
      setProgressMessage('Refreshing period charts and analytics history...');
      const summaries = await loadSnapshotData();
      setSummaryHistory(summaries);
      await loadPeriodCharts(period.resolved);
      setProgress(100);
      setProgressMessage('Generation completed successfully.');
      toast.success(`Analytics generated for ${period.resolved.label}.`);
    } catch (err) {
      const status = err.response?.status;
      const message = err.response?.data?.error || err.message || 'Analytics generation failed.';
      if (status === 409) toast.info(message);
      else toast.error(message);
    } finally {
      clearTimers();
      setTimeout(() => { setIsGenerating(false); setProgress(0); setProgressMessage(''); setProgressStatus(null); }, 900);
    }
  };

  const selectedCategoryData = useMemo(() => (categoryDistribution.length ? categoryDistribution.map((item) => ({ name: item.category, value: Number(item.totalVisits || 0) })) : []), [categoryDistribution]);
  const totalRecords = progressStatus?.total ? `${progressStatus.processed || 0} / ${progressStatus.total}` : 'Waiting';

  return <div className="app-page app-page-wide">
    <div className="page-heading d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div><div className="eyebrow">ANALYTICS WORKSPACE</div><h1 className="h3 mb-1">Browsing Insights Dashboard</h1><p className="text-muted mb-0">Generate once, preserve results, and review the same period without rebuilding it.</p></div>
      {syncInfo?.completedAt && <div className="small text-muted">Last sync: {formatDateTime(syncInfo.completedAt)}</div>}
    </div>

    <PeriodSelector value={period} onChange={setPeriod} disabled={isGenerating} />
    <div className="d-flex flex-wrap align-items-center gap-2 mt-3 mb-4">
      <button className="btn btn-primary px-4" onClick={handleGenerate} disabled={isGenerating}>{isGenerating ? 'Generating…' : 'Generate analytics'}</button>
      <span className="small text-muted">Existing period records are reused automatically.</span>
    </div>

    {isGenerating && <div className="progress-panel card border-0 shadow-sm mb-4"><div className="card-body">
      <div className="d-flex flex-wrap justify-content-between gap-2 mb-2"><strong>{progressMessage || 'Processing...'}</strong><span className="small text-muted">Elapsed: {formatElapsed(elapsedMs)}</span></div>
      <div className="progress" style={{ height: 16 }}><div className="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" style={{ width: `${progress}%` }} aria-valuenow={progress} aria-valuemin="0" aria-valuemax="100" /></div>
      <div className="d-flex flex-wrap justify-content-between mt-2 small text-muted"><span>{progressStatus?.phase || 'STARTING'}</span><span>Records verified: {totalRecords}</span><span>{Math.round(progress)}%</span></div>
    </div></div>}

    {summary && <><AnalyticsSummaryCards summary={summary} /><div className="mt-4"><h2 className="h5 mb-3">Analytic Summary Aggregates</h2><AnalyticsAggregateCards summary={summary} /></div></>}

    <div className="row g-4 mt-1">
      <div className="col-xl-6"><div className="card dashboard-card h-100"><div className="card-header">Analytics trend</div><div className="card-body"><AnalyticsTrendChart summaries={summaryHistory} /></div></div></div>
      <div className="col-xl-6"><div className="card dashboard-card h-100"><div className="card-header">Top domains · selected period</div><div className="card-body"><TopDomainsBarChart domains={topDomains} /></div></div></div>
      <div className="col-xl-6"><div className="card dashboard-card h-100"><div className="card-header">Tag distribution · selected period</div><div className="card-body"><TagDistributionChart distribution={tagDistribution} /></div></div></div>
      <div className="col-xl-6"><div className="card dashboard-card h-100"><div className="card-header">Browsing behaviour categories</div><div className="card-body"><CategoryRadarChart data={selectedCategoryData} /><div className="small text-muted mt-2">Counts are based on synchronized Chrome visit events and the category assigned to each URL.</div></div></div></div>
    </div>

    <div className="card dashboard-card mt-4"><div className="card-header">Current data snapshot</div><div className="card-body"><div className="row g-3">
      <div className="col-6 col-md-3"><div className="snapshot-tile"><span>Stored summaries</span><strong>{summaryHistory.length}</strong></div></div>
      <div className="col-6 col-md-3"><div className="snapshot-tile"><span>Tags</span><strong>{tags.length}</strong></div></div>
      <div className="col-6 col-md-3"><div className="snapshot-tile"><span>Top domains returned</span><strong>{topDomains.length}</strong></div></div>
      <div className="col-6 col-md-3"><div className="snapshot-tile"><span>Min / Avg / Max visits per domain</span><strong>{visitStats ? `${visitStats.minVisits ?? 0} / ${Number(visitStats.avgVisits ?? 0).toFixed(1)} / ${visitStats.maxVisits ?? 0}` : 'N/A'}</strong></div></div>
    </div></div></div>
  </div>;
}
