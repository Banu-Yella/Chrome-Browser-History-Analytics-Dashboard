const pad = (n) => String(n).padStart(2, '0');

export const PERIOD_OPTIONS = [
  { value: 'DAILY', label: 'Day' },
  { value: 'WEEKLY', label: 'Week' },
  { value: 'MONTHLY', label: 'Month' },
  { value: 'QUARTERLY', label: 'Quarter' },
  { value: 'HALF_YEARLY', label: 'Half Year' },
  { value: 'YEARLY', label: 'Year' },
];

export const formatInputDate = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
export const formatInputMonth = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}`;
export const toDateOnly = (value) => {
  if (!value) return null;
  const d = new Date(`${value}T00:00:00`);
  return Number.isNaN(d.getTime()) ? null : d;
};
export const toMonthDate = (value) => value ? toDateOnly(`${value}-01`) : null;
export const getMonday = (date) => {
  const d = new Date(date);
  const day = d.getDay();
  d.setDate(d.getDate() + (day === 0 ? -6 : 1 - day));
  return d;
};
const addDays = (date, days) => { const d = new Date(date); d.setDate(d.getDate() + days); return d; };
const addMonths = (date, months) => { const d = new Date(date); d.setMonth(d.getMonth() + months); return d; };
const lastDayOfMonth = (date) => new Date(date.getFullYear(), date.getMonth() + 1, 0);

export function buildPeriodSelection(periodType, value) {
  if (periodType === 'DAILY') {
    const d = toDateOnly(value);
    return d ? { start: d, end: d, startDate: formatInputDate(d), endDate: formatInputDate(d), referenceDate: formatInputDate(d), label: formatInputDate(d) } : null;
  }

  if (periodType === 'WEEKLY') {
    const d = toDateOnly(value);
    if (!d) return null;
    const start = getMonday(d);
    const end = addDays(start, 6);
    return { start, end, startDate: formatInputDate(start), endDate: formatInputDate(end), referenceDate: formatInputDate(start), label: `${formatInputDate(start)} → ${formatInputDate(end)}` };
  }

  if (periodType === 'MONTHLY') {
    const start = toMonthDate(value);
    if (!start) return null;
    const end = lastDayOfMonth(start);
    return { start, end, startDate: formatInputDate(start), endDate: formatInputDate(end), referenceDate: formatInputDate(start), label: `${formatInputDate(start)} → ${formatInputDate(end)}` };
  }

  if (periodType === 'QUARTERLY') {
    const start = toMonthDate(value);
    if (!start) return null;
    const end = lastDayOfMonth(addMonths(start, 2));
    return { start, end, startDate: formatInputDate(start), endDate: formatInputDate(end), referenceDate: formatInputDate(start), label: `3 months · ${formatInputMonth(start)} → ${formatInputMonth(end)}` };
  }

  if (periodType === 'HALF_YEARLY') {
    const start = toMonthDate(value);
    if (!start) return null;
    const end = lastDayOfMonth(addMonths(start, 5));
    return { start, end, startDate: formatInputDate(start), endDate: formatInputDate(end), referenceDate: formatInputDate(start), label: `6 months · ${formatInputMonth(start)} → ${formatInputMonth(end)}` };
  }

  if (periodType === 'YEARLY') {
    const y = Number(value);
    if (!y) return null;
    const start = new Date(y, 0, 1);
    const end = new Date(y, 11, 31);
    return { start, end, startDate: formatInputDate(start), endDate: formatInputDate(end), referenceDate: formatInputDate(start), label: `${y} · ${formatInputDate(start)} → ${formatInputDate(end)}` };
  }

  return null;
}

export function defaultPeriodValue(periodType) {
  const now = new Date();
  if (periodType === 'DAILY' || periodType === 'WEEKLY') return formatInputDate(now);
  if (periodType === 'MONTHLY' || periodType === 'QUARTERLY' || periodType === 'HALF_YEARLY') return formatInputMonth(now);
  return String(now.getFullYear());
}

export function buildPeriodOptions(periodType, startYear = 2022, endYear = new Date().getFullYear() + 2) {
  const list = [];
  for (let y = endYear; y >= startYear; y -= 1) {
    if (periodType === 'YEARLY') list.push({ value: String(y), label: String(y) });
  }
  return list;
}

export const getPeriodLabel = (periodType) => PERIOD_OPTIONS.find((option) => option.value === periodType)?.label || periodType || 'Period';
