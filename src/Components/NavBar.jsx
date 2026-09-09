import { Offcanvas } from 'bootstrap';
import { useCallback, useEffect } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';

const WORKSPACE_ITEMS = [
  { to: '/', label: 'Dashboard', icon: '▦' },
  { to: '/history', label: 'Browser History', icon: '◷' },
  { to: '/tags', label: 'Tags', icon: '◆' },
  { to: '/analytics-history', label: 'Analytics History', icon: '⌁' },
  { to: '/data', label: 'Data Explorer', icon: '▤' },
];

export default function NavBar() {
  const location = useLocation();
  const navigate = useNavigate();

  const cleanupBootstrapBackdrop = useCallback(() => {
    document.body.classList.remove('offcanvas-backdrop-open', 'modal-open');
    document.body.style.removeProperty('overflow');
    document.body.style.removeProperty('padding-right');
    document.querySelectorAll('.offcanvas-backdrop').forEach((backdrop) => backdrop.remove());
  }, []);

  const navigateFromMenu = (to) => {
    const element = document.getElementById('mainOffcanvas');
    if (!element) {
      navigate(to);
      return;
    }

    const instance = Offcanvas.getOrCreateInstance(element);
    let completed = false;
    const go = () => {
      if (completed) return;
      completed = true;
      cleanupBootstrapBackdrop();
      navigate(to);
      window.setTimeout(cleanupBootstrapBackdrop, 50);
    };

    if (element.classList.contains('show')) {
      element.addEventListener('hidden.bs.offcanvas', go, { once: true });
      instance.hide();
      window.setTimeout(() => {
        if (!element.classList.contains('show')) go();
      }, 450);
    } else {
      go();
    }
  };

  useEffect(() => {
    const element = document.getElementById('mainOffcanvas');
    if (!element) return undefined;
    const handleHidden = () => cleanupBootstrapBackdrop();
    element.addEventListener('hidden.bs.offcanvas', handleHidden);
    return () => element.removeEventListener('hidden.bs.offcanvas', handleHidden);
  }, [cleanupBootstrapBackdrop]);

  return (
    <>
      <nav className="navbar app-navbar shadow-sm">
        <div className="container-fluid px-3 px-lg-4">
          <button
            className="btn burger-button me-3"
            type="button"
            data-bs-toggle="offcanvas"
            data-bs-target="#mainOffcanvas"
            aria-controls="mainOffcanvas"
            aria-label="Open navigation"
          >
            <span aria-hidden="true">☰</span>
          </button>

          <NavLink to="/home" className="navbar-brand-wrap text-decoration-none">
            <span className="navbar-brand mb-0">Browsing Insights</span>
            <span className="brand-subtitle d-none d-md-inline">Chrome History Analytics</span>
          </NavLink>

          <div className="ms-auto d-flex align-items-center gap-2">
            <NavLink to="/home" className="btn btn-sm nav-quick-link">
              Home
            </NavLink>
            <NavLink to="/" end className="btn btn-sm nav-quick-link nav-quick-primary">
              Dashboard
            </NavLink>
          </div>
        </div>
      </nav>

      <div
        className="offcanvas offcanvas-start app-offcanvas"
        tabIndex="-1"
        id="mainOffcanvas"
        aria-labelledby="mainOffcanvasLabel"
      >
        <div className="offcanvas-header border-bottom">
          <div>
            <h5 className="offcanvas-title mb-1" id="mainOffcanvasLabel">
              Browsing Insights
            </h5>
            <div className="small text-muted">Workspace navigation</div>
          </div>
          <button
            type="button"
            className="btn-close"
            data-bs-dismiss="offcanvas"
            aria-label="Close"
          />
        </div>

        <div className="offcanvas-body p-3">
          <div className="d-grid gap-2 mb-4">
            <button
              type="button"
              className={`btn text-start ${location.pathname === '/home' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => navigateFromMenu('/home')}
            >
              ⌂ &nbsp; Home
            </button>
            <button
              type="button"
              className={`btn text-start ${location.pathname === '/' ? 'btn-primary' : 'btn-outline-secondary'}`}
              onClick={() => navigateFromMenu('/')}
            >
              ▦ &nbsp; Dashboard
            </button>
          </div>

          <div className="small text-uppercase text-muted fw-semibold mb-2">
            Pages &amp; tables
          </div>

          <div className="nav-section">
            {WORKSPACE_ITEMS.map((item) => {
              const active = item.to === '/' ? location.pathname === '/' : location.pathname.startsWith(item.to);

              return (
                <button
                  key={item.to}
                  type="button"
                  className={`nav-item-link border ${active ? 'active' : ''}`}
                  onClick={() => navigateFromMenu(item.to)}
                >
                  <span className="nav-item-icon" aria-hidden="true">{item.icon}</span>
                  <span>{item.label}</span>
                </button>
              );
            })}
          </div>

          <div className="offcanvas-note mt-4">
            <div className="fw-semibold mb-1">Current page</div>
            <div className="small text-muted text-break">{location.pathname}</div>
            <div className="small text-muted mt-2">
              Data Explorer exposes the backend tables that have read APIs.
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
