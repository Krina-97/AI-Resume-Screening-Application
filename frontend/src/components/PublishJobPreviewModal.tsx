type Props = {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  department: string;
  location: string;
  experienceRequired: string;
  requiredSkills: string[];
  preferredSkills: string[];
  description: string;
  publishing: boolean;
};

export default function PublishJobPreviewModal({
  open,
  onClose,
  onConfirm,
  title,
  department,
  location,
  experienceRequired,
  requiredSkills,
  preferredSkills,
  description,
  publishing
}: Props) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button type="button" className="absolute inset-0 bg-slate-950/50" aria-label="Close" onClick={onClose} />
      <div className="relative z-10 max-h-[85vh] w-full max-w-2xl overflow-y-auto rounded-2xl border border-slate-200 bg-white p-6 shadow-2xl dark:border-slate-800 dark:bg-slate-900">
        <h2 className="text-lg font-bold text-slate-900 dark:text-white">Preview before publish</h2>
        <dl className="mt-4 space-y-2 text-sm">
          <div>
            <dt className="font-semibold text-slate-500">Title</dt>
            <dd className="text-slate-900 dark:text-white">{title}</dd>
          </div>
          <div className="flex flex-wrap gap-2">
            {department ? <span className="rounded-lg bg-slate-100 px-2 py-1 text-xs dark:bg-slate-800">{department}</span> : null}
            {location ? <span className="rounded-lg bg-slate-100 px-2 py-1 text-xs dark:bg-slate-800">{location}</span> : null}
            {experienceRequired ? (
              <span className="rounded-lg bg-slate-100 px-2 py-1 text-xs dark:bg-slate-800">{experienceRequired}</span>
            ) : null}
          </div>
          {requiredSkills.length > 0 ? (
            <div>
              <dt className="font-semibold text-slate-500">Required skills</dt>
              <dd className="mt-1 flex flex-wrap gap-1">
                {requiredSkills.map((s) => (
                  <span key={s} className="rounded bg-brand-50 px-2 py-0.5 text-xs text-brand-800 dark:bg-brand-950/50 dark:text-brand-300">
                    {s}
                  </span>
                ))}
              </dd>
            </div>
          ) : null}
          <div>
            <dt className="font-semibold text-slate-500">Description excerpt</dt>
            <dd className="mt-1 max-h-40 overflow-y-auto whitespace-pre-wrap text-slate-700 dark:text-slate-300">
              {description.slice(0, 800)}
              {description.length > 800 ? '…' : ''}
            </dd>
          </div>
        </dl>
        <div className="mt-6 flex justify-end gap-2">
          <button type="button" onClick={onClose} className="rounded-xl border px-4 py-2 text-sm font-semibold">
            Edit
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={publishing}
            className="rounded-xl bg-brand-600 px-4 py-2 text-sm font-bold text-white disabled:opacity-50"
          >
            {publishing ? 'Publishing…' : 'Publish job'}
          </button>
        </div>
      </div>
    </div>
  );
}
