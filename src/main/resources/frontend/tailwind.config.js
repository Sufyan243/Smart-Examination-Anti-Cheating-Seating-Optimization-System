/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}"
  ],
  theme: {
    extend: {
      colors: {
        safe: '#4CAF50',
        medium: '#FFC107',
        high: '#F44336'
      }
    },
  },
  plugins: [],
}