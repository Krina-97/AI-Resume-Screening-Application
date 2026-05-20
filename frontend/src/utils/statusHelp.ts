export const STATUS_HELP: Record<string, string> = {
  NEW: 'Match 40–60% or needs manual review. Not auto-shortlisted.',
  SHORTLISTED: 'Match ≥ 60% vs the target job. Ready for interview scheduling.',
  REJECTED: 'Match below 40% for the target job. Deprioritize unless context changes.',
  INTERVIEW_SCHEDULED: 'Interview booked. Candidate is in the active interview stage.'
};

export const SCORE_BANDS = [
  { min: 60, label: 'Shortlist zone', desc: 'Typically auto-shortlisted (≥ 60%).' },
  { min: 40, label: 'Review zone', desc: 'Manual review recommended (40–59%).' },
  { min: 0, label: 'Low fit', desc: 'Usually rejected (< 40%) unless hiring bar flexes.' }
] as const;
