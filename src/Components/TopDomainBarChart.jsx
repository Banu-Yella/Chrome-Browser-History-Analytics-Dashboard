import { ResponsiveContainer, BarChart, Bar, CartesianGrid, XAxis, YAxis, Tooltip } from 'recharts';
export default function TopDomainsBarChart({ domains = [] }) {
  const data = (Array.isArray(domains) ? domains : []).slice(0, 10).map((d) => ({ domain: d.domain, visits: Number(d.totalVisits || 0) }));
  if (!data.length) return <p className="text-muted mb-0">No domain data available yet.</p>;
  return <ResponsiveContainer width="100%" height={330}><BarChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 70 }}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="domain" angle={-35} textAnchor="end" interval={0} /><YAxis allowDecimals={false} /><Tooltip /><Bar dataKey="visits" name="Visits" fill="#1b2a4a" radius={[4, 4, 0, 0]} /></BarChart></ResponsiveContainer>;
}
