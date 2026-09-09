export default function TagTable({ tags = [], onEdit, onDelete }) {
  const safeTags = Array.isArray(tags) ? tags : [];
  if (!safeTags.length) return <p className="text-muted p-4 mb-0">No tags yet — create one above.</p>;
  return <div className="table-responsive"><table className="table table-hover align-middle mb-0 tag-record-table"><thead><tr><th>Name</th><th>Category</th><th>Keywords</th><th>Description</th><th className="text-end">Actions</th></tr></thead><tbody>{safeTags.map((tag) => <tr key={tag.tag_Id}><td className="fw-semibold">{tag.tag_Name}</td><td>{tag.category || '—'}</td><td className="tag-keywords"><code className="small">{tag.matchKeywords}</code></td><td>{tag.description || '—'}</td><td className="text-end text-nowrap"><button className="btn btn-sm btn-outline-primary me-2" onClick={() => onEdit(tag)}>Edit</button><button className="btn btn-sm btn-outline-danger" onClick={() => onDelete(tag.tag_Id)}>Delete</button></td></tr>)}</tbody></table></div>;
}
