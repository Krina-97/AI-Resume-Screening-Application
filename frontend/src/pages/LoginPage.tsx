import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getLoginErrorMessage } from '../utils/loginError';
import { IconSparkles } from '../components/icons';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('hruser');
  const [password, setPassword] = useState('Hr@123456');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError(getLoginErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen overflow-hidden bg-slate-50 dark:bg-slate-950">
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 bg-fade-brand bg-cover opacity-80 dark:opacity-50"
      />
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 bg-grid-slate bg-[length:40px_40px]"
      />
      <div className="relative z-10 mx-auto flex min-h-screen max-w-6xl flex-col lg:flex-row lg:items-stretch">
        <section className="flex flex-col justify-between px-6 py-10 text-slate-900 dark:text-slate-100 lg:w-[46%] lg:px-10 lg:py-14">
          <div>
            <div className="inline-flex items-center gap-2 rounded-full border border-brand-200/80 bg-white/70 px-3 py-1 text-xs font-semibold text-brand-700 shadow-sm backdrop-blur dark:border-brand-500/30 dark:bg-slate-900/60 dark:text-brand-300">
              <IconSparkles className="h-4 w-4 text-brand-500" />
              AI-first hiring intelligence
            </div>
            <h1 className="mt-6 text-3xl font-extrabold tracking-tight sm:text-4xl lg:text-[2.6rem] lg:leading-[1.1]">
              Screen smarter.{' '}
              <span className="bg-gradient-to-r from-brand-600 to-indigo-500 bg-clip-text text-transparent dark:from-brand-400 dark:to-indigo-400">
                Hire faster.
              </span>
            </h1>
            <p className="mt-4 max-w-md text-sm leading-relaxed text-slate-600 dark:text-slate-400">
              Parse resumes, match candidates to live job descriptions, and keep your pipeline in one calm, focused
              workspace—built for HR teams who want velocity without the noise.
            </p>
          </div>
          <ul className="mt-10 hidden gap-6 text-xs font-medium text-slate-500 dark:text-slate-500 lg:grid lg:grid-cols-3">
            <li className="rounded-xl border border-slate-200/80 bg-white/60 p-4 shadow-sm backdrop-blur dark:border-slate-700 dark:bg-slate-900/50">
              <div className="text-2xl font-bold text-slate-900 dark:text-white">PDF & DOCX</div>
              <div className="mt-1">Structured extraction</div>
            </li>
            <li className="rounded-xl border border-slate-200/80 bg-white/60 p-4 shadow-sm backdrop-blur dark:border-slate-700 dark:bg-slate-900/50">
              <div className="text-2xl font-bold text-slate-900 dark:text-white">Match scores</div>
              <div className="mt-1">Role-aware ranking</div>
            </li>
            <li className="rounded-xl border border-slate-200/80 bg-white/60 p-4 shadow-sm backdrop-blur dark:border-slate-700 dark:bg-slate-900/50">
              <div className="text-2xl font-bold text-slate-900 dark:text-white">Pipeline</div>
              <div className="mt-1">Shortlist in one glance</div>
            </li>
          </ul>
        </section>

        <section className="flex flex-1 items-center px-4 py-8 lg:justify-center lg:px-8 lg:py-14">
          <form
            onSubmit={onSubmit}
            className="w-full max-w-md animate-fadeIn rounded-3xl border border-slate-200/80 bg-white/90 p-8 shadow-soft-lg backdrop-blur-xl dark:border-slate-800 dark:bg-slate-900/90 dark:shadow-glow lg:p-10"
          >
            <div className="flex items-center gap-3 lg:hidden mb-8">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-xs font-bold text-white">
                AI
              </div>
              <div>
                <div className="font-bold text-slate-900 dark:text-white">HireSight</div>
                <div className="text-xs text-slate-500">Enterprise HR Suite</div>
              </div>
            </div>
            <div>
              <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">Welcome back</h2>
              <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">Sign in to continue screening and ranking.</p>
            </div>
            {error ? (
              <div
                role="alert"
                className="mt-6 text-sm text-red-700 dark:text-red-300 rounded-xl bg-red-50 dark:bg-red-950/50 border border-red-100 dark:border-red-900/80 px-4 py-3 leading-relaxed"
              >
                {error}
              </div>
            ) : null}
            <div className="mt-8 space-y-5">
              <div>
                <label htmlFor="username" className="mb-1.5 block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                  Username
                </label>
                <input
                  id="username"
                  autoComplete="username"
                  className="input-field"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>
              <div>
                <label
                  htmlFor="password"
                  className="mb-1.5 block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400"
                >
                  Password
                </label>
                <div className="relative">
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    className="input-field pr-24"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((v) => !v)}
                    className="absolute right-2 top-1/2 -translate-y-1/2 rounded-lg px-3 py-1.5 text-xs font-semibold text-brand-700 hover:bg-brand-50 dark:text-brand-300 dark:hover:bg-brand-950/40"
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                    aria-pressed={showPassword}
                  >
                    {showPassword ? 'Hide' : 'Show'}
                  </button>
                </div>
              </div>
            </div>
            <button
              type="submit"
              disabled={loading}
              className="mt-8 w-full rounded-xl bg-gradient-to-r from-brand-600 to-brand-500 py-3 text-sm font-bold text-white shadow-md shadow-brand-500/30 transition hover:from-brand-500 hover:to-brand-400 hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? 'Signing in…' : 'Sign in to dashboard'}
            </button>
            <p className="mt-6 text-center text-xs leading-relaxed text-slate-500 dark:text-slate-500">
              Demo accounts from seed data:{' '}
              <span className="font-mono text-slate-700 dark:text-slate-300">hruser</span> /{' '}
              <span className="font-mono text-slate-700 dark:text-slate-300">Hr@123456</span>
              <span className="mx-1 text-slate-400">·</span>
              <span className="font-mono text-slate-700 dark:text-slate-300">admin</span> /{' '}
              <span className="font-mono text-slate-700 dark:text-slate-300">Admin@123</span>
            </p>
          </form>
        </section>
      </div>
    </div>
  );
}
