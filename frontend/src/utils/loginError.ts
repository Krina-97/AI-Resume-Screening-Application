import axios from 'axios';

export function getLoginErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'Cannot reach the API server. Start the backend: cd backend && mvn spring-boot:run (port 8080).';
    }
    const status = error.response.status;
    const message =
      typeof error.response.data === 'object' &&
      error.response.data !== null &&
      'message' in error.response.data
        ? String((error.response.data as { message: string }).message)
        : null;

    if (status === 401) {
      return message ?? 'Invalid username or password.';
    }
    if (status >= 500) {
      return message ?? 'Server error. Check backend logs.';
    }
    return message ?? `Login failed (HTTP ${status}).`;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Login failed. Please try again.';
}
