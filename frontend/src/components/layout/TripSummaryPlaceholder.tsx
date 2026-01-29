import { Paper, Typography } from '@mui/material';
import { testIds } from '../../testing/testIds';

export default function TripSummaryPlaceholder() {
  return (
    <Paper
      data-testid={testIds.tripSummaryPlaceholder.root}
      sx={{
        width: 320,
        p: 3,
        position: 'sticky',
        top: 80,
      }}
    >
      <Typography variant="h6" gutterBottom>
        Trip Summary
      </Typography>
      <Typography variant="body2" color="text.secondary">
        Coming Soon
      </Typography>
    </Paper>
  );
}
