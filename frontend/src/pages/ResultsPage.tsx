import { Box, Typography, Card, CardContent, Button, Grid, Chip } from '@mui/material';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { queryParamsToSearchCriteria } from '../utils/queryParams';
import { CabinClass } from '../domain/enums';
import { testIds } from '../testing/testIds';
import airportsData from '../data/airports.json';

interface MockFlight {
  flightNumber: string;
  airline: string;
  departTime: string;
  arriveTime: string;
  duration: string;
  price: number;
  currency: string;
  cabinClass: string;
  stops: number;
}

function generateMockFlights(searchParams: URLSearchParams): MockFlight[] {
  const criteria = queryParamsToSearchCriteria(searchParams, airportsData);
  
  const basePrice = {
    [CabinClass.ECONOMY]: 250,
    [CabinClass.PREMIUM_ECONOMY]: 450,
    [CabinClass.BUSINESS]: 1200,
    [CabinClass.FIRST]: 2500,
  }[criteria.cabinClass || CabinClass.ECONOMY];

  const airlines = ['AA', 'DL', 'UA', 'BA', 'LH'];
  const flights: MockFlight[] = [];

  for (let i = 0; i < 4; i++) {
    const airline = airlines[i % airlines.length];
    const flightNum = `${airline}${Math.floor(Math.random() * 900) + 100}`;
    const departHour = 8 + i * 3;
    const arriveHour = departHour + 5 + Math.floor(Math.random() * 3);
    const stops = i === 0 ? 0 : Math.floor(Math.random() * 2);
    const priceVariation = basePrice * (0.8 + Math.random() * 0.4);

    flights.push({
      flightNumber: flightNum,
      airline: airline,
      departTime: `${departHour.toString().padStart(2, '0')}:${(Math.random() * 60).toFixed(0).padStart(2, '0')}`,
      arriveTime: `${arriveHour.toString().padStart(2, '0')}:${(Math.random() * 60).toFixed(0).padStart(2, '0')}`,
      duration: `${5 + Math.floor(Math.random() * 3)}h ${Math.floor(Math.random() * 60)}m`,
      price: Math.round(priceVariation),
      currency: criteria.currency || 'USD',
      cabinClass: criteria.cabinClass || CabinClass.ECONOMY,
      stops: stops,
    });
  }

  return flights;
}

export default function ResultsPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const flights = generateMockFlights(searchParams);

  const from = searchParams.get('from') || '';
  const to = searchParams.get('to') || '';
  const departDate = searchParams.get('departDate') || '';

  return (
    <Box data-testid={testIds.results.root}>
      <Typography variant="h4" gutterBottom>
        Flight Results
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        {from} → {to} on {departDate}
      </Typography>

      <Grid container spacing={3}>
        {flights.map((flight, index) => (
          <Grid size={{ xs: 12 }} key={index}>
            <Card data-testid={testIds.results.flightCard}>
              <CardContent>
                <Grid container spacing={2} alignItems="center">
                  <Grid size={{ xs: 12, md: 2 }}>
                    <Typography variant="h6">{flight.airline}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {flight.flightNumber}
                    </Typography>
                  </Grid>

                  <Grid size={{ xs: 12, md: 3 }}>
                    <Typography variant="h5">{flight.departTime}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {from}
                    </Typography>
                  </Grid>

                  <Grid size={{ xs: 12, md: 2 }}>
                    <Typography variant="body2" align="center">
                      {flight.duration}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" align="center">
                      {flight.stops === 0 ? 'Nonstop' : `${flight.stops} stop${flight.stops > 1 ? 's' : ''}`}
                    </Typography>
                  </Grid>

                  <Grid size={{ xs: 12, md: 3 }}>
                    <Typography variant="h5">{flight.arriveTime}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {to}
                    </Typography>
                  </Grid>

                  <Grid size={{ xs: 12, md: 2 }}>
                    <Box sx={{ textAlign: 'right' }}>
                      <Typography variant="h5" color="primary">
                        {flight.currency} {flight.price}
                      </Typography>
                      <Chip
                        label={flight.cabinClass.replace('-', ' ')}
                        size="small"
                        sx={{ mt: 1, mb: 1 }}
                      />
                      <Button
                        fullWidth
                        variant="contained"
                        onClick={() => navigate('/bookings')}
                        data-testid={testIds.results.selectFlightButton}
                      >
                        Select
                      </Button>
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
