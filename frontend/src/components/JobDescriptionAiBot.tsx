import { useCallback, useEffect, useState } from 'react';
import api from '../services/api';

export type JobDescriptionAssistResult = {
  title: string;
  description: string;
  requiredSkills: string[];
  preferredSkills: string[];
  assistantMessage: string;
  aiGenerated: boolean;
};

type Props = {
  department: string;
  title: string;
  location: string;
  experienceRequired: string;
  onApply: (result: JobDescriptionAssistResult) => void;
};

export default function JobDescriptionAiBot({ department, title, location, experienceRequired, onApply }: Props) {
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [aiGenerated, setAiGenerated] = useState(false);
  const [variant, setVariant] = useState(0);

  const runAssist = useCallback(
    async (payload: {
      department: string;
      title?: string;
      location?: string;
      experienceRequired?: string;
      regenerate?: boolean;
      variant?: number;
    }) => {
      setLoading(true);
      try {
        const res = await api.post<JobDescriptionAssistResult>('/jobs/ai-assist', payload);
        onApply(res.data);
        setMessage(res.data.assistantMessage);
        setAiGenerated(res.data.aiGenerated);
      } catch {
        setMessage('Could not generate a job draft for this department. Try again or fill the form manually.');
        setAiGenerated(false);
      } finally {
        setLoading(false);
      }
    },
    [onApply]
  );

  useEffect(() => {
    if (!department) {
      setMessage('');
      setAiGenerated(false);
      return;
    }
    setVariant(0);
    runAssist({ department, regenerate: false, variant: 0 });
  }, [department, runAssist]);

  const handleRegenerate = () => {
    if (!department) {
      return;
    }
    const nextVariant = variant + 1;
    setVariant(nextVariant);
    runAssist({
      department,
      title: title || undefined,
      location: location || undefined,
      experienceRequired: experienceRequired || undefined,
      regenerate: true,
      variant: nextVariant
    });
  };

  if (!department) {
    return (
      <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/40">
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Select a department above and the AI assistant will draft skills and a job description for you.
        </p>
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-brand-200/80 bg-gradient-to-br from-brand-50/90 to-white p-4 shadow-sm dark:border-brand-900/50 dark:from-brand-950/30 dark:to-slate-900/80">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-600 text-sm font-bold text-white shadow-sm">
            AI
          </span>
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">Job description assistant</h3>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              {aiGenerated ? 'Powered by AI' : 'Using department template'}
              {' · '}
              {department}
            </p>
          </div>
        </div>
        <button
          type="button"
          onClick={handleRegenerate}
          disabled={loading}
          className="shrink-0 rounded-lg border border-brand-300 bg-white px-3 py-1.5 text-xs font-semibold text-brand-700 transition hover:bg-brand-50 disabled:opacity-60 dark:border-brand-800 dark:bg-slate-900 dark:text-brand-300 dark:hover:bg-brand-950/50"
        >
          {loading ? 'Working…' : 'Regenerate'}
        </button>
      </div>
      <div className="mt-3 rounded-xl bg-white/80 px-3 py-2.5 text-sm text-slate-700 dark:bg-slate-950/60 dark:text-slate-300">
        {loading && !message ? (
          <span className="text-brand-600 dark:text-brand-400">Analyzing {department} and selecting skills…</span>
        ) : (
          message || 'Ready to generate your job posting.'
        )}
      </div>
    </div>
  );
}
