# Chrome Browser History Analytics Dashboard

A full-stack browser-history analytics application that imports Google Chrome browsing data, persists it into a structured MySQL analytics model, automatically classifies visited websites using configurable tags, calculates time- and period-based analytics, and presents the results through an interactive React dashboard.

The project is designed around a simple principle:

> **Raw browsing events are the source of truth; analytics summaries are derived records that can be preserved and reviewed later.**

---

## Project Overview

The application reads the local Google Chrome `History` SQLite database and synchronizes Chrome URL and visit records into MySQL. The backend then builds application-level browser-history records, applies configurable automatic tags, calculates analytics for a selected period, and exposes REST APIs consumed by the React frontend.

The frontend provides:

- A responsive Bootstrap navigation bar with an off-canvas workspace menu
- Dashboard analytics and visualizations
- Browser history review
- Tag management
- Saved analytics summary history
- Backend Data Explorer tables
- Period selection for day, week, month, quarter, half year, and year
- Toast notifications for CRUD and duplicate-period events
- Progress reporting while Chrome history is being synchronized and validated
- Table row-count controls and pagination
- Per-summary chart exploration

---

## Main Features

### Chrome History Synchronization

The backend creates a snapshot of Chrome's local SQLite `History` database before reading it. This allows the application to work from a copied database file instead of directly operating on Chrome's active database.

The synchronization pipeline imports:

- Chrome URLs
- Chrome visit events
- Visit timestamps
- Visit duration
- Chrome transition information
- Typed-navigation indicators
- Domain information
- Category information
- Aggregated time spent

The current synchronization process also exposes progress information for the frontend.

### Automatic Tagging

Tags are configurable records containing:

- Tag name
- Category
- Description
- Comma-separated matching keywords
- Created/updated timestamps

The tag service converts configured keywords into a normalized list and the history-tag service applies matching tags to browser-history records.

Examples of useful categories include:

- AI Tools
- Development
- Cloud & Hosting
- Social Media
- Search Engines
- Shopping
- Food & Restaurants
- Entertainment
- Gaming
- Finance & Banking
- Education
- Job & Career
- Travel & Booking
- Docs & Productivity
- Email & Messaging
- News
- Maps & Navigation
- Video & Streaming

### Analytics Periods

The application supports the following analytics periods:

| Period | Meaning |
|---|---|
| Day | One selected calendar date |
| Week | Seven-day range beginning from the resolved week start |
| Month | One calendar month |
| Quarter | Any three consecutive months selected by the user |
| Half Year | Any six consecutive months selected by the user |
| Year | Full calendar year |

A period is resolved into a concrete `periodStart` and `periodEnd` before analytics are calculated.

### Duplicate Period Protection

An analytics period is treated as the same analytical record only when its identity matches the same user, period type, period start, and period end.

Therefore, overlapping periods are allowed.

For example:

- Day: September 1
- Week: September 1–7
- Month: September 1–30
- Quarter: July 1–September 30

These are different analytical records even though their date ranges overlap.

Requesting the exact same user + period type + start + end again is treated as an existing analytics period rather than generating a duplicate summary.

### Analytics Summary

Each saved analytics summary captures calculated values such as:

- Total visits
- Unique domains
- Top domain
- Top tag
- Most active hour
- Total time spent
- Average time spent
- Minimum time spent
- Maximum time spent
- Minimum visit count
- Average visit count
- Maximum visit count
- Created timestamp
- Updated timestamp

The Analytics History page preserves these records for later review.

### Dashboard Visualizations

The dashboard uses Recharts to provide multiple views of browsing behavior:

- **Line chart** — analytics trend across saved periods
- **Bar chart** — top domains by visit volume
- **Pie / donut chart** — tag distribution
- **Radar chart** — category-oriented browsing behavior

The pie chart is intentionally sorted and limits direct slice labels so small categories do not visually collide. Exact values remain available through the chart tooltip and legend.

### Per-Row Analytics Exploration

The Analytics History table includes a row-level chart action.

When a user opens a saved summary's charts:

1. Charts appear above the table.
2. The selected summary becomes the active chart context.
3. Opening another row automatically replaces the previous chart set.
4. Closing the chart panel removes it from view.

This keeps the page focused while still allowing deep exploration of individual periods.

### Progress Monitoring

Synchronization progress is exposed from the backend and represented in the frontend through a progress indicator.

The workflow reports stages such as:

- Starting
- Synchronizing URLs
- Synchronizing visits
- Tagging history
- Calculating analytics
- Completed
- Failed

Progress information can include processed records, total records where known, current phase, status message, and elapsed time.

### Data Explorer

The Data Explorer exposes read-oriented views over the backend's main operational tables.

Current views include:

- Browser History
- Chrome URLs
- Visits
- History Tags
- Tags
- Analytics Summary
- Users

The frontend provides pagination / row-count controls for these views.

### Notifications

React Toastify is used for user feedback, including:

- Create success
- Update success
- Delete success
- Manual tag changes
- Duplicate analytics-period detection
- Synchronization completion
- Request failures

---

## System Architecture

```text
                         Chrome Browser
                              |
                              | local History SQLite DB
                              v
                 +-----------------------------+
                 | Chrome History Reader       |
                 | - snapshot database         |
                 | - read urls                 |
                 | - read visits               |
                 +-------------+---------------+
                               |
                               v
                 +-----------------------------+
                 | Spring Boot Backend          |
                 |                              |
                 | Browser History Services     |
                 | Visit Services               |
                 | Tag Services                |
                 | Analytics Services           |
                 | Progress Service             |
                 +-------------+---------------+
                               |
                               v
                         +-----------+
                         |   MySQL   |
                         +-----------+
                               ^
                               |
                          REST / JSON
                               |
                 +-------------+---------------+
                 | React + Vite Frontend        |
                 |                              |
                 | Dashboard                   |
                 | Tags                        |
                 | Browser History              |
                 | Analytics History            |
                 | Data Explorer                |
                 +-----------------------------+
```

---

## Backend Architecture

The Spring Boot backend is organized by responsibility/domain.

```text
com.chrome_history_dashboard
│
├── analytics_chart_controller
├── analytics_summary_controller
├── analytics_summary_entity
├── analytics_summary_repo
├── analytics_summary_service
│
├── browser_history_controller
├── browser_history_entity
├── browser_history_repo
├── browser_history_service
│
├── browser_history_tag_controller
├── browser_history_tag_entity
├── browser_history_tag_repo
├── browser_history_tag_service
│
├── chrome_url_controller
├── visit_controller
├── visit_service
│
├── tag_controller
├── tag_entity
├── tag_repo
├── tag_service
│
├── user_controller
├── user_entity
├── user_repo
├── user_service
│
├── progress
├── config
└── exception
```

### Main Backend Responsibilities

**`Chrome_History_Reader_Service`**

Reads Chrome's SQLite history snapshot and synchronizes URL and visit information.

**`Browser_History_Service`**

Builds application-level browser history data and applies automatic tags.

**`Browser_History_Tag_Service`**

Maintains history/tag relationships, automatic tagging, manual tagging, tag distribution, top tags, and confidence calculations.

**`Tag_Service`**

Creates, updates, deletes, and retrieves configurable tags.

**`Visit_Service`**

Provides visit retrieval and session-grouping functionality.

**`Analytics_Summary_Service`**

Resolves requested periods, queries visits inside the period, calculates analytics aggregates, and persists analytics summaries.

**`Sync_Progress_Service`**

Tracks current synchronization phase, processed records, status, and completion/failure state.

---

## Database Model

The main MySQL entities are:

```text
User
  |
  +---- Analytics_Summary

Chrome_Url
  |
  +---- Visit

Browser_History
  |
  +---- Browser_History_Tag ---- Tag
```

### Main Tables

| Table | Purpose |
|---|---|
| `user` | Application user information |
| `chrome_url_entity` | Synchronized records from Chrome's `urls` table plus analytics fields |
| `visit_entity` | Individual Chrome visit events |
| `browser_history_entity` | Application-level browser history records |
| `tag` | Configurable automatic/manual tags |
| `browser_history_tag` | Many-to-many relationship between browser history and tags |
| `analytics_summary` | Preserved calculated summaries for requested periods |

### Important Data Principles

#### Raw events are preserved

`Visit_Entity` represents individual browser visit events and is the primary source for time-period analytics.

#### Summaries are derived

An analytics summary is a calculated representation of raw visit data for a defined period. Daily, weekly, monthly, quarterly, half-yearly, and yearly summaries are independent analytical views of the same underlying events.

#### Overlapping periods are valid

A day can belong to a week, month, quarter, half year, and year without those analytical records being duplicates.

#### Exact duplicate summaries are rejected

The business identity of a summary is:

```text
User + Period Type + Period Start + Period End
```

---

## Frontend Architecture

The React application is built with Vite and uses React Router for page navigation.

```text
src/
│
├── api/
│   ├── analyticsApi.js
│   ├── axiosClient.js
│   ├── browserHistoryApi.js
│   ├── browserHistoryTagApi.js
│   └── tagApi.js
│
├── components/
│   ├── NavBar.jsx
│   ├── PeriodSelector.jsx
│   ├── AnalyticsTrendChart.jsx
│   ├── TopDomainBarChart.jsx
│   ├── TagDistributionChart.jsx
│   ├── CategoryRadarChart.jsx
│   ├── AnalyticsAggregateCards.jsx
│   ├── AnalyticsSummaryCards.jsx
│   ├── TagForm.jsx
│   └── TagTable.jsx
│
├── pages/
│   ├── HomePage.jsx
│   ├── Dashboard.jsx
│   ├── TagManagerPage.jsx
│   ├── BrowserHistoryPage.jsx
│   ├── AnalyticsHistoryPage.jsx
│   └── DataExplorerPage.jsx
│
├── utils/
│   ├── period.js
│   ├── category.js
│   ├── format.js
│   └── tagApi.js
│
├── styles/
├── App.jsx
├── App.css
├── index.css
└── main.jsx
```

### Frontend Routes

| Route | Purpose |
|---|---|
| `/home` | Home / landing page |
| `/` | Analytics Dashboard |
| `/tags` | Tag management |
| `/history` | Browser history review |
| `/analytics-history` | Saved analytics summaries and row-level charts |
| `/data` | Data Explorer |

---

## REST API Overview

### Browser History

```text
POST /api/browser-history/sync
GET  /api/browser-history/sync/status
GET  /api/browser-history
GET  /api/browser-history/{id}
GET  /api/browser-history/{id}/tags
GET  /api/browser-history/top-domains
GET  /api/browser-history/stats
```

### Analytics

```text
POST /api/analytics/summary
GET  /api/analytics/summary/history
GET  /api/analytics/charts
```

### Tags

```text
GET    /api/tags
GET    /api/tags/{id}
POST   /api/tags
PUT    /api/tags/{id}
DELETE /api/tags/{id}
```

### Browser History Tags

```text
POST   /api/browser-history-tags
DELETE /api/browser-history-tags/{id}
GET    /api/browser-history-tags
GET    /api/browser-history-tags/distribution
GET    /api/browser-history-tags/top
GET    /api/browser-history-tags/average-confidence
```

### Chrome URLs / Visits / Users

```text
GET /api/chrome-urls
GET /api/visits
GET /api/users/user
```

---

## Technology Stack

### Frontend

- React 19
- Vite
- React Router
- Axios
- Bootstrap 5
- React Toastify
- Recharts
- React Hook Form
- Zod
- ESLint

### Backend

- Java
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA / Hibernate
- Bean Validation
- Lombok
- MySQL Connector/J
- SQLite JDBC
- Spring Boot DevTools
- Maven / Maven Wrapper

### Data Stores

- **Chrome SQLite History DB** — source data
- **MySQL** — normalized application and analytics data

---

## Local Development Requirements

Install or have available:

- Java
- Node.js and npm
- MySQL 8.x
- Google Chrome
- Git

The backend project includes Maven Wrapper files, so Maven does not have to be installed globally.

---

## Backend Setup

Create the MySQL database:

```sql
CREATE DATABASE chrome_browser_history;
```

Configure the backend datasource in `application.properties` or through environment variables.

Example structure:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chrome_browser_history?useSSL=false&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Do not commit real database credentials to GitHub.

From the backend project directory on Windows:

```cmd
mvnw.cmd spring-boot:run
```

Or start `ChromeHistoryDashboardApplication` directly from STS / Eclipse.

The backend normally runs at:

```text
http://localhost:8080
```

---

## Frontend Setup

From the React project directory:

```bash
npm install
npm run dev
```

The Vite development server normally runs at:

```text
http://localhost:5173
```

Useful scripts:

```bash
npm run dev
npm run build
npm run lint
npm run preview
```

The frontend Axios client expects the backend API to be available on the configured backend URL.

---

## Typical User Workflow

```text
Open Dashboard
      |
      v
Select analytical period
      |
      v
Resolve calendar/date range
      |
      v
Check whether exact summary already exists
      |
      +---- Yes ----> Reuse saved summary + Toast notification
      |
      No
      |
      v
Synchronize Chrome history
      |
      v
Validate / store URLs
      |
      v
Validate / store visits
      |
      v
Apply automatic tags
      |
      v
Calculate selected-period analytics
      |
      v
Persist Analytics_Summary
      |
      v
Refresh dashboard charts
```

---

## Analytics Philosophy

The application deliberately separates **data collection** from **analytics interpretation**.

The underlying browser events remain available so that different period definitions can be calculated independently.

For example, a user can generate:

```text
Day       → 2026-09-01
Day       → 2026-09-02
...
Day       → 2026-09-07

Week      → 2026-09-01 through 2026-09-07

Month     → September 2026

Quarter   → July through September 2026

Half Year → April through September 2026

Year      → Calendar year 2026
```

The weekly or quarterly calculation should use the underlying visit events belonging to that range; it should not depend on previously generated daily summaries.

This prevents the analytics system from becoming dependent on which summaries happened to be generated first.

---

## Performance Considerations

The initial Chrome synchronization can take time because Chrome history may contain thousands of URL and visit records. The frontend therefore exposes progress and elapsed time while synchronization is running.

Current synchronization is designed for a personal/local analytics workload. For larger deployments, planned improvements include:

- Incremental synchronization instead of reprocessing the complete Chrome history each time
- JDBC/JPA batching with `saveAll` and batch inserts/updates
- Database-side pagination
- User/job-scoped progress tracking
- More period-specific aggregate queries
- Background job execution for large imports
- Caching of frequently requested analytics

---

## Security Notes

This project reads local browser history, which can contain highly sensitive browsing information.

Recommended practices:

- Keep database passwords out of source control.
- Do not commit `.env` files containing secrets.
- Restrict API access when exposing the backend beyond localhost.
- Use authentication and authorization before multi-user deployment.
- Treat exported browser-history data as sensitive personal information.
- Keep the Chrome History database and analytics database protected.

---

## Known Technical Considerations

### Java SQLite native-access warning

With newer Java versions, the SQLite JDBC driver may report a native-access warning similar to:

```text
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by org.sqlite.SQLiteJDBCLoader
```

SQLite can still operate normally. The warning can be addressed in the JVM configuration with the appropriate native-access option when required.

### Time zones

Browser timestamps originate from Chrome's timestamp representation and analytics periods are resolved as local calendar ranges. A production deployment should standardize storage around `Instant` and apply an explicit `ZoneId` for user-facing period calculations.

---

## Future Roadmap

The architecture can be extended naturally into additional analytics capabilities:

- Browser productivity scoring
- Work / personal browsing segmentation
- Session detection and session duration analysis
- Focus-time and distraction analysis
- Category time allocation
- Daily / weekly behavioral trends
- Domain-level time and frequency rankings
- Search-vs-direct navigation analysis
- Transition-type analytics
- Hour-of-day heatmaps
- Day-of-week heatmaps
- Category comparison across periods
- Export to CSV / Excel / PDF
- Scheduled reports
- Multi-user accounts
- Authentication and authorization
- Configurable tagging rules and tag confidence models
- Background synchronization jobs
- Historical data coverage indicators
- Data retention and privacy controls

---

## Repository Structure

The project is maintained as a full-stack application with backend and frontend work separated by responsibility.

A practical branch arrangement is:

```text
main
  └── Spring Boot backend

Frontend-React.js
  └── React frontend
```

The frontend branch contains the React/Vite dashboard application, while the backend is maintained in the Spring Boot application structure.

---

## Project Status

Current application capabilities include:

- Chrome History synchronization
- MySQL persistence
- Automatic tagging
- Manual tag operations
- Period-based analytics
- Saved analytics summaries
- Duplicate-period protection
- Dashboard visualization
- Analytics History review
- Data Explorer
- Bootstrap off-canvas navigation
- Toast notifications
- Progress reporting
- Paginated table views
- Row-specific analytics charts

The next major engineering milestone is to move from full synchronization toward **incremental synchronization and database-side aggregation**, which will make the application substantially faster as browsing history grows.

---

## Author

**Banu-Yella**

Chrome Browser History Analytics Dashboard

---

## License

Add the project's intended license here before public distribution.
