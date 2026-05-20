import { FormEvent, useState } from 'react';
import api from '../services/api';
import { useToast } from '../context/ToastContext';
import { formatPersonName } from '../utils/formatDisplay';

type Props = {
  open: boolean;
  onClose: () => void;
  candidateId: number;
  candidateName: string;
  onScheduled: () => void;
};

export default function InterviewScheduleModal({ open, onClose, candidateId, candidateName, onScheduled }: Props) {
  const { showToast } = useToast();
  const [scheduledAt, setScheduledAt] = useState('');
  const [interviewerName, setInterviewerName] = useState('');
  const [interviewType, setInterviewType] = useState('Technical');
  const [location, setLocation] = useState('');
  const [meetingLink, setMeetingLink] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  if (!open) return null;

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (!scheduledAt) return;
    setSaving(true);
    try {
      await api.post(`/candidates/${candidateId}/interviews`, {
        scheduledAt: new Date(scheduledAt).toISOString(),
        interviewerName,
        interviewType,
        location,
        meetingLink,
        notes,
        status: 'SCHEDULED'
      });
      showToast(`Interview scheduled for ${formatPersonName(candidateName)}`, 'success');
      onScheduled();
      onClose();
    } catch {
      showToast('Could not schedule interview', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button type="button" className="absolute inset-0 bg-slate-950/50" aria-label="Close" onClick={onClose} />
      <form
        onSubmit={submit}
        className="relative z-10 w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-6 shadow-2xl dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 className="text-lg font-bold text-slate-900 dark:text-white">Schedule interview</h2>
        <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">{formatPersonName(candidateName)}</p>
        <div className="mt-4 space-y-3">
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600">Date & time *</label>
            <input
              type="datetime-local"
              required
              className="input-field"
              value={scheduledAt}
              onChange={(e) => setScheduledAt(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600">Interviewer</label>
            <input className="input-field" value={interviewerName} onChange={(e) => setInterviewerName(e.target.value)} />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600">Type</label>
            <select className="input-field" value={interviewType} onChange={(e) => setInterviewType(e.target.value)}>
              <option>Technical</option>
              <option>HR screen</option>
              <option>Panel</option>
              <option>Final</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600">Location</label>
            <input className="input-field" value={location} onChange={(e) => setLocation(e.target.value)} />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600">Meeting link</label>
            <input className="input-field" value={meetingLink} onChange={(e) => setMeetingLink(e.target.value)} />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-slate-600">Notes</label>
            <textarea className="input-field" rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} />
          </div>
        </div>
        <div className="mt-6 flex justify-end gap-2">
          <button type="button" onClick={onClose} className="rounded-xl border px-4 py-2 text-sm font-semibold">
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="rounded-xl bg-brand-600 px-4 py-2 text-sm font-bold text-white disabled:opacity-50"
          >
            {saving ? 'Saving…' : 'Schedule'}
          </button>
        </div>
      </form>
    </div>
  );
}
