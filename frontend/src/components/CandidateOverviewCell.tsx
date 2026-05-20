import { useState } from 'react';
import { normalizeDisplayText } from '../utils/formatDisplay';

type CandidateOverview = {
  executiveSummary?: string;
  summary?: string;
  interviewVerdict?: string;
  interviewHeadline?: string;
  interviewPros?: string[];
  interviewConcerns?: string[];
  matchPercent?: number;
  jobTitle?: string;
  workExperience?: string;
  skills?: string;
  strengths?: string;
  roleFit?: string;
};

function splitTags(value?: string) {
  if (!value?.trim()) return [];
  return value
    .split(/[,;]/)
    .map((s) => s.trim())
    .filter(Boolean);
}

function verdictStyles(verdict?: string) {
  const v = verdict?.toUpperCase() ?? 'HOLD';
  if (v === 'INTERVIEW') {
    return {
      badge: 'bg-emerald-100 text-emerald-900 ring-emerald-500/30 dark:bg-emerald-950/60 dark:text-emerald-200',
      border: 'border-emerald-200 dark:border-emerald-800/80',
      icon: '✓',
      label: 'Recommend interview',
    };
  }
  if (v === 'DO_NOT_INTERVIEW') {
    return {
      badge: 'bg-rose-100 text-rose-900 ring-rose-500/30 dark:bg-rose-950/60 dark:text-rose-200',
      border: 'border-rose-200 dark:border-rose-800/80',
      icon: '✕',
      label: 'Not recommended',
    };
  }
  return {
    badge: 'bg-amber-100 text-amber-900 ring-amber-500/30 dark:bg-amber-950/60 dark:text-amber-200',
    border: 'border-amber-200 dark:border-amber-800/80',
    icon: '◐',
    label: 'Hold — review further',
  };
}

export default function CandidateOverviewCell({
  overview,
  fallbackSummary,
  defaultCollapsed = true,
  compact = false
}: {
  overview?: CandidateOverview | null;
  fallbackSummary?: string;
  defaultCollapsed?: boolean;
  compact?: boolean;
}) {
  const [expanded, setExpanded] = useState(!defaultCollapsed);
  if (!overview) {
    return (
      <p className="text-sm leading-relaxed text-slate-600 dark:text-slate-300">
        {normalizeDisplayText(fallbackSummary)?.trim() || '—'}
      </p>
    );
  }

  const executiveSummary = normalizeDisplayText(
    overview.executiveSummary ?? overview.summary ?? fallbackSummary
  );
  const verdict = overview.interviewVerdict ?? 'HOLD';
  const styles = verdictStyles(verdict);
  const headline = normalizeDisplayText(overview.interviewHeadline);
  const pros = (overview.interviewPros ?? []).map((p) => normalizeDisplayText(p)).filter(Boolean);
  const concerns = (overview.interviewConcerns ?? []).map((c) => normalizeDisplayText(c)).filter(Boolean);
  const skillTags = splitTags(normalizeDisplayText(overview.skills));
  const strengthTags = splitTags(normalizeDisplayText(overview.strengths));
  const workExperience = normalizeDisplayText(overview.workExperience);

  const verdictPreview = overview.interviewHeadline ?? overview.executiveSummary?.slice(0, 120);

  if (defaultCollapsed && !expanded) {
    return (
      <div className="space-y-2">
        <div className="flex flex-wrap items-center gap-2">
          {overview.matchPercent != null ? (
            <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-brand-600 text-xs font-bold text-white">
              {overview.matchPercent.toFixed(0)}%
            </span>
          ) : null}
          <span
            className={`rounded-full px-2 py-0.5 text-[10px] font-bold uppercase ${verdictStyles(overview.interviewVerdict).badge}`}
          >
            {verdictStyles(overview.interviewVerdict).label}
          </span>
          {verdictPreview ? (
            <span className="min-w-0 flex-1 text-xs text-slate-600 dark:text-slate-400 line-clamp-2">{verdictPreview}</span>
          ) : null}
          <button
            type="button"
            onClick={() => setExpanded(true)}
            className="shrink-0 text-xs font-semibold text-brand-600 hover:underline dark:text-brand-400"
          >
            Expand overview
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={`space-y-3 ${compact ? 'text-xs' : ''}`}>
      {defaultCollapsed ? (
        <button
          type="button"
          onClick={() => setExpanded(false)}
          className="text-xs font-semibold text-slate-500 hover:text-brand-600"
        >
          Collapse overview
        </button>
      ) : null}
      <div className="flex flex-wrap items-center gap-2 rounded-xl border border-slate-200/90 bg-slate-50/80 px-3 py-2.5 dark:border-slate-700 dark:bg-slate-800/50">
        {overview.matchPercent != null ? (
          <div
            className="flex h-11 w-11 shrink-0 flex-col items-center justify-center rounded-full bg-brand-600 text-center text-white shadow-md shadow-brand-500/30"
            title="Match score"
          >
            <span className="text-sm font-bold leading-none">{overview.matchPercent.toFixed(0)}</span>
            <span className="text-[9px] font-semibold uppercase opacity-90">%</span>
          </div>
        ) : null}
        <div className="min-w-0 flex-1">
          {overview.jobTitle ? (
            <div className="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Evaluated for
            </div>
          ) : null}
          <div className="truncate text-sm font-semibold text-slate-900 dark:text-white">
            {overview.jobTitle ?? 'Target job'}
          </div>
        </div>
        <span
          className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-bold ring-1 ring-inset ${styles.badge}`}
        >
          <span aria-hidden>{styles.icon}</span>
          {styles.label}
        </span>
      </div>

      {executiveSummary ? (
        <div className="rounded-xl border border-slate-200/90 bg-white p-3.5 shadow-sm dark:border-slate-700 dark:bg-slate-900/60">
          <div className="mb-2 flex items-center gap-2">
            <span className="flex h-6 w-6 items-center justify-center rounded-lg bg-brand-100 text-xs font-bold text-brand-700 dark:bg-brand-950 dark:text-brand-300">
              CV
            </span>
            <span className="text-xs font-bold uppercase tracking-wider text-slate-600 dark:text-slate-300">
              Executive summary
            </span>
          </div>
          <p className="text-sm leading-relaxed text-slate-700 dark:text-slate-200">{executiveSummary}</p>
        </div>
      ) : null}

      <div className={`rounded-xl border-2 bg-white p-3.5 shadow-sm dark:bg-slate-900/60 ${styles.border}`}>
        <div className="mb-3 flex items-start gap-2">
          <span
            className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-sm font-bold ${styles.badge}`}
          >
            {styles.icon}
          </span>
          <div className="min-w-0">
            <div className="text-xs font-bold uppercase tracking-wider text-slate-600 dark:text-slate-300">
              Interview recommendation
            </div>
            {headline ? (
              <p className="mt-1 text-sm font-medium leading-snug text-slate-800 dark:text-slate-100">{headline}</p>
            ) : null}
          </div>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          {pros.length > 0 ? (
            <div className="rounded-lg bg-emerald-50/80 p-3 dark:bg-emerald-950/30">
              <div className="mb-2 text-[10px] font-bold uppercase tracking-wider text-emerald-800 dark:text-emerald-300">
                Why interview
              </div>
              <ul className="space-y-1.5">
                {pros.map((item) => (
                  <li key={item} className="flex gap-2 text-xs leading-relaxed text-emerald-950 dark:text-emerald-100">
                    <span className="mt-0.5 shrink-0 font-bold text-emerald-600 dark:text-emerald-400">+</span>
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          ) : null}

          {concerns.length > 0 ? (
            <div className="rounded-lg bg-rose-50/80 p-3 dark:bg-rose-950/30">
              <div className="mb-2 text-[10px] font-bold uppercase tracking-wider text-rose-800 dark:text-rose-300">
                Why not / risks
              </div>
              <ul className="space-y-1.5">
                {concerns.map((item) => (
                  <li key={item} className="flex gap-2 text-xs leading-relaxed text-rose-950 dark:text-rose-100">
                    <span className="mt-0.5 shrink-0 font-bold text-rose-600 dark:text-rose-400">−</span>
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          ) : null}
        </div>
      </div>

      {(skillTags.length > 0 || strengthTags.length > 0 || workExperience) && (
        <details className="group rounded-xl border border-slate-200/80 bg-slate-50/50 dark:border-slate-700 dark:bg-slate-800/30">
          <summary className="cursor-pointer list-none px-3.5 py-2.5 text-xs font-bold uppercase tracking-wider text-slate-500 transition hover:text-brand-600 dark:text-slate-400 dark:hover:text-brand-400">
            <span className="inline-flex items-center gap-2">
              <span className="text-slate-400 transition group-open:rotate-90">▸</span>
              Resume details (skills, strengths, experience)
            </span>
          </summary>
          <div className="space-y-3 border-t border-slate-200/80 px-3.5 pb-3.5 pt-3 dark:border-slate-700">
            {skillTags.length > 0 ? (
              <div>
                <div className="mb-1.5 text-[10px] font-bold uppercase tracking-wider text-slate-500">Skills</div>
                <div className="flex flex-wrap gap-1.5">
                  {skillTags.map((tag) => (
                    <span
                      key={tag}
                      className="rounded-md bg-white px-2 py-0.5 text-xs font-medium text-slate-700 ring-1 ring-slate-200 dark:bg-slate-900 dark:text-slate-200 dark:ring-slate-600"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            ) : null}
            {strengthTags.length > 0 ? (
              <div>
                <div className="mb-1.5 text-[10px] font-bold uppercase tracking-wider text-slate-500">Strengths</div>
                <div className="flex flex-wrap gap-1.5">
                  {strengthTags.map((tag) => (
                    <span
                      key={tag}
                      className="rounded-md bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-800 dark:bg-emerald-950/50 dark:text-emerald-300"
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            ) : null}
            {workExperience ? (
              <div>
                <div className="mb-1.5 text-[10px] font-bold uppercase tracking-wider text-slate-500">
                  Work experience
                </div>
                <p className="whitespace-pre-line text-xs leading-relaxed text-slate-600 dark:text-slate-300">
                  {workExperience}
                </p>
              </div>
            ) : null}
          </div>
        </details>
      )}
    </div>
  );
}
