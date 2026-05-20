import { DragEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import api from '../services/api';
import AppBreadcrumbs from '../components/AppBreadcrumbs';
import CandidateCompareModal from '../components/CandidateCompareModal';
import CandidateOverviewCell from '../components/CandidateOverviewCell';
import EmptyState from '../components/EmptyState';
import InterviewScheduleModal from '../components/InterviewScheduleModal';
import ScoreExplainDrawer from '../components/ScoreExplainDrawer';
import { IconTrash } from '../components/icons';
import PageHeader from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { usePersistedState } from '../hooks/usePersistedState';
import { loadOnboarding, saveOnboarding } from '../components/OnboardingChecklist';
import {
  downloadBulkExportZip,
  downloadCandidateExport,
  downloadFilteredExport,
  parseApiError
} from '../utils/downloadReport';
import { STATUS_HELP } from '../utils/statusHelp';
import { formatPersonName, formatPipelineStatus } from '../utils/formatDisplay';

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
};

type Candidate = {
  id: number;
  fullName: string;
  email: string;
  status: string;
  jobDescriptionId?: number;
  latestMatchScore?: number;
  matchingSkills?: string;
  missingSkills?: string;
  hrNotes?: string;
  aiSummary?: string;
  overview?: CandidateOverview | null;
};

type Job = {
  id: number;
  title: string;
};

function statusTone(status: string) {
  const s = status?.toUpperCase() ?? '';
  if (s === 'SHORTLISTED') return 'bg-emerald-100 text-emerald-800 ring-emerald-500/25 dark:bg-emerald-950/50 dark:text-emerald-300';
  if (s === 'REJECTED') return 'bg-rose-100 text-rose-800 ring-rose-500/25 dark:bg-rose-950/50 dark:text-rose-300';
  if (s === 'INTERVIEW_SCHEDULED') return 'bg-violet-100 text-violet-800 ring-violet-500/25 dark:bg-violet-950/50 dark:text-violet-300';
  return 'bg-sky-100 text-sky-800 ring-sky-500/20 dark:bg-sky-950/50 dark:text-sky-300';
}

const PIPELINE_STATUSES = ['NEW', 'SHORTLISTED', 'REJECTED', 'INTERVIEW_SCHEDULED'] as const;

const ROW_GRID =
  'grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-[2.25rem_minmax(8rem,1fr)_minmax(10rem,1.4fr)_minmax(11rem,auto)_4.5rem] lg:items-center lg:gap-x-4 lg:gap-y-0';

const FILTER_KEY = 'hiresight_candidate_filters_v1';

export default function CandidatesPage() {
  const { token } = useAuth();
  const { showToast } = useToast();
  const [searchParams, setSearchParams] = useSearchParams();
  const [persistedFilters, setPersistedFilters] = usePersistedState(FILTER_KEY, {
    jobId: '',
    status: '',
    q: ''
  });

  const [rows, setRows] = useState<Candidate[]>([]);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [jobId, setJobId] = useState(searchParams.get('jobId') ?? persistedFilters.jobId);
  const [status, setStatus] = useState(searchParams.get('status') ?? persistedFilters.status);
  const [q, setQ] = useState(searchParams.get('q') ?? persistedFilters.q);
  const [file, setFile] = useState<File | null>(null);
  const [jobForUpload, setJobForUpload] = useState<string>('');
  const [uploadOpen, setUploadOpen] = useState(false);
  const [compact, setCompact] = usePersistedState('hiresight_compact_candidates', false);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const [fileInputKey, setFileInputKey] = useState(0);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [bulkDeleting, setBulkDeleting] = useState(false);
  const [updatingStatusId, setUpdatingStatusId] = useState<number | null>(null);
  const [rescoringId, setRescoringId] = useState<number | null>(null);
  const [exporting, setExporting] = useState<string | null>(null);
  const [scoreJobByCandidate, setScoreJobByCandidate] = useState<Record<number, string>>({});
  const [compareOpen, setCompareOpen] = useState(false);
  const [scoreDrawer, setScoreDrawer] = useState<Candidate | null>(null);
  const [scheduleCandidate, setScheduleCandidate] = useState<Candidate | null>(null);
  const [notesDraft, setNotesDraft] = useState<Record<number, string>>({});

  const params = useMemo(() => {
    const p = new URLSearchParams();
    if (jobId) p.append('jobId', jobId);
    if (status) p.append('status', status);
    if (q) p.append('q', q);
    return p.toString();
  }, [jobId, status, q]);

  const allSelected = rows.length > 0 && selectedIds.size === rows.length;
  const someSelected = selectedIds.size > 0;
  const selectedList = Array.from(selectedIds);

  const syncUrl = useCallback(
    (nextJob: string, nextStatus: string, nextQ: string) => {
      const p = new URLSearchParams();
      if (nextJob) p.set('jobId', nextJob);
      if (nextStatus) p.set('status', nextStatus);
      if (nextQ) p.set('q', nextQ);
      setSearchParams(p, { replace: true });
      setPersistedFilters({ jobId: nextJob, status: nextStatus, q: nextQ });
    },
    [setSearchParams, setPersistedFilters]
  );

  const load = useCallback(() => {
    if (!token) {
      setRows([]);
      setSelectedIds(new Set());
      return;
    }
    setLoading(true);
    api
      .get(`/candidates?${params}`)
      .then((res) => {
        setRows(res.data);
        setSelectedIds(new Set());
      })
      .catch(() => {
        showToast('Could not load candidates', 'error');
        setRows([]);
        setSelectedIds(new Set());
      })
      .finally(() => setLoading(false));
  }, [params, token, showToast]);

  useEffect(() => {
    if (!token) return;
    api
      .get<Job[]>('/jobs')
      .then((res) => setJobs(res.data))
      .catch(() => setJobs([]));
  }, [token]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    const openUpload = () => setUploadOpen(true);
    window.addEventListener('hiresight:open-upload', openUpload);
    return () => window.removeEventListener('hiresight:open-upload', openUpload);
  }, []);

  const onDropUpload = (e: DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    const f = e.dataTransfer.files?.[0];
    if (f && /\.(pdf|docx)$/i.test(f.name)) {
      setFile(f);
      setUploadOpen(true);
    }
  };

  const toggleSelect = (id: number) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    if (allSelected) setSelectedIds(new Set());
    else setSelectedIds(new Set(rows.map((c) => c.id)));
  };

  const deleteSelected = async () => {
    const ids = Array.from(selectedIds);
    if (ids.length === 0) return;
    if (!window.confirm(`Delete ${ids.length} selected candidate${ids.length === 1 ? '' : 's'}?`)) return;
    setBulkDeleting(true);
    try {
      await api.post('/candidates/bulk-delete', { ids });
      setRows((prev) => prev.filter((c) => !selectedIds.has(c.id)));
      setSelectedIds(new Set());
      showToast(`Deleted ${ids.length} candidate${ids.length === 1 ? '' : 's'}`, 'success');
      load();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      showToast(axiosErr.response?.data?.message ?? 'Could not delete selected candidates', 'error');
    } finally {
      setBulkDeleting(false);
    }
  };

  const updateStatus = async (candidateId: number, newStatus: string) => {
    setUpdatingStatusId(candidateId);
    try {
      await api.patch(`/candidates/${candidateId}`, { status: newStatus });
      setRows((prev) => prev.map((c) => (c.id === candidateId ? { ...c, status: newStatus } : c)));
      showToast('Status updated', 'success');
    } catch {
      showToast('Could not update status', 'error');
    } finally {
      setUpdatingStatusId(null);
    }
  };

  const saveNotes = async (candidateId: number) => {
    const hrNotes = notesDraft[candidateId] ?? '';
    try {
      await api.patch(`/candidates/${candidateId}`, { hrNotes });
      setRows((prev) => prev.map((c) => (c.id === candidateId ? { ...c, hrNotes } : c)));
      showToast('Notes saved', 'success');
    } catch {
      showToast('Could not save notes', 'error');
    }
  };

  const resolveScoreJobId = (c: Candidate): number | null => {
    const fromPicker = scoreJobByCandidate[c.id];
    if (fromPicker) return Number(fromPicker);
    if (c.jobDescriptionId) return c.jobDescriptionId;
    if (jobId) return Number(jobId);
    if (jobs[0]?.id) return jobs[0].id;
    return null;
  };

  const reEvaluate = async (c: Candidate) => {
    const jid = resolveScoreJobId(c);
    if (!jid || Number.isNaN(jid)) {
      showToast('Choose a target job for re-evaluation', 'info');
      return;
    }
    setRescoringId(c.id);
    try {
      const { data } = await api.post<Candidate>(`/candidates/${c.id}/score?jobDescriptionId=${jid}`);
      setRows((prev) => prev.map((row) => (row.id === c.id ? data : row)));
      showToast('Match score and overview refreshed', 'success');
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      showToast(axiosErr.response?.data?.message ?? 'Re-evaluation failed', 'error');
    } finally {
      setRescoringId(null);
    }
  };

  const filterParams = () => {
    const p = new URLSearchParams();
    if (jobId) p.append('jobId', jobId);
    if (status) p.append('status', status);
    if (q) p.append('q', q);
    return p;
  };

  const exportFiltered = async (format: 'xlsx' | 'pdf') => {
    setExporting(`filter-${format}`);
    try {
      await downloadFilteredExport(format, filterParams());
      showToast(`Downloaded filtered ${format.toUpperCase()}`, 'success');
    } catch (err: unknown) {
      showToast(
        err instanceof Error ? err.message : await parseApiError(err, 'Export failed'),
        'error'
      );
    } finally {
      setExporting(null);
    }
  };

  const exportSelectedZip = async (formats: ('xlsx' | 'pdf')[]) => {
    const ids = Array.from(selectedIds);
    if (ids.length === 0) {
      showToast('Select at least one candidate', 'info');
      return;
    }
    setExporting(`zip-${formats.join('-')}`);
    try {
      await downloadBulkExportZip(ids, formats);
      showToast(`Downloaded ZIP (${ids.length} candidate${ids.length === 1 ? '' : 's'})`, 'success');
      setSelectedIds(new Set());
    } catch (err: unknown) {
      showToast(
        err instanceof Error ? err.message : await parseApiError(err, 'Bulk export failed'),
        'error'
      );
    } finally {
      setExporting(null);
    }
  };

  const exportOne = async (candidateId: number, format: 'xlsx' | 'pdf') => {
    setExporting(`one-${candidateId}-${format}`);
    try {
      await downloadCandidateExport(candidateId, format);
      showToast(`Downloaded ${format.toUpperCase()}`, 'success');
    } catch (err: unknown) {
      showToast(
        err instanceof Error ? err.message : await parseApiError(err, 'Export failed'),
        'error'
      );
    } finally {
      setExporting(null);
    }
  };

  const upload = async () => {
    if (!file || !jobForUpload.trim()) return;
    setUploading(true);
    const form = new FormData();
    form.append('file', file);
    const url = `/resumes/upload?jobDescriptionId=${encodeURIComponent(jobForUpload.trim())}`;
    try {
      const { data } = await api.post<{ message?: string }>(url, form);
      setFile(null);
      setJobForUpload('');
      setFileInputKey((k) => k + 1);
      const ob = loadOnboarding();
      saveOnboarding({ ...ob, resumeUploaded: true });
      showToast(data?.message ?? 'Resume evaluated and added', 'success');
      load();
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      showToast(axiosErr.response?.data?.message ?? 'Upload failed', 'error');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="min-w-0 space-y-8 animate-fadeIn">
      <AppBreadcrumbs />
      <PageHeader
        title="Candidates"
        subtitle="Filter your pipeline, drop in resumes, and read AI-aligned overviews beside every profile."
        actions={
          <button
            type="button"
            onClick={() => setUploadOpen((o) => !o)}
            className="rounded-xl bg-brand-600 px-4 py-2 text-sm font-bold text-white shadow-md hover:bg-brand-500"
          >
            {uploadOpen ? 'Hide upload' : '+ Add candidate'}
          </button>
        }
      />

      <div className="flex flex-wrap items-end gap-3 rounded-2xl border border-slate-200/90 bg-white/80 p-4 shadow-soft backdrop-blur dark:border-slate-800 dark:bg-slate-900/70">
        <div className="min-w-[200px] flex-1">
          <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Job</label>
          <select
            className="input-field py-2"
            value={jobId}
            onChange={(e) => {
              setJobId(e.target.value);
              syncUrl(e.target.value, status, q);
            }}
          >
            <option value="">All jobs</option>
            {jobs.map((job) => (
              <option key={job.id} value={String(job.id)}>
                {job.title}
              </option>
            ))}
          </select>
        </div>
        <div className="min-w-[160px]">
          <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Status</label>
          <select
            className="input-field py-2"
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              syncUrl(jobId, e.target.value, q);
            }}
          >
            <option value="">All statuses</option>
            {PIPELINE_STATUSES.map((s) => (
              <option key={s} value={s}>
                {formatPipelineStatus(s)}
              </option>
            ))}
          </select>
        </div>
        <div className="min-w-[200px] flex-[2]">
          <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Search</label>
          <input
            placeholder="Name or email…"
            className="input-field py-2"
            value={q}
            onChange={(e) => {
              setQ(e.target.value);
              syncUrl(jobId, status, e.target.value);
            }}
          />
        </div>
        <label className="flex cursor-pointer items-center gap-2 pb-2 text-sm font-medium text-slate-600 dark:text-slate-300">
          <input type="checkbox" checked={compact} onChange={(e) => setCompact(e.target.checked)} className="rounded" />
          Compact rows
        </label>
      </div>

      {uploadOpen ? (
        <div
          onDragOver={(e) => {
            e.preventDefault();
            setDragOver(true);
          }}
          onDragLeave={() => setDragOver(false)}
          onDrop={onDropUpload}
          className={`max-w-full overflow-x-hidden rounded-2xl border-2 border-dashed p-6 transition-all md:p-8 ${
            dragOver
              ? 'border-brand-500 bg-brand-50/80 dark:border-brand-400 dark:bg-brand-950/30'
              : 'border-slate-300/90 bg-white/70 dark:border-slate-700 dark:bg-slate-900/50'
          }`}
        >
          <div className="mx-auto flex w-full flex-col gap-6">
            <div>
              <div className="text-sm font-bold text-slate-900 dark:text-white">Upload resume</div>
              <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
                PDF or DOCX — scored against the selected job (auto status by match band).
              </p>
              {file ? (
                <p className="mt-2 text-xs font-medium text-brand-700 dark:text-brand-300">Selected: {file.name}</p>
              ) : null}
            </div>
            <label className="inline-flex w-full cursor-pointer">
              <span className="inline-flex w-full items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold dark:border-slate-700 dark:bg-slate-950">
                Choose file
              </span>
              <input
                key={fileInputKey}
                type="file"
                accept=".pdf,.docx"
                className="hidden"
                onChange={(e) => setFile(e.target.files?.[0] ?? null)}
              />
            </label>
            <select
              className="input-field"
              value={jobForUpload}
              onChange={(e) => setJobForUpload(e.target.value)}
            >
              <option value="">Select job to evaluate against</option>
              {jobs.map((job) => (
                <option key={job.id} value={String(job.id)}>
                  {job.title}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={upload}
              disabled={!file || !jobForUpload.trim() || uploading || jobs.length === 0}
              className="rounded-xl bg-gradient-to-r from-brand-600 to-brand-500 py-2.5 text-sm font-bold text-white disabled:opacity-50"
            >
              {uploading ? 'Evaluating…' : 'Upload resume'}
            </button>
          </div>
        </div>
      ) : null}

      <div className="w-full min-w-0 overflow-hidden rounded-2xl border border-slate-200/90 bg-white/90 shadow-soft dark:border-slate-800 dark:bg-slate-900/80">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-slate-50/90 px-4 py-3 dark:border-slate-800 dark:bg-slate-800/50">
          <div className="flex flex-wrap items-center gap-3">
            {rows.length > 0 ? (
              <label className="flex cursor-pointer items-center gap-2 text-sm font-medium">
                <input type="checkbox" checked={allSelected} onChange={toggleSelectAll} className="h-4 w-4 rounded" />
                Select all
              </label>
            ) : null}
            {someSelected && selectedList.length === 2 ? (
              <button
                type="button"
                onClick={() => setCompareOpen(true)}
                className="rounded-xl border border-brand-200 bg-brand-50 px-3 py-1.5 text-sm font-semibold text-brand-800 dark:border-brand-800 dark:bg-brand-950/50 dark:text-brand-200"
              >
                Compare selected
              </button>
            ) : null}
            <span className="hidden text-[10px] font-bold uppercase tracking-wider text-slate-400 sm:inline">
              Filtered list
            </span>
            <button
              type="button"
              disabled={exporting !== null || rows.length === 0}
              onClick={() => exportFiltered('xlsx')}
              className="rounded-xl border px-3 py-1.5 text-sm font-semibold disabled:opacity-50"
              title="One Excel file for all candidates matching filters"
            >
              {exporting === 'filter-xlsx' ? '…' : 'Excel (all filtered)'}
            </button>
            <button
              type="button"
              disabled={exporting !== null || rows.length === 0}
              onClick={() => exportFiltered('pdf')}
              className="rounded-xl border px-3 py-1.5 text-sm font-semibold disabled:opacity-50"
              title="One PDF for all candidates matching filters"
            >
              {exporting === 'filter-pdf' ? '…' : 'PDF (all filtered)'}
            </button>
            {someSelected ? (
              <>
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Selected</span>
                <button
                  type="button"
                  disabled={exporting !== null}
                  onClick={() => exportSelectedZip(['xlsx', 'pdf'])}
                  className="rounded-xl border border-brand-200 bg-brand-50 px-3 py-1.5 text-sm font-semibold text-brand-800 disabled:opacity-50 dark:border-brand-800 dark:bg-brand-950/50 dark:text-brand-200"
                  title="ZIP with Excel + PDF per selected candidate"
                >
                  {exporting === 'zip-xlsx-pdf' ? '…' : `ZIP (${selectedIds.size})`}
                </button>
                <button
                  type="button"
                  disabled={exporting !== null}
                  onClick={() => exportSelectedZip(['xlsx'])}
                  className="rounded-xl border px-3 py-1.5 text-sm font-semibold disabled:opacity-50"
                >
                  {exporting === 'zip-xlsx' ? '…' : 'Excel ZIP'}
                </button>
                <button
                  type="button"
                  disabled={exporting !== null}
                  onClick={() => exportSelectedZip(['pdf'])}
                  className="rounded-xl border px-3 py-1.5 text-sm font-semibold disabled:opacity-50"
                >
                  {exporting === 'zip-pdf' ? '…' : 'PDF ZIP'}
                </button>
              </>
            ) : null}
          </div>
          <button
            type="button"
            disabled={!someSelected || bulkDeleting}
            onClick={deleteSelected}
            className="inline-flex items-center gap-2 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-semibold text-rose-800 disabled:opacity-50 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-200"
          >
            <IconTrash className="h-4 w-4" />
            {bulkDeleting ? 'Deleting…' : `Delete selected${someSelected ? ` (${selectedIds.size})` : ''}`}
          </button>
        </div>

        {loading ? (
          <div className="px-5 py-16 text-center text-sm text-slate-500">Loading candidates…</div>
        ) : rows.length === 0 ? (
          <div className="p-6">
            <EmptyState
              title="No candidates yet"
              description="Upload a resume against a published job to see AI match scores and interview recommendations."
              action={
                <div className="flex flex-wrap justify-center gap-2">
                  <button
                    type="button"
                    onClick={() => setUploadOpen(true)}
                    className="rounded-xl bg-brand-600 px-4 py-2 text-sm font-bold text-white"
                  >
                    Upload resume
                  </button>
                  <Link to="/jobs" className="rounded-xl border px-4 py-2 text-sm font-semibold">
                    Publish a job
                  </Link>
                </div>
              }
            />
          </div>
        ) : (
          <ul className={`divide-y divide-slate-100 dark:divide-slate-800 ${compact ? 'text-xs' : ''}`}>
            {rows.map((c) => {
              const checked = selectedIds.has(c.id);
              const notesValue = notesDraft[c.id] ?? c.hrNotes ?? '';
              const jobTitle = jobs.find((j) => j.id === c.jobDescriptionId)?.title ?? c.overview?.jobTitle;
              return (
                <li
                  key={c.id}
                  className={`transition-colors ${checked ? 'bg-brand-50/40 dark:bg-brand-950/20' : 'hover:bg-slate-50/90 dark:hover:bg-slate-800/40'}`}
                >
                  <div className={`px-4 py-3 ${ROW_GRID}`}>
                    <input
                      type="checkbox"
                      className="h-4 w-4 rounded lg:justify-self-center"
                      checked={checked}
                      onChange={() => toggleSelect(c.id)}
                    />
                    <div className="min-w-0">
                      <div className="text-sm font-semibold text-slate-900 dark:text-slate-50">
                        {formatPersonName(c.fullName)}
                      </div>
                    </div>
                    <div className="min-w-0 break-all text-sm text-slate-600 dark:text-slate-300">{c.email}</div>
                    <div className="min-w-0">
                      <select
                        title={STATUS_HELP[c.status?.toUpperCase() ?? 'NEW']}
                        className={`input-field status-select w-full font-semibold ${statusTone(c.status)}`}
                        value={c.status?.toUpperCase() ?? 'NEW'}
                        disabled={updatingStatusId === c.id}
                        onChange={(e) => updateStatus(c.id, e.target.value)}
                      >
                        {PIPELINE_STATUSES.map((s) => (
                          <option key={s} value={s}>
                            {formatPipelineStatus(s)}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="flex items-center gap-1 lg:justify-center">
                      <button
                        type="button"
                        onClick={() => setScoreDrawer(c)}
                        className="inline-flex items-center justify-center rounded-lg bg-brand-50 px-2 py-1 text-sm font-bold tabular-nums text-brand-700 hover:ring-2 hover:ring-brand-300 dark:bg-brand-950/50 dark:text-brand-300"
                        title="Why this score?"
                      >
                        {c.latestMatchScore != null ? `${c.latestMatchScore.toFixed(1)}%` : '—'}
                      </button>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-2 border-t border-slate-100/80 px-4 py-2 dark:border-slate-800/80 lg:pl-12">
                    <label className="sr-only" htmlFor={`score-job-${c.id}`}>
                      Job for re-evaluation
                    </label>
                    <select
                      id={`score-job-${c.id}`}
                      className="input-field max-w-[200px] py-1 text-xs"
                      value={
                        scoreJobByCandidate[c.id] ??
                        String(c.jobDescriptionId ?? jobId ?? jobs[0]?.id ?? '')
                      }
                      onChange={(e) =>
                        setScoreJobByCandidate((prev) => ({ ...prev, [c.id]: e.target.value }))
                      }
                      disabled={jobs.length === 0 || rescoringId === c.id}
                    >
                      <option value="">Job for scoring</option>
                      {jobs.map((job) => (
                        <option key={job.id} value={String(job.id)}>
                          {job.title}
                        </option>
                      ))}
                    </select>
                    <button
                      type="button"
                      disabled={rescoringId === c.id || jobs.length === 0}
                      onClick={() => reEvaluate(c)}
                      className="rounded-lg border px-2 py-1 text-xs font-semibold disabled:opacity-50"
                    >
                      {rescoringId === c.id ? 'Scoring…' : 'Re-evaluate'}
                    </button>
                    <button
                      type="button"
                      disabled={exporting !== null}
                      onClick={() => exportOne(c.id, 'xlsx')}
                      className="rounded-lg border px-2 py-1 text-xs font-semibold disabled:opacity-50"
                    >
                      Excel
                    </button>
                    <button
                      type="button"
                      disabled={exporting !== null}
                      onClick={() => exportOne(c.id, 'pdf')}
                      className="rounded-lg border px-2 py-1 text-xs font-semibold disabled:opacity-50"
                    >
                      PDF
                    </button>
                    {(c.status?.toUpperCase() === 'SHORTLISTED' || c.status?.toUpperCase() === 'NEW') && (
                      <button
                        type="button"
                        onClick={() => setScheduleCandidate(c)}
                        className="rounded-lg border border-violet-200 bg-violet-50 px-2 py-1 text-xs font-semibold text-violet-800 dark:border-violet-900 dark:bg-violet-950/40 dark:text-violet-200"
                      >
                        Schedule interview
                      </button>
                    )}
                  </div>

                  <div className="border-t border-slate-100/80 px-4 pb-3 pt-2 dark:border-slate-800/80 lg:pl-12">
                    <CandidateOverviewCell
                      overview={c.overview}
                      fallbackSummary={c.aiSummary}
                      defaultCollapsed
                      compact={compact}
                    />
                  </div>

                  <div className="border-t border-slate-100/80 px-4 pb-4 dark:border-slate-800/80 lg:pl-12">
                    <label className="mb-1 block text-[10px] font-bold uppercase tracking-wider text-slate-500">
                      HR notes
                    </label>
                    <div className="flex gap-2">
                      <textarea
                        rows={compact ? 1 : 2}
                        className="input-field min-h-0 flex-1 text-sm"
                        placeholder="Private notes for your team…"
                        value={notesValue}
                        onChange={(e) => setNotesDraft((d) => ({ ...d, [c.id]: e.target.value }))}
                      />
                      <button
                        type="button"
                        onClick={() => saveNotes(c.id)}
                        className="shrink-0 self-end rounded-lg border px-3 py-2 text-xs font-semibold"
                      >
                        Save
                      </button>
                    </div>
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      <CandidateCompareModal ids={selectedList} open={compareOpen} onClose={() => setCompareOpen(false)} />

      <ScoreExplainDrawer
        open={scoreDrawer != null}
        onClose={() => setScoreDrawer(null)}
        name={scoreDrawer?.fullName ?? ''}
        matchPercent={scoreDrawer?.latestMatchScore}
        matchingSkills={scoreDrawer?.matchingSkills}
        missingSkills={scoreDrawer?.missingSkills}
        jobTitle={scoreDrawer ? jobs.find((j) => j.id === scoreDrawer.jobDescriptionId)?.title : undefined}
      />

      {scheduleCandidate ? (
        <InterviewScheduleModal
          open
          candidateId={scheduleCandidate.id}
          candidateName={scheduleCandidate.fullName}
          onClose={() => setScheduleCandidate(null)}
          onScheduled={load}
        />
      ) : null}
    </div>
  );
}
