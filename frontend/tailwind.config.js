/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        navy: {
          50: '#eef1f8',
          100: '#d5ddef',
          200: '#aabade',
          300: '#7a93cc',
          400: '#4e72bd',
          500: '#2c5282',
          600: '#1e3a6e',
          700: '#162b57',
          800: '#0f1e40',
          900: '#0a1429',
          950: '#060c1a',
        },
        gold: {
          50: '#fdf9ee',
          100: '#f9f0d0',
          200: '#f3e0a1',
          300: '#edcf72',
          400: '#e7bd43',
          500: '#c9a84c',
          600: '#a8863c',
          700: '#87692e',
          800: '#664f22',
          900: '#453416',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        serif: ['Playfair Display', 'Georgia', 'serif'],
      },
    },
  },
  plugins: [],
}
