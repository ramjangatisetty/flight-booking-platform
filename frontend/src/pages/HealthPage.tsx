import { Box, Card, CardContent, Typography, Chip } from '@mui/material';
import { env } from '../config/env';
import { testIds } from '../testing/testIds';

export default function HealthPage() {
  return (
    <Box data-testid={testIds.health.root}>
      <Card>
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
            <Typography variant="h4">
              Application Health
            </Typography>
            <Chip
              label="Healthy"
              color="success"
              data-testid={testIds.health.statusIndicator}
            />
          </Box>

          <Box sx={{ mb: 3 }} data-testid={testIds.health.versionInfo}>
            <Typography variant="h6" gutterBottom>
              Version Information
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Application: SkyBooker Airlines Frontend
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Version: 1.0.0-dev
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Build: {new Date().toISOString()}
            </Typography>
          </Box>

          <Box>
            <Typography variant="h6" gutterBottom>
              Backend Services
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Booking Service: {env.bookingServiceUrl}
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Inventory Service: {env.inventoryServiceUrl}
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Payment Service: {env.paymentServiceUrl}
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Loyalty Service: {env.loyaltyServiceUrl}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Baggage Service: {env.baggageServiceUrl}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
