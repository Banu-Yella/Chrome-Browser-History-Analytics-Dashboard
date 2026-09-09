import { PERIOD_OPTIONS, buildPeriodSelection, defaultPeriodValue, buildPeriodOptions } from '../utils/period';

export default function PeriodSelector({ value, onChange, disabled }) {
  const periodType = value?.periodType || 'DAILY';
  const selection = value?.selection || defaultPeriodValue(periodType);
  const resolved = buildPeriodSelection(periodType, selection);
  const yearOptions = buildPeriodOptions(periodType);

  const emit = (type, nextSelection) => onChange({
    periodType: type,
    selection: nextSelection,
    resolved: buildPeriodSelection(type, nextSelection),
  });

  let control;
  if (periodType === 'DAILY') {
    control = <><label className="form-label mb-1">Date</label><input type="date" className="form-control" value={selection} disabled={disabled} onChange={(e) => emit(periodType, e.target.value)} /><div className="form-text">Select the exact calendar day.</div></>;
  } else if (periodType === 'WEEKLY') {
    control = <><label className="form-label mb-1">Week start / any date</label><input type="date" className="form-control" value={selection} disabled={disabled} onChange={(e) => emit(periodType, e.target.value)} /><div className="form-text">The selected date is normalized to a Monday–Sunday seven-day range.</div></>;
  } else if (periodType === 'MONTHLY' || periodType === 'QUARTERLY' || periodType === 'HALF_YEARLY') {
    const hint = periodType === 'MONTHLY' ? 'One calendar month.' : periodType === 'QUARTERLY' ? 'Choose any starting month; the range covers exactly three months.' : 'Choose any starting month; the range covers exactly six months.';
    control = <><label className="form-label mb-1">Starting month</label><input type="month" className="form-control" value={selection} disabled={disabled} onChange={(e) => emit(periodType, e.target.value)} /><div className="form-text">{hint}</div></>;
  } else {
    control = <><label className="form-label mb-1">Year</label><select className="form-select" value={selection} disabled={disabled} onChange={(e) => emit(periodType, e.target.value)}>{yearOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></>;
  }

  return <div className="period-selector card border-0 bg-light-subtle"><div className="card-body"><div className="row g-3 align-items-end">
    <div className="col-md-4 col-lg-3"><label className="form-label mb-1">Period</label><select className="form-select" value={periodType} disabled={disabled} onChange={(e) => emit(e.target.value, defaultPeriodValue(e.target.value))}>{PERIOD_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></div>
    <div className="col-md-5 col-lg-4">{control}</div>
    <div className="col-md-7 col-lg-5"><label className="form-label mb-1">Resolved range</label><div className="resolved-range"><strong className="me-2">{resolved?.label || 'Choose a period.'}</strong>{resolved && <span className="small text-muted">({resolved.startDate} → {resolved.endDate})</span>}</div></div>
  </div></div></div>;
}
