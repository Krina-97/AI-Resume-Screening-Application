import { useEffect, useMemo, useState } from 'react';
import api from '../services/api';

type Candidate = {
  id: number;
  fullName: string;
  email: string;
  status: string;
  latestMatchScore?: number;
  aiSummary?: string;
};

type Job = {
  id: number;
  title: string;
  department?: string;
  location?: string;
};

export default function CandidatesPage() {
  const [rows, setRows] = useState<Candidate[]>([]);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [jobId, setJobId] = useState<string>('');
  const [status, setStatus] = useState<string>('');
  const [q, setQ] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [jobForUpload, setJobForUpload] = useState<string>('');
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [fileInputKey, setFileInputKey] = useState(0);

  const params = useMemo(() => {
    const p = new URLSearchParams();
    if (jobId) p.append('jobId', jobId);
    if (status) p.append('status', status);
    if (q) p.append('q', q);
    return p.toString();
  }, [jobId, status, q]);

  const load = () => {
    api.get(`/candidates?${params}`).then((res) => setRows(res.data));
  };

  useEffect(() => {
    api.get<Job[]>('/jobs').then((res) => setJobs(res.data));
  }, []);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params]);

  const upload = async () => {
    if (!file) return;
    setUploading(true);
    setUploadError(null);
    const form = new FormData();
    form.append('file', file);
    const jobParam = jobForUpload.trim();
    const url = jobParam
      ? `/resumes/upload?jobDescriptionId=${encodeURIComponent(jobParam)}`
      : '/resumes/upload';
    try {
      await api.post(url, form);
      setFile(null);
      setJobForUpload('');
      setFileInputKey((k) => k + 1);
      load();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setUploadError(axiosErr.response?.data?.message ?? 'Upload failed. Is the backend running?');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900 dark:text-white">Candidates</h1>
          <p className="text-sm text-slate-500">Search, filter, and upload resumes for AI ranking</p>
        </div>
        <div className="flex flex-wrap gap-2 items-center">
          <select
            className="rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-2 py-1 text-sm min-w-[200px] max-w-xs"
            value={jobId}
            onChange={(e) => setJobId(e.target.value)}
          >
            <option value="">All jobs</option>
            {jobs.map((job) => (
              <option key={job.id} value={String(job.id)}>
                {job.title}
              </option>
            ))}
          </select>
          <select
            className="rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-2 py-1 text-sm"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            <option value="">All statuses</option>
            <option>NEW</option>
            <option>SHORTLISTED</option>
            <option>REJECTED</option>
            <option>INTERVIEW_SCHEDULED</option>
          </select>
          <input
            placeholder="Search"
            className="rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-2 py-1 text-sm"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
      </div>

      <div className="rounded-2xl border border-dashed border-slate-300 dark:border-slate-700 p-4 bg-white/60 dark:bg-slate-900/60 flex flex-col md:flex-row gap-3 md:items-center">
        <div className="flex-1 space-y-1">
          <div className="text-sm font-semibold text-slate-800 dark:text-slate-100">Drag & drop upload</div>
          <p className="text-xs text-slate-500">PDF or DOCX. Select a job for instant AI match scoring (optional).</p>
        </div>
        <input
          key={fileInputKey}
          type="file"
          accept=".pdf,.docx"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />
        <select
          className="rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 px-2 py-1 text-sm min-w-[220px] max-w-sm"
          value={jobForUpload}
          onChange={(e) => setJobForUpload(e.target.value)}
        >
          <option value="">Select job (optional)</option>
          {jobs.map((job) => (
            <option key={job.id} value={String(job.id)}>
              {job.title}
            </option>
          ))}
        </select>
        <button
          onClick={upload}
          className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
          disabled={!file || uploading}
        >
          {uploading ? 'Uploading…' : 'Upload & parse'}
        </button>
      </div>
      {uploadError && (
        <div className="text-sm text-red-600 dark:text-red-400 rounded-lg bg-red-50 dark:bg-red-950/40 border border-red-100 dark:border-red-900 px-3 py-2">
          {uploadError}
        </div>
      )}

      <div className="overflow-auto rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm">
        <table className="min-w-full text-sm">
          <thead className="bg-slate-50 dark:bg-slate-800/80">
            <tr>
              <th className="px-4 py-2 text-left font-semibold text-slate-600 dark:text-slate-300">Name</th>
              <th className="px-4 py-2 text-left font-semibold text-slate-600 dark:text-slate-300">Email</th>
              <th className="px-4 py-2 text-left font-semibold text-slate-600 dark:text-slate-300">Status</th>
              <th className="px-4 py-2 text-left font-semibold text-slate-600 dark:text-slate-300">Match</th>
              <th className="px-4 py-2 text-left font-semibold text-slate-600 dark:text-slate-300 min-w-[280px]">
                Candidate Overview
              </th>
            </tr>
          </thead>
          <tbody>
            {rows.map((c) => (
              <tr key={c.id} className="border-t border-slate-100 dark:border-slate-800 align-top">
                <td className="px-4 py-3 font-medium text-slate-900 dark:text-slate-50 whitespace-nowrap">{c.fullName}</td>
                <td className="px-4 py-3 text-slate-600 dark:text-slate-300">{c.email}</td>
                <td className="px-4 py-3">
                  <span className="inline-flex rounded-full bg-slate-100 dark:bg-slate-800 px-2 py-0.5 text-xs font-semibold">
                    {c.status}
                  </span>
                </td>
                <td className="px-4 py-3 text-brand-600 font-semibold whitespace-nowrap">
                  {c.latestMatchScore != null ? c.latestMatchScore.toFixed(1) : '—'}
                </td>
                <td className="px-4 py-3 text-slate-600 dark:text-slate-300 align-top min-w-[280px] max-w-xl whitespace-normal break-words leading-relaxed">
                  {c.aiSummary ?? '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
