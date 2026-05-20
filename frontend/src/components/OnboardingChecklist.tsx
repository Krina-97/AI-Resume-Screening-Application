import { Link } from 'react-router-dom';
import { defaultOnboarding, ONBOARDING_KEY, type OnboardingState } from '../constants/onboarding';

type Props = {
  jobsCount: number;
  candidatesCount: number;
  onDismiss: () => void;
};

export function loadOnboarding(): OnboardingState {
  try {
    const raw = localStorage.getItem(ONBOARDING_KEY);
    if (raw) return { ...defaultOnboarding(), ...JSON.parse(raw) };
  } catch {
    /* ignore */
  }
  return defaultOnboarding();
}

export function saveOnboarding(state: OnboardingState) {
  localStorage.setItem(ONBOARDING_KEY, JSON.stringify(state));
}

export default function OnboardingChecklist({ jobsCount, candidatesCount, onDismiss }: Props) {
  const state = loadOnboarding();
  const jobDone = state.jobCreated || jobsCount > 0;
  const resumeDone = state.resumeUploaded || candidatesCount > 0;
  if (state.dismissed || (jobDone && resumeDone)) return null;

  return (
    <div className="rounded-2xl border border-brand-200 bg-gradient-to-r from-brand-50 to-indigo-50/80 p-5 shadow-soft dark:border-brand-800/60 dark:from-brand-950/40 dark:to-indigo-950/30">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-sm font-bold text-slate-900 dark:text-white">Get started in 3 steps</h2>
          <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">Complete setup to unlock the full hiring workflow.</p>
        </div>
        <button
          type="button"
          onClick={onDismiss}
          className="text-xs font-semibold text-slate-500 hover:text-slate-800 dark:hover:text-slate-200"
        >
          Dismiss
        </button>
      </div>
      <ol className="mt-4 grid gap-2 sm:grid-cols-3">
        <li className={`rounded-xl border px-3 py-3 text-sm ${jobDone ? 'border-emerald-200 bg-white dark:border-emerald-800' : 'border-slate-200 bg-white/80 dark:border-slate-700 dark:bg-slate-900/60'}`}>
          <span className="font-bold text-brand-600">1.</span> Publish a job
          {jobDone ? <span className="ml-2 text-emerald-600">✓</span> : null}
          {!jobDone ? (
            <Link to="/jobs" className="mt-1 block text-xs font-semibold text-brand-600 hover:underline">
              Go to Jobs →
            </Link>
          ) : null}
        </li>
        <li className={`rounded-xl border px-3 py-3 text-sm ${resumeDone ? 'border-emerald-200 bg-white dark:border-emerald-800' : 'border-slate-200 bg-white/80 dark:border-slate-700 dark:bg-slate-900/60'}`}>
          <span className="font-bold text-brand-600">2.</span> Upload a resume
          {resumeDone ? <span className="ml-2 text-emerald-600">✓</span> : null}
          {!resumeDone ? (
            <Link to="/candidates" className="mt-1 block text-xs font-semibold text-brand-600 hover:underline">
              Go to Candidates →
            </Link>
          ) : null}
        </li>
        <li className="rounded-xl border border-slate-200 bg-white/80 px-3 py-3 text-sm dark:border-slate-700 dark:bg-slate-900/60">
          <span className="font-bold text-brand-600">3.</span> Review AI match
          <p className="mt-1 text-xs text-slate-500">Open overview & interview recommendation on each row.</p>
        </li>
      </ol>
    </div>
  );
}
