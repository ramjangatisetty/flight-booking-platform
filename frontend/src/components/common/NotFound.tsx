import { Box, Typography, Button } from '@mui/material';
import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        textAlign: 'center',
      }}
    >
      <Typography variant="h2" gutterBottom>
        404
      </Typography>
      <Typography variant="h5" gutterBottom color="text.secondary">
        Page Not Found
      </Typography>
      <Typography variant="body1" paragraph color="text.secondary">
        The page you're looking for doesn't exist.
      </Typography>
      <Button
        variant="contained"
        component={Link}
        to="/"
        size="large"
      >
        Go Home
      </Button>
    </Box>
  );
}
