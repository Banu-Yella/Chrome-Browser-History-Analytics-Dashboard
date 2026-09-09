import { PolarAngleAxis, PolarGrid, PolarRadiusAxis, Radar, RadarChart, ResponsiveContainer, Tooltip } from 'recharts';
export default function CategoryRadarChart({ data = [] }) {
  const safeData = Array.isArray(data) ? data.filter((item) => Number(item.value) > 0) : [];
  if (!safeData.length) return <p className="text-muted mb-0">No category data available yet.</p>;
  return <ResponsiveContainer width="100%" height={340}><RadarChart data={safeData} outerRadius="72%"><PolarGrid /><PolarAngleAxis dataKey="name" tick={{ fontSize: 11 }} /><PolarRadiusAxis /><Radar dataKey="value" name="Tagged visits" fill="#1b2a4a" fillOpacity={0.45} stroke="#1b2a4a" /><Tooltip /></RadarChart></ResponsiveContainer>;
}
