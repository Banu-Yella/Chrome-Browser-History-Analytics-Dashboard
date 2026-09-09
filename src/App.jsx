import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import NavBar from './components/NavBar';
import HomePage from './pages/HomePage';
import Dashboard from './pages/Dashboard';
import TagManagerPage from './pages/TagManagerPage';
import BrowserHistoryPage from './pages/BrowserHistoryPage';
import AnalyticsHistoryPage from './pages/AnalyticsHistoryPage';
import DataExplorerPage from './pages/DataExplorerPage';

export default function App() {
  return (
    <BrowserRouter>
      <NavBar />
      <main className="app-main">
        <Routes>
          <Route path="/home" element={<HomePage />} />
          <Route path="/" element={<Dashboard />} />
          <Route path="/tags" element={<TagManagerPage />} />
          <Route path="/history" element={<BrowserHistoryPage />} />
          <Route path="/analytics-history" element={<AnalyticsHistoryPage />} />
          <Route path="/data" element={<DataExplorerPage />} />
          <Route path="*" element={<HomePage />} />
        </Routes>
      </main>
      <ToastContainer position="top-right" autoClose={3500} hideProgressBar={false} newestOnTop closeOnClick pauseOnFocusLoss draggable pauseOnHover />
    </BrowserRouter>
  );
}
