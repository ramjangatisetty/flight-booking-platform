import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppShell from './components/layout/AppShell';
import HomePage from './pages/HomePage';
import ResultsPage from './pages/ResultsPage';
import HealthPage from './pages/HealthPage';
import PlaceholderPage from './components/common/PlaceholderPage';
import NotFound from './components/common/NotFound';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppShell />}>
          <Route index element={<HomePage />} />
          <Route path="results" element={<ResultsPage />} />
          <Route path="health" element={<HealthPage />} />
          <Route
            path="bookings"
            element={
              <PlaceholderPage
                title="My Bookings"
                message="Booking management coming soon"
              />
            }
          />
          <Route
            path="help"
            element={
              <PlaceholderPage
                title="Help"
                message="Help center coming soon"
              />
            }
          />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
