import api from '../services/api';

function triggerDownload(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  window.URL.revokeObjectURL(url);
}

function filenameFromDisposition(header?: string, fallback = 'download') {
  if (!header) return fallback;
  const match = /filename="?([^";\n]+)"?/i.exec(header);
  return match?.[1]?.trim() ?? fallback;
}

/** Read JSON error body when axios responseType is blob. */
export async function parseApiError(err: unknown, fallback: string): Promise<string> {
  const axiosErr = err as {
    response?: { data?: Blob | { message?: string }; status?: number };
  };
  const data = axiosErr.response?.data;
  if (data instanceof Blob) {
    try {
      const text = await data.text();
      const json = JSON.parse(text) as { message?: string };
      if (json.message) return json.message;
      return text.slice(0, 300) || fallback;
    } catch {
      return fallback;
    }
  }
  if (data && typeof data === 'object' && 'message' in data && typeof data.message === 'string') {
    return data.message;
  }
  return fallback;
}

async function downloadGet(path: string, fallbackName: string) {
  const response = await api.get(path, { responseType: 'blob' });
  const contentType = response.headers['content-type'] ?? '';
  if (contentType.includes('application/json')) {
    const text = await (response.data as Blob).text();
    const json = JSON.parse(text) as { message?: string };
    throw new Error(json.message ?? 'Export failed');
  }
  const name = filenameFromDisposition(response.headers['content-disposition'], fallbackName);
  triggerDownload(new Blob([response.data]), name);
}

/** Export all candidates matching current filters (single file). */
export async function downloadFilteredExport(format: 'xlsx' | 'pdf', params: URLSearchParams) {
  const path = format === 'xlsx' ? '/reports/candidates.xlsx' : '/reports/candidates.pdf';
  await downloadGet(`${path}?${params.toString()}`, format === 'xlsx' ? 'candidates.xlsx' : 'candidates.pdf');
}

/** Export one candidate. */
export async function downloadCandidateExport(candidateId: number, format: 'xlsx' | 'pdf') {
  await downloadGet(`/reports/candidates/${candidateId}.${format}`, `candidate-${candidateId}.${format}`);
}

/** Bulk export selected candidates as ZIP (per-candidate files + summary). */
export async function downloadBulkExportZip(ids: number[], formats: ('xlsx' | 'pdf')[]) {
  const response = await api.post('/reports/candidates/bulk-export', { ids, formats }, { responseType: 'blob' });
  const contentType = response.headers['content-type'] ?? '';
  if (contentType.includes('application/json')) {
    const text = await (response.data as Blob).text();
    const json = JSON.parse(text) as { message?: string };
    throw new Error(json.message ?? 'Bulk export failed');
  }
  const name = filenameFromDisposition(response.headers['content-disposition'], 'candidates-export.zip');
  triggerDownload(new Blob([response.data]), name);
}
