import { useEffect, useState } from 'react';
import api from '../services/api';

type AiStatus = { mode: string; label: string; detail: string };

export default function AiStatusChip() {
  const [status, setStatus] = useState<AiStatus | null>(null);

  useEffect(() => {
    api.get<AiStatus>('/auth/ai-status').then((r) => setStatus(r.data)).catch(() => null);
  }, []);

  if (!status) return null;

  const isAi = status.mode === 'ai';

  return (
    <div
      className={`hidden rounded-lg border px-2.5 py-1.5 text-left md:block ${
        isAi
          ? 'border-emerald-200/80 bg-emerald-50/90 dark:border-emerald-900/50 dark:bg-emerald-950/40'
          : 'border-amber-200/80 bg-amber-50/90 dark:border-amber-900/50 dark:bg-amber-950/40'
      }`}
      title={status.detail}
    >
      <div
        className={`text-[10px] font-bold uppercase tracking-wider ${
          isAi ? 'text-emerald-700 dark:text-emerald-300' : 'text-amber-800 dark:text-amber-300'
        }`}
      >
        {status.label}
      </div>
    </div>
  );
}
