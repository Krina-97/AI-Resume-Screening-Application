/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'system-ui', 'Segoe UI', 'Roboto', 'sans-serif']
      },
      colors: {
        brand: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
          950: '#1e1b4b'
        }
      },
      boxShadow: {
        soft:
          '0 2px 15px -3px rgb(15 23 42 / 0.06), 0 10px 24px -4px rgb(15 23 42 / 0.05)',
        'soft-lg':
          '0 8px 30px -6px rgb(15 23 42 / 0.08), 0 18px 40px -12px rgb(79 70 229 / 0.12)',
        glow: '0 0 48px -10px rgb(99 102 241 / 0.45)'
      },
      backgroundImage: {
        'grid-slate':
          'linear-gradient(to right, rgb(148 163 184 / 0.08) 1px, transparent 1px), linear-gradient(to bottom, rgb(148 163 184 / 0.08) 1px, transparent 1px)',
        'fade-brand':
          'radial-gradient(ellipse 80% 60% at 50% -20%, rgb(129 140 248 / 0.22), transparent 55%)'
      },
      backgroundSize: {
        grid: '32px 32px'
      },
      keyframes: {
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' }
        },
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(6px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        }
      },
      animation: {
        shimmer: 'shimmer 2s ease-in-out infinite',
        fadeIn: 'fadeIn 0.4s ease-out forwards'
      }
    }
  },
  plugins: []
};
