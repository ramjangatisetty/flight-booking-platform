import { Box } from '@mui/material';
import { testIds } from '../../testing/testIds';

export default function BookingStepperPlaceholder() {
  return (
    <Box
      data-testid={testIds.bookingStepperPlaceholder.root}
      sx={{
        height: 40,
        backgroundColor: 'background.default',
      }}
    />
  );
}
