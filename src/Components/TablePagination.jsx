const DEFAULT_OPTIONS = [25, 50, 100, 1000, 'ALL'];

export default function TablePagination({
  page,
  pageSize,
  total,
  onPageChange,
  onPageSizeChange,
  options = DEFAULT_OPTIONS,
}) {
  const normalizedSize = pageSize === 'ALL' ? Math.max(total, 1) : Number(pageSize) || 25;
  const pageCount = Math.max(1, Math.ceil(total / normalizedSize));
  const currentPage = Math.min(Math.max(1, Number(page) || 1), pageCount);
  const start = total === 0 ? 0 : (currentPage - 1) * normalizedSize + 1;
  const end = total === 0 ? 0 : Math.min(currentPage * normalizedSize, total);

  return (
    <div className="table-pagination d-flex flex-wrap align-items-center justify-content-between gap-3 p-3 border-top">
      <div className="d-flex flex-wrap align-items-center gap-2 small text-muted">
        <span>Showing {start}–{end} of {total}</span>
        <span className="text-body-secondary">|</span>
        <label className="d-flex align-items-center gap-2 mb-0">
          <span>Rows</span>
          <select
            className="form-select form-select-sm table-page-size"
            value={pageSize}
            onChange={(event) => onPageSizeChange(event.target.value === 'ALL' ? 'ALL' : Number(event.target.value))}
          >
            {options.map((option) => (
              <option key={String(option)} value={option}>{option === 'ALL' ? 'All' : option}</option>
            ))}
          </select>
        </label>
      </div>

      <div className="d-flex align-items-center gap-2">
        <span className="small text-muted">Page {currentPage} of {pageCount}</span>
        <div className="btn-group btn-group-sm" role="group" aria-label="Table pagination">
          <button type="button" className="btn btn-outline-secondary" disabled={currentPage <= 1} onClick={() => onPageChange(currentPage - 1)}>
            Previous
          </button>
          <button type="button" className="btn btn-outline-secondary" disabled={currentPage >= pageCount} onClick={() => onPageChange(currentPage + 1)}>
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
