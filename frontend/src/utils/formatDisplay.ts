/** Fix UTF-8 mojibake (e.g. â€“ → –) in API text. */
export function normalizeDisplayText(raw: string | null | undefined): string {
  if (!raw) return raw ?? '';
  return raw
    .replace(/â€“/g, '–')
    .replace(/â€”/g, '—')
    .replace(/â€™/g, "'")
    .replace(/â€œ/g, '"')
    .replace(/â€\u009d/g, '"')
    .replace(/\u00a0/g, ' ')
    .replace(/\u00e2\u0080\u0093/g, '–')
    .replace(/\u00e2\u0080\u0094/g, '—');
}

/**
 * Prefer API title case ("Priya Sharma") but normalize ALL CAPS or odd casing from legacy rows.
 */
export function formatPersonName(raw: string | null | undefined): string {
  if (!raw?.trim()) return raw ?? '';
  return raw
    .trim()
    .split(/\s+/)
    .map((word) =>
      word.length === 0
        ? word
        : word.charAt(0).toLocaleUpperCase() + word.slice(1).toLocaleLowerCase()
    )
    .join(' ');
}

/** e.g. INTERVIEW_SCHEDULED → Interview scheduled */
export function formatPipelineStatus(raw: string | null | undefined): string {
  if (!raw) return '';
  return raw
    .toLowerCase()
    .split('_')
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}
