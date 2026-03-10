# EU Stats

EU Stats is a self-hosted web analytics project for people who want a simple way to watch traffic on their own site without turning it into a large marketing stack.

In its current form, the project focuses on pageview-oriented analytics:

- pageviews
- top pages
- referrers
- custom event reporting
- a small site management dashboard

The bundled tracker is intentionally small, but it is not storage-free in its current form. It does not use cookies and it does not use `localStorage`, but it does use `sessionStorage` to keep a per-tab identifier for heartbeat-based live presence tracking. It also respects Do Not Track.

## What The Project Does

At a high level, EU Stats gives you:

- A Spring Boot backend that receives tracking payloads and stores them in PostgreSQL
- A React frontend for managing sites and viewing analytics
- A tiny tracker script served by the backend at `/s.js?id=<SITE_ID>`
- Scheduled backend jobs that build aggregated reporting tables from raw pageview rows

This means you can install one script on your page, open the dashboard, and see traffic trends without needing an external analytics provider.

## What It Tracks Right Now

The default tracker currently sends:

- Site identifier
- Page path
- Page title
- Referrer domain
- User agent string
- Event type
- Event name
- Timestamp

The built-in tracker currently sends:

- Pageviews
- Heartbeats for live presence tracking
- Generic `link_click` events

The backend also enriches requests with hashed visitor identity, device data, and country lookups before storing raw events.

## Privacy Direction

The project currently takes a deliberately reduced-data approach:

- No cookies
- No `localStorage`
- Uses `sessionStorage` only for a per-tab live presence identifier
- No raw internet protocol persistence in application logic
- Visitor identity is derived from a daily-rotated server-side hash
- Path-only page address storage
- Referrer stored as domain only
- Do Not Track respected in the tracker

Important:

- This is a technical privacy posture, not a legal certification
- You still need your own privacy notice on the tracked site
- You should still review reverse proxy, web server, hosting, and database logs separately

## How It Works

The flow is simple:

1. You create a site in the frontend.
2. The backend gives you a snippet like:

```html
<script src="http://localhost:8080/s.js?id=1"></script>
```

3. You place that script on your website.
4. When the page loads, the tracker sends a pageview payload to `POST /api/c`.
5. While the tab stays visible, the tracker also sends heartbeat events and uses a per-tab `sessionStorage` key so the backend can estimate live presence.
6. The backend normalizes and enriches incoming rows, then stores them in PostgreSQL.
7. Scheduled jobs aggregate those raw rows into reporting tables.
8. The frontend reads the aggregated data through `/api/sites/{siteId}/d/*`.

This split keeps collection simple and pushes heavier reporting work into scheduled backend processing.

## Tech Stack

### Backend

- Java 21
- Spring Boot 3.4.2
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Actuator
- Liquibase
- PostgreSQL JDBC driver

### Frontend

- React 19
- TypeScript 5.9
- Vite 7
- Tailwind CSS 4
- Axios
- React Router

### Database

- PostgreSQL

### Tracker

- TypeScript source in `frontend/tracker/tracker.ts`
- Compiled into `backend/src/main/resources/static/tracker.js`

## Project Structure

- `backend/`
  Spring Boot application, REST endpoints, ingestion, aggregation jobs, and tracker delivery
- `frontend/`
  React application for site management and analytics pages
- `frontend/tracker/`
  Tracker source code that compiles into the backend static asset
- `docker-compose.yml`
  Local PostgreSQL service for development
- `backend/src/main/resources/db/`
  Liquibase changelog and schema bootstrap SQL

## Requirements

You need:

- Java 21
- Maven
- Node.js and npm
- PostgreSQL

Docker is optional, but it is the easiest way to get a local PostgreSQL instance running.

## Database

EU Stats needs PostgreSQL.

Default local values are:

- Database: `analytics`
- User: `eu-stats`
- Password: `eu-stats`
- Host port: `5430`
- Container port: `5432`

### Using `docker-compose.yml`

If Docker is running on your machine, you can create the database service directly from the repository root:

```bash
docker compose up -d postgres
```

What this does:

- Pulls the configured PostgreSQL image if it is not present
- Starts a container named `eu-stats-postgres`
- Creates the database using the values from `docker-compose.yml`
- Exposes the database on `localhost:5430`
- Persists data in the Docker volume `pgdata`

If you want to reset the local database completely:

```bash
docker compose down -v
docker compose up -d postgres
```

### If You Do Not Want Docker

You can use any PostgreSQL instance you already have, as long as you provide matching connection settings through environment variables.

### Schema Creation

The backend uses Liquibase on startup.

That means:

- The database itself must exist before the backend starts
- Liquibase creates the tables and indexes
- The bootstrap SQL in `backend/src/main/resources/db/migration/create_fresh_schema.sql` initializes the schema structure

## Partitioning Note

The `pageviews` table is defined as a range-partitioned table on `viewed_at`.

Right now, the schema includes:

- `pageviews` as the partitioned parent table
- `pageviews_default` as the default partition

Important:

- Dedicated month-by-month partitions are not created automatically at the moment
- If you want one table per month, a partition for the upcoming month should be created ahead of time
- That creation should ideally be automated with a scheduled database job, a Liquibase change process, or an operational script

So, for now:

- The project works with the default partition
- Monthly partition creation is a future operational improvement, not something the current code automates for you

## Configuration

The backend reads configuration from:

- `backend/.env`
- `.env` in the repository root

Example backend configuration:

```env
DB_HOST=localhost
DB_PORT=5430
DB_NAME=analytics
DB_USERNAME=eu-stats
DB_PASSWORD=eu-stats

APP_TRACKER_BASE_URL=http://localhost:8080
APP_DATA_RETENTION_MONTHS=24
APP_AGGREGATION_LOOKBACK_DAYS=2
APP_CORS_ORIGINS=http://localhost:5173,http://localhost
```

For the frontend, you only need an override file if the backend is not available at the default location:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Quick Start

### 1. Install npm Workspace Dependencies

From the repository root:

```bash
npm install
```

This installs the root-level helper dependency and the frontend workspace dependencies used by the root build scripts.

### 2. Start PostgreSQL

If Docker is running:

```bash
docker compose up -d postgres
```

### 3. Start Backend And Frontend Together

From the repository root:

```bash
npm run all
```

What `npm run all` does:

- Starts the frontend Vite dev server
- Starts the backend Spring Boot application
- Runs both processes at the same time in one terminal using `concurrently`

Under the hood it runs:

- `npm run dev:frontend`
- `npm run dev:backend`

### 4. Open The App

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`

### 5. Create A Site

1. Open the dashboard
2. Add a site
3. Copy the generated tracker snippet
4. Place it on your page

### 6. View Analytics

After the page receives traffic, open the analytics page for that site in the dashboard.

## Root Scripts

These commands are intended to be run from the repository root.

### `npm run all`

Starts the frontend and backend together for development.

Use this when you want the full project running locally in one terminal.

### `npm run build`

Builds the whole project in the right order:

1. Rebuilds the tracker asset
2. Builds the frontend production bundle
3. Builds the backend package

Under the hood it runs:

- `npm run build:frontend`
- `npm run build:backend`

`npm run build:frontend` rebuilds `backend/src/main/resources/static/tracker.js` from `frontend/tracker/tracker.ts` before the Vite production bundle is created.

Important:

- The backend build uses `mvn -B clean package -Dmaven.test.skip=true`
- That means test execution and test compilation are both skipped in the root build command

### Other Useful Root Scripts

- `npm run build:tracker`
  Rebuilds `backend/src/main/resources/static/tracker.js` from `frontend/tracker/tracker.ts`
- `npm run build:frontend`
  Rebuilds the tracker asset and then builds the React app
- `npm run build:backend`
  Builds the Spring Boot application jar and skips test compilation and test execution
- `npm run dev:frontend`
  Starts only the frontend dev server
- `npm run dev:backend`
  Starts only the backend

## Module-Level Commands

### Backend

```bash
cd backend
mvn spring-boot:run
mvn test
mvn clean package
```

### Frontend

```bash
cd frontend
npm run dev
npm run build
npm run preview
```

`npm run build` in `frontend/` also regenerates `../backend/src/main/resources/static/tracker.js`.

### Tracker Only

```bash
cd frontend
npm run build:tracker
```

## API Surface

### Site Management

- `GET /api/sites`
- `POST /api/sites`
- `GET /api/sites/{siteId}`
- `PUT /api/sites/{siteId}`
- `DELETE /api/sites/{siteId}`
- `GET /api/sites/{siteId}/snippet`

### Tracker

- `GET /s.js?id=<SITE_ID>`
- `POST /api/c`

### Analytics Reads

- `GET /api/sites/{siteId}/d/summary`
- `GET /api/sites/{siteId}/d/content`
- `GET /api/sites/{siteId}/d/sources`
- `GET /api/sites/{siteId}/d/actions`

## Scheduled Jobs

### `AggregateStatsJob`

Runs on a short interval and refreshes reporting tables from recent raw pageview rows.

### `DataRetentionJob`

Deletes raw pageview rows older than the configured retention window.

## Current Product Shape

The current project is intentionally smaller than a full analytics suite.

What is included:

- Pageview collection
- Top page reporting
- Referrer reporting
- Event reporting support
- Site management

What is not currently part of the default experience:

- Cookie banners
- Browser-storage-based identity
- Real-time visitor presence widgets
- Device and operating system charts
- Geographic charts

## Notes

- The README describes the current codebase as it exists now
- If you later reintroduce richer analytics dimensions, update both the tracker and the README together
- If you want production use, you should still add deployment notes for reverse proxy, HTTPS, backups, and privacy policy handling
