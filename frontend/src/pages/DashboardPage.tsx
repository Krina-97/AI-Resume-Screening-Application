import { useEffect, useState } from 'react';
import api from '../services/api';
import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

type Dashboard = {
  totalCandidates: number;
  shortlisted: number;
  rejected: number;
  interviewsScheduled: number;
  averageMatchScore: number | null;
  topCandidates: Array<{ id: number; fullName: string; latestMatchScore?: number }>;
};

export default function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    api.get('/analytics/dashboard').then((res) => setData(res.data));
  }, []);

  if (!data) {
    return <div className="text-slate-500">Loading analytics…</div>;
  }

  const chartData = [
    { name: 'Shortlisted', value: data.shortlisted },
    { name: 'Rejected', value: data.rejected },
    { name: 'Interviews', value: data.interviewsScheduled }
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-white">HR Command Center</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">Live hiring funnel and AI match quality</p>
      </div>
      <div className="grid gap-4 md:grid-cols-4">
        <StatCard label="Total candidates" value={data.totalCandidates} />
        <StatCard label="Shortlisted" value={data.shortlisted} accent="text-emerald-600" />
        <StatCard label="Rejected" value={data.rejected} accent="text-rose-600" />
        <StatCard
          label="Avg match score"
          value={data.averageMatchScore != null ? data.averageMatchScore.toFixed(1) : '—'}
        />
      </div>
      <div className="grid gap-4 lg:grid-cols-2">
        <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 shadow-sm">
          <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100 mb-4">Pipeline snapshot</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <XAxis dataKey="name" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip />
                <Bar dataKey="value" fill="#6366f1" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
        <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 shadow-sm">
          <h2 className="text-sm font-semibold text-slate-800 dark:text-slate-100 mb-4">Top AI matches</h2>
          <ul className="divide-y divide-slate-100 dark:divide-slate-800">
            {data.topCandidates.map((c) => (
              <li key={c.id} className="py-3 flex justify-between text-sm">
                <span className="font-medium text-slate-800 dark:text-slate-100">{c.fullName}</span>
                <span className="text-brand-600 font-semibold">
                  {c.latestMatchScore != null ? c.latestMatchScore.toFixed(1) : '—'}
                </span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

function StatCard({ label, value, accent }: { label: string; value: string | number; accent?: string }) {
  return (
    <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 shadow-sm">
      <div className="text-xs uppercase tracking-wide text-slate-500">{label}</div>
      <div className={`mt-2 text-3xl font-semibold ${accent ?? 'text-slate-900 dark:text-white'}`}>{value}</div>
    </div>
  );
}
