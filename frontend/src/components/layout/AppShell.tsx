import { Box } from '@mui/material';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import TripSummaryPlaceholder from './TripSummaryPlaceholder';
import { testIds } from '../../testing/testIds';

export default function AppShell() {
  return (
    <Box data-testid={testIds.appShell.root}>
      <Navbar />
      <Box
        sx={{
          marginTop: '64px',
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', md: '1fr 320px' },
          gap: 3,
          p: { xs: 2, md: 3 },
        }}
      >
        <Box component="main">
          <Outlet />
        </Box>
        <Box sx={{ display: { xs: 'none', md: 'block' } }}>
          <TripSummaryPlaceholder />
        </Box>
      </Box>
    </Box>
  );
}
