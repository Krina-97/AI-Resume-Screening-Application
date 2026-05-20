import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `block rounded-lg px-3 py-2 text-sm font-medium ${
    isActive
      ? 'bg-brand-600 text-white'
      : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800'
  }`;

export default function Layout() {
  const { logout, username, role } = useAuth();
  const { theme, toggle } = useTheme();

  return (
    <div className="min-h-screen flex bg-slate-50 dark:bg-slate-950">
      <aside className="w-64 border-r border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 flex flex-col gap-6">
        <div>
          <div className="text-lg font-semibold text-brand-600">AI Resume Screening</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">Enterprise HR Suite</div>
        </div>
        <nav className="space-y-1">
          <NavLink to="/" end className={linkClass}>
            Dashboard
          </NavLink>
          <NavLink to="/candidates" className={linkClass}>
            Candidates
          </NavLink>
          <NavLink to="/jobs" className={linkClass}>
            Job Descriptions
          </NavLink>
        </nav>
        <div className="mt-auto space-y-2 text-xs text-slate-500">
          <div>Signed in as {username}</div>
          <div>Role: {role}</div>
          <button
            onClick={toggle}
            className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm dark:border-slate-700"
          >
            {theme === 'dark' ? 'Light mode' : 'Dark mode'}
          </button>
          <button
            onClick={logout}
            className="w-full rounded-lg bg-slate-900 px-3 py-2 text-sm text-white dark:bg-slate-100 dark:text-slate-900"
          >
            Logout
          </button>
        </div>
      </aside>
      <main className="flex-1 p-6 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
