import { Box, Card, CardContent, Typography, Button } from '@mui/material';
import { Link } from 'react-router-dom';
import { testIds } from '../../testing/testIds';

interface PlaceholderPageProps {
  title: string;
  message: string;
}

export default function PlaceholderPage({ title, message }: PlaceholderPageProps) {
  return (
    <Box
      data-testid={testIds.placeholder.root}
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
      }}
    >
      <Card sx={{ maxWidth: 500, textAlign: 'center' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" gutterBottom>
            {title}
          </Typography>
          <Typography variant="body1" paragraph color="text.secondary">
            {message}
          </Typography>
          <Button
            variant="contained"
            component={Link}
            to="/"
            data-testid={testIds.placeholder.backHomeLink}
          >
            Back to Home
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
}
