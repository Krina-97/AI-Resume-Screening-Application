import { NavLink, Outlet } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import GlobalSearch from './GlobalSearch';
import AiStatusChip from './AiStatusChip';
import {
  IconBriefcase,
  IconClose,
  IconDashboard,
  IconLogout,
  IconMenu,
  IconMoon,
  IconSun,
  IconUsers
} from './icons';

const baseLink =
  'group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all duration-200';

function navClass({ isActive }: { isActive: boolean }) {
  return isActive
    ? `${baseLink} bg-gradient-to-r from-brand-600 to-brand-500 text-white shadow-md shadow-brand-500/25`
    : `${baseLink} text-slate-600 hover:bg-white/80 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800/80 dark:hover:text-white`;
}

export default function Layout() {
  const { logout, username, role } = useAuth();
  const { theme, toggle } = useTheme();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    const onResize = () => {
      if (window.innerWidth >= 768) setSidebarOpen(false);
    };
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === '/' && !['INPUT', 'TEXTAREA', 'SELECT'].includes((e.target as HTMLElement)?.tagName)) {
        e.preventDefault();
        document.getElementById('global-search')?.focus();
      }
      if (e.key === 'n' && !e.ctrlKey && !e.metaKey && !['INPUT', 'TEXTAREA'].includes((e.target as HTMLElement)?.tagName)) {
        const path = window.location.pathname;
        if (path.startsWith('/candidates')) {
          window.dispatchEvent(new CustomEvent('hiresight:open-upload'));
        }
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  const closeMobile = () => setSidebarOpen(false);

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-950 md:flex-row md:items-start">
      {sidebarOpen ? (
        <button
          type="button"
          aria-label="Close menu"
          className="fixed inset-0 z-40 bg-slate-950/50 backdrop-blur-sm md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      ) : null}

      <header className="sticky top-0 z-30 flex items-center justify-between border-b border-slate-200/80 bg-white/90 px-4 py-3 backdrop-blur-md dark:border-slate-800 dark:bg-slate-900/90 md:hidden">
        <div className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-brand-500 to-brand-700 text-sm font-bold text-white shadow-glow">
            AI
          </div>
          <div>
            <div className="text-sm font-semibold text-slate-900 dark:text-white">HireSight</div>
            <div className="text-[10px] font-medium uppercase tracking-wider text-brand-600 dark:text-brand-400">
              Resume intelligence
            </div>
          </div>
        </div>
        <button
          type="button"
          className="rounded-lg border border-slate-200 p-2 text-slate-700 dark:border-slate-700 dark:text-slate-200"
          onClick={() => setSidebarOpen((v) => !v)}
          aria-expanded={sidebarOpen}
        >
          {sidebarOpen ? <IconClose /> : <IconMenu />}
        </button>
      </header>

      <aside
        className={`fixed bottom-0 left-0 top-14 z-50 flex h-[calc(100dvh-3.5rem)] w-[min(18rem,85vw)] shrink-0 flex-col overflow-y-auto border-r border-slate-200/80 bg-white/95 p-4 shadow-soft-lg backdrop-blur-xl transition-transform duration-300 dark:border-slate-800 dark:bg-slate-900/98 md:sticky md:top-0 md:h-dvh md:max-h-dvh md:w-64 md:translate-x-0 md:shadow-none ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        }`}
      >
        <div className="mb-6 flex items-center gap-3 md:hidden">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-xs font-bold text-white">
            AI
          </div>
          <div>
            <div className="text-sm font-bold text-slate-900 dark:text-white">HireSight</div>
            <div className="text-[10px] font-semibold uppercase tracking-wider text-brand-600 dark:text-brand-400">
              Menu
            </div>
          </div>
        </div>
        <div className="mb-8 hidden items-center gap-3 md:flex">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 via-brand-600 to-indigo-700 text-sm font-bold text-white shadow-glow">
            AI
          </div>
          <div>
            <div className="text-lg font-bold tracking-tight text-slate-900 dark:text-white">HireSight</div>
            <div className="text-[11px] font-medium uppercase tracking-widest text-brand-600 dark:text-brand-400">
              Enterprise HR
            </div>
          </div>
        </div>

        <nav className="flex flex-col gap-1">
          <NavLink to="/" end className={navClass} onClick={closeMobile}>
            <IconDashboard className="opacity-90" />
            Dashboard
          </NavLink>
          <NavLink to="/candidates" className={navClass} onClick={closeMobile}>
            <IconUsers className="opacity-90" />
            Candidates
          </NavLink>
          <NavLink to="/jobs" className={navClass} onClick={closeMobile}>
            <IconBriefcase className="opacity-90" />
            Job descriptions
          </NavLink>
        </nav>

        <div className="mt-auto space-y-3 border-t border-slate-200/80 pt-4 dark:border-slate-800">
          <div className="rounded-xl bg-slate-100/80 px-3 py-2.5 dark:bg-slate-800/80">
            <div className="text-[10px] font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Signed in
            </div>
            <div className="truncate text-sm font-semibold text-slate-900 dark:text-white">{username}</div>
            <div className="mt-0.5 text-xs text-slate-600 dark:text-slate-400">{role}</div>
          </div>
          <button
            type="button"
            onClick={toggle}
            className="flex w-full items-center justify-center gap-2 rounded-xl border border-slate-200 px-3 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
          >
            {theme === 'dark' ? (
              <>
                <IconSun /> Light mode
              </>
            ) : (
              <>
                <IconMoon /> Dark mode
              </>
            )}
          </button>
          <button
            type="button"
            onClick={logout}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-slate-900 px-3 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            <IconLogout />
            Sign out
          </button>
        </div>
      </aside>

      <main className="relative min-h-0 min-w-0 flex-1 overflow-auto md:min-h-dvh">
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0 bg-fade-brand bg-cover opacity-70 dark:opacity-40"
        />
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0 bg-grid-slate bg-[length:40px_40px]"
        />
        <div className="hidden border-b border-slate-200/80 bg-white/70 px-4 py-2 backdrop-blur dark:border-slate-800 dark:bg-slate-900/70 md:flex md:items-center md:gap-4 md:px-8">
          <GlobalSearch />
          <AiStatusChip />
        </div>
        <div className="relative mx-auto max-w-7xl px-4 py-6 md:px-8 md:py-10">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
