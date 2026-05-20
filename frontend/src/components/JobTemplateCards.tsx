import { useEffect, useState } from 'react';
import api from '../services/api';

export type DepartmentTemplate = {
  id: number;
  departmentName: string;
  title: string;
  description: string;
  requiredSkills: string;
  preferredSkills: string;
};

type Props = {
  onApply: (t: DepartmentTemplate) => void;
};

export default function JobTemplateCards({ onApply }: Props) {
  const [templates, setTemplates] = useState<DepartmentTemplate[]>([]);

  useEffect(() => {
    api.get<DepartmentTemplate[]>('/department-templates/catalog').then((r) => setTemplates(r.data)).catch(() => setTemplates([]));
  }, []);

  if (templates.length === 0) return null;

  return (
    <div className="space-y-3">
      <h3 className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">
        Start from template
      </h3>
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {templates.map((t) => (
          <button
            key={t.id}
            type="button"
            onClick={() => onApply(t)}
            className="rounded-xl border border-slate-200 bg-slate-50/80 p-4 text-left transition hover:border-brand-300 hover:bg-brand-50/50 dark:border-slate-700 dark:bg-slate-800/50 dark:hover:border-brand-600"
          >
            <div className="text-[10px] font-bold uppercase tracking-wider text-brand-600 dark:text-brand-400">
              {t.departmentName}
            </div>
            <div className="mt-1 text-sm font-semibold text-slate-900 dark:text-white">{t.title}</div>
            <p className="mt-2 line-clamp-2 text-xs text-slate-500">{t.description}</p>
          </button>
        ))}
      </div>
    </div>
  );
}
