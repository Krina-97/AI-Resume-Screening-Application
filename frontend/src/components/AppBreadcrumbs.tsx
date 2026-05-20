import { Link, useLocation } from 'react-router-dom';

const LABELS: Record<string, string> = {
  '': 'Dashboard',
  candidates: 'Candidates',
  jobs: 'Job descriptions'
};

export default function AppBreadcrumbs({ extra }: { extra?: string }) {
  const { pathname } = useLocation();
  const segment = pathname.split('/').filter(Boolean)[0] ?? '';
  const base = LABELS[segment] ?? 'HireSight';

  return (
    <nav aria-label="Breadcrumb" className="mb-4 text-sm text-slate-500 dark:text-slate-400">
      <Link to="/" className="font-medium hover:text-brand-600 dark:hover:text-brand-400">
        HireSight
      </Link>
      {segment ? (
        <>
          <span className="mx-2">›</span>
          <span className="font-semibold text-slate-700 dark:text-slate-200">{base}</span>
        </>
      ) : null}
      {extra ? (
        <>
          <span className="mx-2">›</span>
          <span className="text-slate-600 dark:text-slate-300">{extra}</span>
        </>
      ) : null}
    </nav>
  );
}
