import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { Link } from 'react-router-dom';
import { testIds } from '../../testing/testIds';

export default function Navbar() {
  return (
    <AppBar position="fixed" data-testid={testIds.navbar.root}>
      <Toolbar>
        <Typography
          variant="h6"
          component={Link}
          to="/"
          sx={{
            flexGrow: 1,
            textDecoration: 'none',
            color: 'inherit',
            fontWeight: 700,
          }}
          data-testid={testIds.navbar.logo}
        >
          SkyBooker Airlines
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            color="inherit"
            component={Link}
            to="/"
            data-testid={testIds.navbar.homeLink}
          >
            Home
          </Button>
          <Button
            color="inherit"
            component={Link}
            to="/bookings"
            data-testid={testIds.navbar.bookingsLink}
          >
            My Bookings
          </Button>
          <Button
            color="inherit"
            component={Link}
            to="/help"
            data-testid={testIds.navbar.helpLink}
          >
            Help
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
