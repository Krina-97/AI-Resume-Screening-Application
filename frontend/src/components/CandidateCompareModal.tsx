import { useEffect, useState } from 'react';
import api from '../services/api';
import CandidateOverviewCell from './CandidateOverviewCell';
import { formatPersonName } from '../utils/formatDisplay';

type Candidate = {
  id: number;
  fullName: string;
  latestMatchScore?: number;
  overview?: unknown;
};

type Props = {
  ids: number[];
  open: boolean;
  onClose: () => void;
};

export default function CandidateCompareModal({ ids, open, onClose }: Props) {
  const [rows, setRows] = useState<Candidate[]>([]);

  useEffect(() => {
    if (!open || ids.length < 2) return;
    Promise.all(ids.slice(0, 2).map((id) => api.get<Candidate>(`/candidates/${id}`).then((r) => r.data))).then(setRows);
  }, [open, ids]);

  if (!open || ids.length < 2) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button type="button" className="absolute inset-0 bg-slate-950/50" aria-label="Close" onClick={onClose} />
      <div className="relative z-10 max-h-[90vh] w-full max-w-5xl overflow-y-auto rounded-2xl border border-slate-200 bg-white p-6 shadow-2xl dark:border-slate-800 dark:bg-slate-900">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-slate-900 dark:text-white">Compare candidates</h2>
          <button type="button" onClick={onClose} className="text-sm font-semibold text-slate-500">
            Close
          </button>
        </div>
        <div className="mt-4 grid gap-4 lg:grid-cols-2">
          {rows.map((c) => (
            <div key={c.id} className="rounded-xl border border-slate-200 p-4 dark:border-slate-700">
              <div className="mb-3 flex items-center justify-between">
                <span className="font-bold text-slate-900 dark:text-white">{formatPersonName(c.fullName)}</span>
                <span className="rounded-lg bg-brand-50 px-2 py-1 text-sm font-bold text-brand-700 dark:bg-brand-950/50 dark:text-brand-300">
                  {c.latestMatchScore != null ? `${c.latestMatchScore.toFixed(1)}%` : '—'}
                </span>
              </div>
              <CandidateOverviewCell overview={c.overview as never} defaultCollapsed={false} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
