import { FormEvent, useEffect, useState } from 'react';
import api from '../services/api';

type Job = {
  id: number;
  title: string;
  department?: string;
  location?: string;
  active: boolean;
};

export default function JobsPage() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');

  const load = () => api.get('/jobs').then((res) => setJobs(res.data));

  useEffect(() => {
    load();
  }, []);

  const create = async (e: FormEvent) => {
    e.preventDefault();
    await api.post('/jobs', {
      title,
      description,
      department: 'General',
      location: 'Remote',
      experienceRequired: '3+ years',
      requiredSkills: 'Java, Spring Boot, SQL',
      preferredSkills: 'React, AWS',
      active: true
    });
    setTitle('');
    setDescription('');
    load();
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-white">Job descriptions</h1>
        <p className="text-sm text-slate-500">Create roles that power AI resume matching</p>
      </div>
      <form
        onSubmit={create}
        className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 shadow-sm space-y-3 max-w-xl"
      >
        <div className="space-y-1">
          <label className="text-sm font-medium">Title</label>
          <input
            required
            className="w-full rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-950 px-3 py-2 text-sm"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </div>
        <div className="space-y-1">
          <label className="text-sm font-medium">Description</label>
          <textarea
            required
            rows={4}
            className="w-full rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-950 px-3 py-2 text-sm"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <button
          type="submit"
          className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-500"
        >
          Save job
        </button>
      </form>
      <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm divide-y divide-slate-100 dark:divide-slate-800">
        {jobs.map((j) => (
          <div key={j.id} className="px-4 py-3 flex justify-between gap-4">
            <div>
              <div className="font-semibold text-slate-900 dark:text-slate-50">{j.title}</div>
              <div className="text-xs text-slate-500">
                {j.department} · {j.location}
              </div>
            </div>
            <span className="text-xs font-semibold text-emerald-600">{j.active ? 'Active' : 'Inactive'}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
