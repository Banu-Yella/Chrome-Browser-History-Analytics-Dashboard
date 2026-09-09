import { Link } from 'react-router-dom';

const sections = [
  { title: 'Dashboard', text: 'Generate period analytics, review charts, and monitor validation progress.', to: '/' },
  { title: 'Browser History', text: 'Search and inspect synchronized Chrome history records and their tags.', to: '/history' },
  { title: 'Tags', text: 'Create, update, validate, and maintain the keyword taxonomy used for auto-tagging.', to: '/tags' },
  { title: 'Analytics History', text: 'Review previously generated periods and their stored aggregate metrics.', to: '/analytics-history' },
];

export default function HomePage() {
  return <div className="app-page">
    <div className="home-hero p-4 p-lg-5 mb-4"><div className="row align-items-center g-4">
      <div className="col-lg-8"><div className="eyebrow">BROWSER HISTORY ANALYTICS</div><h1 className="display-6 fw-bold mb-3">Turn browsing history into a usable analytics record.</h1><p className="lead text-muted mb-0">Synchronize Chrome history, apply tags, preserve generated periods, and inspect browsing behaviour through tables and charts.</p></div>
      <div className="col-lg-4"><div className="home-quick-panel"><div className="small text-uppercase text-muted fw-semibold">Quick access</div><div className="d-grid gap-2 mt-2"><Link className="btn btn-primary" to="/">Open Dashboard</Link><Link className="btn btn-outline-secondary" to="/history">Review Browser History</Link></div></div></div>
    </div></div>
    <div className="row g-3">{sections.map((section) => <div key={section.to} className="col-md-6 col-xl-3"><Link to={section.to} className="card h-100 app-link-card text-decoration-none"><div className="card-body"><h5 className="card-title">{section.title}</h5><p className="card-text text-muted">{section.text}</p><span className="small fw-semibold">Open →</span></div></Link></div>)}</div>
  </div>;
}
