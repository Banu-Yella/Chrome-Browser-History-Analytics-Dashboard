import { useEffect, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import TagForm from '../components/TagForm';
import TagTable from '../components/TagTable';
import TablePagination from '../components/TablePagination';
import { getAllTags, createTag, updateTag, deleteTag } from '../api/tagApi';

export default function TagManagerPage() {
  const [tags, setTags] = useState([]);
  const [editingTag, setEditingTag] = useState(null);
  const [isSaving, setIsSaving] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(25);

  const loadTags = async () => setTags(await getAllTags());
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => { loadTags().catch((err) => toast.error(err.response?.data?.error || err.message || 'Unable to load tags.')); }, []);

  const normalizedSize = pageSize === 'ALL' ? Math.max(tags.length, 1) : pageSize;
  const pageCount = Math.max(1, Math.ceil(tags.length / normalizedSize));
  const currentPage = Math.min(page, pageCount);
  const visibleTags = useMemo(() => tags.slice((currentPage - 1) * normalizedSize, currentPage * normalizedSize), [tags, currentPage, normalizedSize]);

  const handleSubmit = async (values) => {
    setIsSaving(true);
    try {
      if (editingTag) { await updateTag(editingTag.tag_Id, values); toast.success(`Tag "${values.tagName}" updated.`); }
      else { await createTag(values); toast.success(`Tag "${values.tagName}" created.`); }
      setEditingTag(null); await loadTags();
    } catch (err) { toast.error(err.response?.data?.error || err.message || 'Unable to save tag.'); }
    finally { setIsSaving(false); }
  };

  const handleDelete = async (id) => {
    const tag = tags.find((item) => item.tag_Id === id);
    if (!window.confirm(`Delete tag "${tag?.tag_Name || id}"? This also removes its links from tagged history.`)) return;
    try { await deleteTag(id); toast.success(`Tag "${tag?.tag_Name || id}" deleted.`); await loadTags(); }
    catch (err) { toast.error(err.response?.data?.error || err.message || 'Unable to delete tag.'); }
  };

  const editValues = editingTag ? { tagName: editingTag.tag_Name, description: editingTag.description || '', category: editingTag.category || '', matchKeywords: editingTag.matchKeywords } : null;
  return <div className="app-page app-page-wide">
    <div className="page-heading mb-4"><div className="eyebrow">TAG TAXONOMY</div><h1 className="h3 mb-1">Tags</h1><p className="text-muted mb-0">Create the keyword taxonomy used by the backend auto-tagging service.</p></div>
    <TagForm initialValues={editValues} onSubmit={handleSubmit} onCancel={() => setEditingTag(null)} isSaving={isSaving} />
    <div className="card dashboard-card">
      <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2"><span>Tag records</span><span className="small text-muted">{tags.length} total tags</span></div>
      <TagTable tags={visibleTags} onEdit={setEditingTag} onDelete={handleDelete} />
      <TablePagination page={currentPage} pageSize={pageSize} total={tags.length} onPageChange={setPage} onPageSizeChange={(size) => { setPageSize(size); setPage(1); }} />
    </div>
  </div>;
}
