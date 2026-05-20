import { SCORE_BANDS } from '../utils/statusHelp';
import { normalizeDisplayText } from '../utils/formatDisplay';

type Props = {
  open: boolean;
  onClose: () => void;
  name: string;
  matchPercent?: number;
  matchingSkills?: string;
  missingSkills?: string;
  jobTitle?: string;
};

export default function ScoreExplainDrawer({
  open,
  onClose,
  name,
  matchPercent,
  matchingSkills,
  missingSkills,
  jobTitle
}: Props) {
  if (!open) return null;

  const matching = matchingSkills?.split(/[,;]/).map((s) => s.trim()).filter(Boolean) ?? [];
  const missing = missingSkills?.split(/[,;]/).map((s) => s.trim()).filter(Boolean) ?? [];

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button type="button" className="absolute inset-0 bg-slate-950/40" aria-label="Close" onClick={onClose} />
      <aside className="relative flex h-full w-full max-w-md flex-col border-l border-slate-200 bg-white shadow-2xl dark:border-slate-800 dark:bg-slate-900">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4 dark:border-slate-800">
          <h2 className="text-lg font-bold text-slate-900 dark:text-white">Why this score?</h2>
          <button type="button" onClick={onClose} className="text-sm font-semibold text-slate-500">
            Close
          </button>
        </div>
        <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
          <p className="text-sm text-slate-600 dark:text-slate-400">
            <span className="font-semibold text-slate-900 dark:text-white">{normalizeDisplayText(name)}</span>
            {jobTitle ? ` vs ${jobTitle}` : ''}
          </p>
          {matchPercent != null ? (
            <div className="rounded-xl bg-brand-50 p-4 text-center dark:bg-brand-950/50">
              <div className="text-3xl font-bold text-brand-700 dark:text-brand-300">{matchPercent.toFixed(1)}%</div>
              <div className="text-xs font-semibold uppercase tracking-wider text-slate-500">Overall match</div>
            </div>
          ) : null}
          <div>
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500">Score bands</h3>
            <ul className="mt-2 space-y-2 text-sm">
              {SCORE_BANDS.map((b) => (
                <li key={b.label} className="rounded-lg border border-slate-100 px-3 py-2 dark:border-slate-800">
                  <span className="font-semibold">{b.label}</span> — {b.desc}
                </li>
              ))}
            </ul>
          </div>
          {matching.length > 0 ? (
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-emerald-700">Aligned</h3>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {matching.map((t) => (
                  <span key={t} className="rounded-md bg-emerald-50 px-2 py-0.5 text-xs text-emerald-800 dark:bg-emerald-950/50 dark:text-emerald-300">
                    {t}
                  </span>
                ))}
              </div>
            </div>
          ) : null}
          {missing.length > 0 ? (
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-rose-700">Gaps</h3>
              <div className="mt-2 flex flex-wrap gap-1.5">
                {missing.map((t) => (
                  <span key={t} className="rounded-md bg-rose-50 px-2 py-0.5 text-xs text-rose-800 dark:bg-rose-950/50 dark:text-rose-300">
                    {t}
                  </span>
                ))}
              </div>
            </div>
          ) : null}
        </div>
      </aside>
    </div>
  );
}
