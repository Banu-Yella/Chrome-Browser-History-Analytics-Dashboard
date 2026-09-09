# Browser History Analytics - Implementation Notes

## Frontend changes

- Bootstrap off-canvas navigation is available from the left burger button on every viewport.
- Home and Dashboard remain as quick-access navbar buttons.
- Added Data Explorer at `/data` with tabbed read-only tables for Browser History, Chrome URLs, Visits, History Tags, Tags, Analytics Summary, and Users.
- Added period-aware selector labels: Day, Week, Month, Quarter, Half Year, Year.
- Day uses a date. Week resolves a selected date to Monday-Sunday. Month uses a month input. Quarter uses any starting month and covers three complete calendar months. Half Year uses any starting month and covers six complete calendar months. Year uses a year selector.
- Added generation progress with backend phase, verified record count, percentage, and elapsed time.
- Added selected-period Top Domains, Tag Distribution, and Behaviour Category chart data.
- Dashboard includes line, bar, pie, and radar charts.
- Analytics Summary page shows aggregate min/average/max metrics above the persisted summary table.
- Existing period summaries are reused by the frontend; a duplicate is rejected by the backend and displayed as a Toastify notification.
- Toastify notifications are present for create/update/delete tag actions, manual history-tag actions, generation success/failure, and duplicate-period detection.
- Normalized source folders to lowercase `components` and `pages` so imports remain portable on case-sensitive filesystems.

## Backend changes

- Added `GET /api/browser-history/sync/status` for progress polling.
- Added progress tracking phases for URL sync, visit sync, history tagging, and analytics calculation.
- Fixed browser-history tag replacement to use a direct bulk delete before reinserting tag links.
- Added immutable analytics-period protection using a unique constraint over user + period type + period start + period end and a 409 duplicate response.
- Added optional `startDate` and `endDate` to analytics summary generation for custom complete Quarter/Half Year ranges.
- Added `GET /api/analytics/charts` for period-specific/all-time chart data.
- Added read-only table endpoints: `/api/chrome-urls`, `/api/visits`, and `GET /api/browser-history-tags`.
- Added safe scalar IDs to history-tag JSON and ignored recursive JPA collections in JSON serialization.
- Expanded URL categorization to cover common categories such as AI Tools, Dev Tools, Cloud & Hosting, Food & Restaurants, Finance & Banking, Education, Shopping, News, Travel & Booking, Docs & Productivity, Job & Career, Communication, Gaming, Entertainment, Search, and Social Media.

## Validation

- Frontend source passes ESLint using the available dependency tree.
- A full Vite production build could not be completed in the Linux validation container because the uploaded `node_modules`/Rolldown native optional dependency tree is platform-specific; the final source archive intentionally excludes `node_modules`.
- A full Maven compile could not be run in the validation container because Maven/dependencies were not locally cached and the container could not download them. The source changes should therefore be recompiled locally in STS/Eclipse before committing.

## Local install

Frontend:

```bash
npm install
npm run lint
npm run build
npm run dev
```

Backend:

```bash
./mvnw clean compile
```

Then run Spring Boot on port 8080 and the Vite frontend on port 5173.

## Important data-model note

The current dashboard summary is immutable by period. A new period is generated once and remains available in Analytics History. This prevents accidental regeneration and keeps historical analytics reproducible.

For production-scale history volumes, the next optimization should be server-side pagination and JDBC/JPA batch upsert for Chrome URLs and visits; the present implementation still synchronizes records one at a time.
