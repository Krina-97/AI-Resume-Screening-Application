import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import AppBreadcrumbs from '../components/AppBreadcrumbs';
import OnboardingChecklist, { loadOnboarding, saveOnboarding } from '../components/OnboardingChecklist';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { useTheme } from '../context/ThemeContext';
import { formatPersonName } from '../utils/formatDisplay';

type WorkQueue = {
  newCandidates: number;
  shortlisted: number;
  interviewScheduled: number;
  jobsWithoutCandidates: number;
  emptyJobs: Array<{ id: number; title: string }>;
};

type Dashboard = {
  totalCandidates: number;
  shortlisted: number;
  rejected: number;
  interviewsScheduled: number;
  averageMatchScore: number | null;
  topCandidates: Array<{ id: number; fullName: string; latestMatchScore?: number }>;
  workQueue?: WorkQueue;
};

export default function DashboardPage() {
  const { token } = useAuth();
  const [data, setData] = useState<Dashboard | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [jobsCount, setJobsCount] = useState(0);
  const [onboardingDismissed, setOnboardingDismissed] = useState(() => loadOnboarding().dismissed);
  const { theme } = useTheme();

  useEffect(() => {
    if (!token) return;
    api
      .get('/analytics/dashboard')
      .then((res) => {
        setData(res.data);
        setLoadError(null);
      })
      .catch(() => {
        setLoadError('Unable to load analytics. Is the backend running on port 8080?');
      });
    api
      .get('/jobs')
      .then((res) => setJobsCount(res.data?.length ?? 0))
      .catch(() => setJobsCount(0));
  }, [token]);

  const chartData = useMemo(() => {
    if (!data) return [];
    return [
      { name: 'Shortlisted', value: data.shortlisted },
      { name: 'Rejected', value: data.rejected },
      { name: 'Interviews', value: data.interviewsScheduled }
    ];
  }, [data]);

  const chartColors = theme === 'dark' ? '#94a3b8' : '#64748b';
  const barFill = '#6366f1';
  const tooltipStyle = theme === 'dark'
    ? { backgroundColor: '#1e293b', border: '1px solid #334155', borderRadius: '12px' }
    : { backgroundColor: '#fff', border: '1px solid #e2e8f0', borderRadius: '12px', boxShadow: '0 8px 24px rgb(15 23 42 / 0.08)' };

  if (!data && !loadError) {
    return (
      <div className="space-y-8 animate-fadeIn">
        <div className="space-y-2">
          <div className="h-8 w-64 animate-pulse rounded-lg bg-slate-200 dark:bg-slate-800" />
          <div className="h-4 w-96 animate-pulse rounded-lg bg-slate-200 dark:bg-slate-800" />
        </div>
        <div className="grid gap-4 md:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-28 animate-pulse rounded-2xl bg-slate-200 dark:bg-slate-800" />
          ))}
        </div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="animate-fadeIn rounded-2xl border border-amber-200 bg-amber-50 px-6 py-10 text-center dark:border-amber-900/60 dark:bg-amber-950/30">
        <p className="text-sm font-medium text-amber-900 dark:text-amber-200">{loadError}</p>
      </div>
    );
  }

  if (!data) return null;

  const wq = data.workQueue;

  return (
    <div className="space-y-10 animate-fadeIn">
      <AppBreadcrumbs />
      <PageHeader
        title="HR command center"
        subtitle="Live funnel health, AI match quality, and the candidates trending toward your reqs."
      />

      {!onboardingDismissed ? (
        <OnboardingChecklist
          jobsCount={jobsCount}
          candidatesCount={data.totalCandidates}
          onDismiss={() => {
            const s = loadOnboarding();
            saveOnboarding({ ...s, dismissed: true });
            setOnboardingDismissed(true);
          }}
        />
      ) : null}

      {wq ? (
        <section className="space-y-3">
          <h2 className="text-sm font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">Work queue</h2>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <QueueCard
              label="New — needs review"
              count={wq.newCandidates}
              href="/candidates?status=NEW"
              tint="sky"
            />
            <QueueCard
              label="Shortlisted"
              count={wq.shortlisted}
              href="/candidates?status=SHORTLISTED"
              tint="emerald"
            />
            <QueueCard
              label="Interviews scheduled"
              count={wq.interviewScheduled}
              href="/candidates?status=INTERVIEW_SCHEDULED"
              tint="violet"
            />
            <QueueCard
              label="Jobs without applicants"
              count={wq.jobsWithoutCandidates}
              href="/jobs"
              tint="amber"
            />
          </div>
          {wq.emptyJobs.length > 0 ? (
            <div className="rounded-xl border border-slate-200/90 bg-white/80 p-4 dark:border-slate-800 dark:bg-slate-900/70">
              <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Empty roles</p>
              <ul className="mt-2 flex flex-wrap gap-2">
                {wq.emptyJobs.map((j) => (
                  <li key={j.id}>
                    <Link
                      to={`/candidates?jobId=${j.id}`}
                      className="rounded-lg border border-slate-200 px-3 py-1.5 text-sm font-medium text-brand-700 hover:bg-brand-50 dark:border-slate-700 dark:text-brand-300 dark:hover:bg-brand-950/40"
                    >
                      {j.title}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ) : null}
        </section>
      ) : null}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Total candidates" value={data.totalCandidates} hint="In your workspace" tint="brand" />
        <StatCard
          label="Shortlisted"
          value={data.shortlisted}
          accent="text-emerald-600 dark:text-emerald-400"
          hint="Ready for review"
          tint="emerald"
        />
        <StatCard
          label="Rejected"
          value={data.rejected}
          accent="text-rose-600 dark:text-rose-400"
          hint="Closed this cycle"
          tint="rose"
        />
        <StatCard
          label="Avg match score"
          value={data.averageMatchScore != null ? data.averageMatchScore.toFixed(1) : '—'}
          hint="Across ranked profiles"
          tint="amber"
          accent={
            data.averageMatchScore != null && data.averageMatchScore >= 70
              ? 'text-emerald-600 dark:text-emerald-400'
              : 'text-slate-900 dark:text-white'
          }
        />
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-2xl border border-slate-200/90 bg-white/90 p-5 shadow-soft backdrop-blur dark:border-slate-800 dark:bg-slate-900/80">
          <div className="mb-1 text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">
            Pipeline snapshot
          </div>
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Where candidates land</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 8, right: 8, left: -8, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={theme === 'dark' ? '#334155' : '#e2e8f0'} vertical={false} />
                <XAxis dataKey="name" tick={{ fill: chartColors, fontSize: 12 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: chartColors, fontSize: 12 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip
                  cursor={{ fill: theme === 'dark' ? 'rgb(51 65 85 / 0.35)' : 'rgb(241 245 249 / 0.9)' }}
                  contentStyle={tooltipStyle}
                  labelStyle={{ fontWeight: 600, marginBottom: 4 }}
                />
                <Bar dataKey="value" fill={barFill} radius={[10, 10, 0, 0]} maxBarSize={48} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
        <div className="rounded-2xl border border-slate-200/90 bg-white/90 p-5 shadow-soft backdrop-blur dark:border-slate-800 dark:bg-slate-900/80">
          <div className="mb-1 text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">
            Top matches
          </div>
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Highest AI scores</h2>
          <ul className="mt-4 divide-y divide-slate-100 dark:divide-slate-800">
            {data.topCandidates.length === 0 ? (
              <li className="py-10 text-center text-sm text-slate-500 dark:text-slate-400">
                No scored candidates yet—upload resumes from the Candidates page.
              </li>
            ) : (
              data.topCandidates.map((c) => (
                <li key={c.id} className="flex items-center justify-between gap-3 py-3.5 first:pt-0">
                  <div>
                    <div className="font-semibold text-slate-900 dark:text-slate-100">{formatPersonName(c.fullName)}</div>
                    <div className="text-xs text-slate-500 dark:text-slate-400">Profile #{c.id}</div>
                  </div>
                  <div className="flex h-11 min-w-[3rem] items-center justify-center rounded-xl bg-brand-50 px-3 text-lg font-bold text-brand-700 dark:bg-brand-950/60 dark:text-brand-300">
                    {c.latestMatchScore != null ? c.latestMatchScore.toFixed(0) : '—'}
                  </div>
                </li>
              ))
            )}
          </ul>
        </div>
      </div>
    </div>
  );
}

function QueueCard({
  label,
  count,
  href,
  tint
}: {
  label: string;
  count: number;
  href: string;
  tint: 'sky' | 'emerald' | 'violet' | 'amber';
}) {
  const ring =
    tint === 'emerald'
      ? 'hover:border-emerald-300 dark:hover:border-emerald-700'
      : tint === 'violet'
        ? 'hover:border-violet-300 dark:hover:border-violet-700'
        : tint === 'amber'
          ? 'hover:border-amber-300 dark:hover:border-amber-700'
          : 'hover:border-sky-300 dark:hover:border-sky-700';

  return (
    <Link
      to={href}
      className={`block rounded-2xl border border-slate-200/90 bg-white/90 p-4 shadow-soft transition hover:-translate-y-0.5 hover:shadow-soft-lg dark:border-slate-800 dark:bg-slate-900/80 ${ring}`}
    >
      <div className="text-[11px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">{label}</div>
      <div className="mt-2 text-3xl font-bold text-slate-900 dark:text-white">{count}</div>
      <div className="mt-2 text-xs font-semibold text-brand-600 dark:text-brand-400">Open queue →</div>
    </Link>
  );
}

function StatCard({
  label,
  value,
  hint,
  accent,
  tint
}: {
  label: string;
  value: string | number;
  hint?: string;
  accent?: string;
  tint?: 'brand' | 'emerald' | 'rose' | 'amber';
}) {
  const ring =
    tint === 'emerald'
      ? 'from-emerald-500/80 to-teal-400/0'
      : tint === 'rose'
        ? 'from-rose-500/80 to-rose-400/0'
        : tint === 'amber'
          ? 'from-amber-500/80 to-amber-400/0'
          : 'from-brand-500/80 to-indigo-400/0';

  return (
    <div className="group relative overflow-hidden rounded-2xl border border-slate-200/90 bg-white/90 p-5 shadow-soft backdrop-blur transition hover:shadow-soft-lg dark:border-slate-800 dark:bg-slate-900/80">
      <div className={`pointer-events-none absolute inset-x-0 top-0 h-1 bg-gradient-to-r ${ring}`} aria-hidden />
      <div className="text-[11px] font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">{label}</div>
      <div className={`mt-2 text-3xl font-bold tracking-tight ${accent ?? 'text-slate-900 dark:text-white'}`}>{value}</div>
      {hint ? <div className="mt-2 text-xs text-slate-500 dark:text-slate-500">{hint}</div> : null}
    </div>
  );
}
