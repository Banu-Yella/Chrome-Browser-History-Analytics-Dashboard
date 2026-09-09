import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

// Mirrors Tag_Entity's @NotBlank/@Size constraints — catches the same errors
// client-side that the backend would reject, without a round-trip.
const tagSchema = z.object({
  tagName: z.string().min(1, 'Tag name is required').max(100, 'Max 100 characters'),
  description: z.string().max(500, 'Max 500 characters').optional().or(z.literal('')),
  category: z.string().max(100, 'Max 100 characters').optional().or(z.literal('')),
  matchKeywords: z.string().min(1, 'At least one keyword is required (comma-separated)'),
});

const EMPTY_VALUES = { tagName: '', description: '', category: '', matchKeywords: '' };

export default function TagForm({ initialValues, onSubmit, onCancel, isSaving }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(tagSchema),
    defaultValues: initialValues || EMPTY_VALUES,
  });

  useEffect(() => {
    reset(initialValues || EMPTY_VALUES);
  }, [initialValues, reset]);

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="card card-body mb-4">
      <div className="row g-3">
        <div className="col-md-6">
          <label className="form-label">Tag Name</label>
          <input className="form-control" {...register('tagName')} />
          {errors.tagName && <div className="text-danger small">{errors.tagName.message}</div>}
        </div>

        <div className="col-md-6">
          <label className="form-label">Category</label>
          <input className="form-control" {...register('category')} placeholder="e.g. Dev Tools" />
          {errors.category && <div className="text-danger small">{errors.category.message}</div>}
        </div>

        <div className="col-12">
          <label className="form-label">Description</label>
          <input className="form-control" {...register('description')} />
          {errors.description && <div className="text-danger small">{errors.description.message}</div>}
        </div>

        <div className="col-12">
          <label className="form-label">Match Keywords (comma-separated)</label>
          <input
            className="form-control"
            {...register('matchKeywords')}
            placeholder="github, stackoverflow, leetcode"
          />
          {errors.matchKeywords && <div className="text-danger small">{errors.matchKeywords.message}</div>}
          <div className="form-text">
            Any page whose domain, title, or URL contains one of these gets auto-tagged.
          </div>
        </div>
      </div>

      <div className="mt-3 d-flex gap-2">
        <button type="submit" className="btn btn-primary" disabled={isSaving}>
          {isSaving ? 'Saving…' : initialValues ? 'Update Tag' : 'Create Tag'}
        </button>
        {initialValues && (
          <button type="button" className="btn btn-outline-secondary" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
