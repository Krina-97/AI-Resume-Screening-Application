import { FormEvent, useCallback, useEffect, useState } from 'react';
import api from '../services/api';
import AppBreadcrumbs from '../components/AppBreadcrumbs';
import JobTemplateCards, { type DepartmentTemplate } from '../components/JobTemplateCards';
import PublishJobPreviewModal from '../components/PublishJobPreviewModal';
import { IconTrash } from '../components/icons';
import PageHeader from '../components/PageHeader';
import JobDescriptionAiBot, { JobDescriptionAssistResult } from '../components/JobDescriptionAiBot';
import { loadOnboarding, saveOnboarding } from '../components/OnboardingChecklist';
import { useToast } from '../context/ToastContext';

type Job = {
  id: number;
  title: string;
  department?: string;
  location?: string;
  active: boolean;
};

type Department = {
  id: number;
  name: string;
};

type LocationOption = {
  id: number;
  name: string;
};

type SkillOption = {
  id: number;
  name: string;
};

type ExperienceOption = {
  id: number;
  name: string;
};


export default function JobsPage() {
  const { showToast } = useToast();
  const [jobs, setJobs] = useState<Job[]>([]);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [locations, setLocations] = useState<LocationOption[]>([]);
  const [experienceLevels, setExperienceLevels] = useState<ExperienceOption[]>([]);
  const [skillOptions, setSkillOptions] = useState<SkillOption[]>([]);
  const [preferredSkillOptions, setPreferredSkillOptions] = useState<SkillOption[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [department, setDepartment] = useState('');
  const [location, setLocation] = useState('');
  const [experienceRequired, setExperienceRequired] = useState('');
  const [selectedRequiredSkills, setSelectedRequiredSkills] = useState<string[]>([]);
  const [selectedPreferredSkills, setSelectedPreferredSkills] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);
  const [deletingJobId, setDeletingJobId] = useState<number | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const applyAiAssist = useCallback((result: JobDescriptionAssistResult) => {
    setTitle(result.title);
    setDescription(result.description);
    setSelectedRequiredSkills(result.requiredSkills);
    setSelectedPreferredSkills(result.preferredSkills);
  }, []);

  const load = () => api.get('/jobs').then((res) => setJobs(res.data));

  const deleteJob = async (job: Job) => {
    if (!window.confirm(`Delete job "${job.title}"? Match scores for this role will be removed.`)) {
      return;
    }
    setDeletingJobId(job.id);
    setDeleteError(null);
    try {
      await api.delete(`/jobs/${job.id}`);
      setJobs((prev) => prev.filter((j) => j.id !== job.id));
      showToast(`Deleted "${job.title}"`, 'success');
    } catch {
      setDeleteError('Could not delete job. Try again.');
      showToast('Could not delete job', 'error');
    } finally {
      setDeletingJobId(null);
    }
  };

  const loadDepartments = () => api.get('/departments').then((res) => setDepartments(res.data));

  const loadLocations = () => api.get('/locations').then((res) => setLocations(res.data));

  const loadSkills = () => api.get('/skills').then((res) => setSkillOptions(res.data));

  const loadPreferredSkills = () => api.get('/preferred-skills').then((res) => setPreferredSkillOptions(res.data));

  const loadExperienceLevels = () => api.get('/experience-levels').then((res) => setExperienceLevels(res.data));

  useEffect(() => {
    load();
    loadDepartments();
    loadLocations();
    loadExperienceLevels();
    loadSkills();
    loadPreferredSkills();
  }, []);

  const toggleRequiredSkill = (skillName: string) => {
    setSelectedRequiredSkills((prev) =>
      prev.includes(skillName) ? prev.filter((s) => s !== skillName) : [...prev, skillName]
    );
  };

  const togglePreferredSkill = (skillName: string) => {
    setSelectedPreferredSkills((prev) =>
      prev.includes(skillName) ? prev.filter((s) => s !== skillName) : [...prev, skillName]
    );
  };

  const handleDepartmentChange = (deptName: string) => {
    setDepartment(deptName);
    if (!deptName) {
      setTitle('');
      setDescription('');
      setSelectedRequiredSkills([]);
      setSelectedPreferredSkills([]);
    }
  };

  const applyTemplate = (t: DepartmentTemplate) => {
    setDepartment(t.departmentName);
    setTitle(t.title);
    setDescription(t.description);
    setSelectedRequiredSkills(
      t.requiredSkills
        .split(/[,;]/)
        .map((s) => s.trim())
        .filter(Boolean)
    );
    setSelectedPreferredSkills(
      t.preferredSkills
        .split(/[,;]/)
        .map((s) => s.trim())
        .filter(Boolean)
    );
    showToast(`Applied ${t.title} template`, 'info');
  };

  const openPreview = (e: FormEvent) => {
    e.preventDefault();
    setPreviewOpen(true);
  };

  const publishJob = async () => {
    setSaving(true);
    try {
      await api.post('/jobs', {
        title,
        description,
        department,
        location,
        experienceRequired,
        requiredSkills: selectedRequiredSkills.join(', '),
        preferredSkills: selectedPreferredSkills.join(', '),
        active: true
      });
      const ob = loadOnboarding();
      saveOnboarding({ ...ob, jobCreated: true });
      setTitle('');
      setDescription('');
      setDepartment('');
      setLocation('');
      setExperienceRequired('');
      setSelectedRequiredSkills([]);
      setSelectedPreferredSkills([]);
      setPreviewOpen(false);
      showToast('Job published', 'success');
      load();
    } catch {
      showToast('Could not publish job', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-10 animate-fadeIn">
      <AppBreadcrumbs />
      <PageHeader
        title="Job descriptions"
        subtitle="Rich job posts become the signal for AI matching—skills, seniority, and narrative all flow into candidate scores."
      />

      <JobTemplateCards onApply={applyTemplate} />

      <div className="flex flex-col gap-10">
        <form
          onSubmit={openPreview}
          className="w-full max-w-none space-y-4 rounded-2xl border border-slate-200/90 bg-white/90 p-6 shadow-soft backdrop-blur dark:border-slate-800 dark:bg-slate-900/80"
        >
          <div>
            <h2 className="text-sm font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">Create role</h2>
            <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
              Select a department — the AI assistant below will draft skills and the job description.
            </p>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600 dark:text-slate-400">Department</label>
            <select
              className="input-field"
              value={department}
              onChange={(e) => handleDepartmentChange(e.target.value)}
            >
              <option value="">Select department</option>
              {departments.map((d) => (
                <option key={d.id} value={d.name}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600 dark:text-slate-400">Title *</label>
            <input
              required
              className="input-field"
              placeholder="Senior backend engineer"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs font-semibold text-slate-600 dark:text-slate-400">Location</label>
              <select
                className="input-field"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
              >
                <option value="">Select location</option>
                {locations.map((l) => (
                  <option key={l.id} value={l.name}>
                    {l.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold text-slate-600 dark:text-slate-400">Experience</label>
              <select
                className="input-field"
                value={experienceRequired}
                onChange={(e) => setExperienceRequired(e.target.value)}
              >
                <option value="">Select experience</option>
                {experienceLevels.map((level) => (
                  <option key={level.id} value={level.name}>
                    {level.name}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600 dark:text-slate-400">Required skills</label>
            <p className="mb-2 text-xs text-slate-500 dark:text-slate-400">Select one or more skills from the catalog.</p>
            <div className="max-h-48 overflow-y-auto rounded-xl border border-slate-200/90 bg-white p-3 shadow-sm dark:border-slate-700 dark:bg-slate-950">
              <div className="grid gap-2 sm:grid-cols-2">
                {skillOptions.map((skill) => (
                  <label
                    key={skill.id}
                    className="flex cursor-pointer items-center gap-2 rounded-lg px-2 py-1.5 text-sm text-slate-700 transition hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-900"
                  >
                    <input
                      type="checkbox"
                      className="rounded border-slate-300 text-brand-600 focus:ring-brand-500/30 dark:border-slate-600"
                      checked={selectedRequiredSkills.includes(skill.name)}
                      onChange={() => toggleRequiredSkill(skill.name)}
                    />
                    <span>{skill.name}</span>
                  </label>
                ))}
              </div>
            </div>
            {selectedRequiredSkills.length > 0 && (
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                Selected: {selectedRequiredSkills.join(', ')}
              </p>
            )}
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600 dark:text-slate-400">Nice-to-have</label>
            <p className="mb-2 text-xs text-slate-500 dark:text-slate-400">Optional skills that strengthen a candidate profile.</p>
            <div className="max-h-48 overflow-y-auto rounded-xl border border-slate-200/90 bg-white p-3 shadow-sm dark:border-slate-700 dark:bg-slate-950">
              <div className="grid gap-2 sm:grid-cols-2">
                {preferredSkillOptions.map((skill) => (
                  <label
                    key={skill.id}
                    className="flex cursor-pointer items-center gap-2 rounded-lg px-2 py-1.5 text-sm text-slate-700 transition hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-900"
                  >
                    <input
                      type="checkbox"
                      className="rounded border-slate-300 text-brand-600 focus:ring-brand-500/30 dark:border-slate-600"
                      checked={selectedPreferredSkills.includes(skill.name)}
                      onChange={() => togglePreferredSkill(skill.name)}
                    />
                    <span>{skill.name}</span>
                  </label>
                ))}
              </div>
            </div>
            {selectedPreferredSkills.length > 0 && (
              <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                Selected: {selectedPreferredSkills.join(', ')}
              </p>
            )}
          </div>
          <JobDescriptionAiBot
            department={department}
            title={title}
            location={location}
            experienceRequired={experienceRequired}
            onApply={applyAiAssist}
          />
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600 dark:text-slate-400">Description *</label>
            <textarea
              required
              rows={14}
              className="input-field resize-y min-h-[280px]"
              placeholder="Select a department above — the assistant will draft a full job description here."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <button
            type="submit"
            disabled={saving}
            className="w-full rounded-xl bg-gradient-to-r from-brand-600 to-brand-500 py-3 text-sm font-bold text-white shadow-md shadow-brand-500/25 transition hover:from-brand-500 hover:to-brand-400 disabled:opacity-60"
          >
            Preview & publish
          </button>
        </form>

        <PublishJobPreviewModal
          open={previewOpen}
          onClose={() => setPreviewOpen(false)}
          onConfirm={publishJob}
          title={title}
          department={department}
          location={location}
          experienceRequired={experienceRequired}
          requiredSkills={selectedRequiredSkills}
          preferredSkills={selectedPreferredSkills}
          description={description}
          publishing={saving}
        />

        <div className="w-full space-y-4">
          {deleteError ? (
            <div className="text-sm text-red-800 dark:text-red-200 rounded-xl bg-red-50 dark:bg-red-950/45 border border-red-100 dark:border-red-900/80 px-4 py-3">
              {deleteError}
            </div>
          ) : null}
          <h2 className="text-sm font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">Live roles ({jobs.length})</h2>
          <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 xl:grid-cols-3">
            {jobs.length === 0 ? (
              <div className="col-span-full rounded-2xl border border-dashed border-slate-300 bg-white/60 px-6 py-14 text-center text-sm text-slate-500 dark:border-slate-700 dark:bg-slate-900/40 dark:text-slate-400">
                No jobs yet. Create your first posting—candidates will match against whatever you publish here.
              </div>
            ) : (
              jobs.map((j) => (
                <article
                  key={j.id}
                  className="group flex flex-col rounded-2xl border border-slate-200/90 bg-white/90 p-5 shadow-soft backdrop-blur transition hover:-translate-y-0.5 hover:shadow-soft-lg dark:border-slate-800 dark:bg-slate-900/80"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="text-lg font-bold text-slate-900 dark:text-white">{j.title}</h3>
                      <div className="mt-2 flex flex-wrap gap-2 text-xs font-medium">
                        <span className="rounded-lg bg-slate-100 px-2.5 py-1 text-slate-700 dark:bg-slate-800 dark:text-slate-300">
                          {j.department ?? '—'}
                        </span>
                        <span className="rounded-lg bg-slate-100 px-2.5 py-1 text-slate-700 dark:bg-slate-800 dark:text-slate-300">
                          {j.location ?? '—'}
                        </span>
                      </div>
                    </div>
                    <div className="flex shrink-0 items-center gap-2">
                      <span
                        className={`rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-wide ${
                          j.active ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300' : 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400'
                        }`}
                      >
                        {j.active ? 'Active' : 'Inactive'}
                      </span>
                      <button
                        type="button"
                        aria-label={`Delete ${j.title}`}
                        title="Delete job"
                        disabled={deletingJobId === j.id}
                        onClick={() => deleteJob(j)}
                        className="inline-flex items-center justify-center rounded-lg border border-rose-200 bg-rose-50 p-2 text-rose-700 transition hover:bg-rose-100 disabled:opacity-50 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-300 dark:hover:bg-rose-950/70"
                      >
                        <IconTrash className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                  <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-4 text-[11px] font-semibold uppercase tracking-wider text-slate-400 dark:border-slate-800">
                    <span>#{j.id}</span>
                    <span className="text-brand-600 dark:text-brand-400">Ready for matching</span>
                  </div>
                </article>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
