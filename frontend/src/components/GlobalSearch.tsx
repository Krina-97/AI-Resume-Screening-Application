import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function GlobalSearch() {
  const [q, setQ] = useState('');
  const navigate = useNavigate();

  const submit = (e: FormEvent) => {
    e.preventDefault();
    const query = q.trim();
    if (!query) return;
    navigate(`/candidates?q=${encodeURIComponent(query)}`);
    setQ('');
  };

  return (
    <form onSubmit={submit} className="hidden min-w-0 flex-1 max-w-md lg:flex">
      <label className="sr-only" htmlFor="global-search">
        Search candidates
      </label>
      <input
        id="global-search"
        type="search"
        placeholder="Search name or email… (press /)"
        className="input-field w-full py-2 text-sm"
        value={q}
        onChange={(e) => setQ(e.target.value)}
      />
    </form>
  );
}
