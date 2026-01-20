# Frontend Development

This directory contains the React + Vite frontend for the Smart Examination Anti-Cheating System.

## Development Setup

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Start development server:**
   ```bash
   npm run dev
   ```
   - Runs on http://localhost:5173
   - API calls are proxied to Spring Boot backend at http://localhost:8080

3. **Build for production:**
   ```bash
   npm run build
   ```
   - Outputs to `../static/` directory
   - Files are served by Spring Boot in production

## Environment Variables

- **Development:** `.env`
  - `VITE_API_BASE_URL=http://localhost:8080`

- **Production:** `.env.production`
  - `VITE_API_BASE_URL=` (empty for relative URLs)

- **Local overrides:** `.env.local` (gitignored)

## API Proxy Configuration

During development, Vite proxies API calls:
- `/api/*` → `http://localhost:8080/api/*`

## Build Integration

The Maven build automatically:
1. Installs Node.js and npm
2. Runs `npm install`
3. Runs `npm run build`
4. Outputs to Spring Boot's static resources

## Troubleshooting

- **Port conflicts:** Change `server.port` in `vite.config.js`
- **API errors:** Ensure Spring Boot is running on port 8080
- **Build failures:** Check Node.js version (requires 20.19+)