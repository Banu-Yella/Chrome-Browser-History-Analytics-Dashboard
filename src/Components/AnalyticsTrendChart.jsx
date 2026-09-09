import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { formatDate, formatSeconds } from '../utils/format';
export default function AnalyticsTrendChart({ summaries = [] }) {
  const data = (Array.isArray(summaries) ? [...summaries] : []).sort((a, b) => new Date(a.periodStart) - new Date(b.periodStart)).map((s) => ({ period: formatDate(s.periodStart), totalVisits: Number(s.totalVisits || 0), uniqueDomains: Number(s.uniqueDomains || 0), averageTime: Number(s.averageTimeSpent || 0) }));
  if (!data.length) return <p className="text-muted mb-0">No analytics history yet.</p>;
  return <ResponsiveContainer width="100%" height={340}><LineChart data={data}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="period" /><YAxis /><Tooltip formatter={(value, key) => key === 'averageTime' ? [formatSeconds(value), 'Avg time / visit'] : [value, key === 'totalVisits' ? 'Total visits' : 'Unique domains']} /><Legend /><Line type="monotone" dataKey="totalVisits" name="Total Visits" stroke="#1b2a4a" strokeWidth={2} /><Line type="monotone" dataKey="uniqueDomains" name="Unique Domains" stroke="#d9822b" strokeWidth={2} /></LineChart></ResponsiveContainer>;
}
