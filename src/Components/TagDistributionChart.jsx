import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';

const COLORS = [
  '#1b2a4a', '#d9822b', '#3f7d5c', '#a3374a', '#6a5acd',
  '#2f9e9e', '#c98a2c', '#516b84', '#7a5a41', '#4c6f91',
];

export default function TagDistributionChart({ distribution }) {
  const data = Object.entries(distribution || {})
    .map(([name, value]) => ({
      name,
      value: Number(value || 0),
    }))
    .filter((item) => item.value > 0)
    .sort((a, b) => b.value - a.value);

  if (!data.length) {
    return <p className="text-muted mb-0">No tagged history yet.</p>;
  }

  const total = data.reduce((sum, item) => sum + item.value, 0);

  return (
    <ResponsiveContainer width="100%" height={390}>
      <PieChart margin={{ top: 10, right: 10, bottom: 18, left: 10 }}>
        <Pie
          data={data}
          dataKey="value"
          nameKey="name"
          cx="42%"
          cy="44%"
          innerRadius={52}
          outerRadius={112}
          paddingAngle={1}
          stroke="#fff"
          strokeWidth={1}
          labelLine={false}
          label={({ index, value }) => (index < 5 ? value : '')}
        >
          {data.map((entry, index) => (
            <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>

        <Tooltip
          formatter={(value, name) => {
            const numericValue = Number(value || 0);
            const percent = total ? ((numericValue / total) * 100).toFixed(1) : '0.0';
            return [`${numericValue} (${percent}%)`, name];
          }}
        />

        <Legend
          layout="vertical"
          align="right"
          verticalAlign="middle"
          wrapperStyle={{
            width: '44%',
            maxHeight: 320,
            overflowY: 'auto',
            fontSize: 11,
            lineHeight: '18px',
          }}
        />
      </PieChart>
    </ResponsiveContainer>
  );
}
