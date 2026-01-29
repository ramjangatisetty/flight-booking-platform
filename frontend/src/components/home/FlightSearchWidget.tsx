import { useState } from 'react';
import {
  Card,
  Grid,
  RadioGroup,
  FormControlLabel,
  Radio,
  Autocomplete,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { useNavigate } from 'react-router-dom';
import dayjs, { Dayjs } from 'dayjs';
import { TripType, CabinClass, Currency } from '../../domain/enums';
import type { Airport } from '../../domain/types';
import { searchCriteriaToQueryParams } from '../../utils/queryParams';
import { testIds } from '../../testing/testIds';
import airportsData from '../../data/airports.json';

const airports: Airport[] = airportsData;

interface FormState {
  tripType: TripType;
  origin: Airport | null;
  destination: Airport | null;
  departDate: Date | null;
  returnDate: Date | null;
  paxAdults: number;
  paxChildren: number;
  cabinClass: CabinClass;
  currency: Currency;
}

interface FormErrors {
  origin?: string;
  destination?: string;
  departDate?: string;
  returnDate?: string;
  paxAdults?: string;
  paxChildren?: string;
}

export default function FlightSearchWidget() {
  const navigate = useNavigate();
  
  const [state, setState] = useState<FormState>({
    tripType: TripType.ROUND_TRIP,
    origin: null,
    destination: null,
    departDate: null,
    returnDate: null,
    paxAdults: 1,
    paxChildren: 0,
    cabinClass: CabinClass.ECONOMY,
    currency: Currency.USD,
  });

  const [errors, setErrors] = useState<FormErrors>({});

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!state.origin) {
      newErrors.origin = 'Origin airport is required';
    }

    if (!state.destination) {
      newErrors.destination = 'Destination airport is required';
    }

    if (state.origin && state.destination && state.origin.iata === state.destination.iata) {
      newErrors.destination = 'Destination must be different from origin';
    }

    if (!state.departDate) {
      newErrors.departDate = 'Departure date is required';
    } else {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      if (state.departDate < today) {
        newErrors.departDate = 'Departure date cannot be in the past';
      }
    }

    if (state.tripType === TripType.ROUND_TRIP) {
      if (!state.returnDate) {
        newErrors.returnDate = 'Return date is required for round-trip';
      } else if (state.departDate && state.returnDate < state.departDate) {
        newErrors.returnDate = 'Return date must be after departure date';
      }
    }

    if (state.paxAdults < 1) {
      newErrors.paxAdults = 'At least 1 adult passenger is required';
    }

    if (state.paxAdults > 9) {
      newErrors.paxAdults = 'Maximum 9 adult passengers';
    }

    if (state.paxChildren < 0) {
      newErrors.paxChildren = 'Children count cannot be negative';
    }

    if (state.paxChildren > 9) {
      newErrors.paxChildren = 'Maximum 9 children';
    }

    if (state.paxAdults + state.paxChildren > 9) {
      newErrors.paxAdults = 'Total passengers cannot exceed 9';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (!validateForm()) {
      return;
    }

    const queryParams = searchCriteriaToQueryParams({
      tripType: state.tripType,
      origin: state.origin!,
      destination: state.destination!,
      departDate: state.departDate!,
      returnDate: state.returnDate || undefined,
      paxAdults: state.paxAdults,
      paxChildren: state.paxChildren,
      cabinClass: state.cabinClass,
      currency: state.currency,
    });

    const searchParams = new URLSearchParams();
    Object.entries(queryParams).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value);
      }
    });

    navigate(`/results?${searchParams.toString()}`);
  };

  const clearError = (field: keyof FormErrors) => {
    if (errors[field]) {
      setErrors({ ...errors, [field]: undefined });
    }
  };

  return (
    <Card
      data-testid={testIds.flightSearch.root}
      elevation={8}
      sx={{
        maxWidth: 900,
        width: '100%',
        p: 4,
        borderRadius: 4,
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)',
      }}
    >
      <Grid container spacing={3}>
        {/* Trip Type */}
        <Grid size={{ xs: 12 }}>
          <RadioGroup
            row
            value={state.tripType}
            onChange={(e) => setState({ ...state, tripType: e.target.value as TripType })}
          >
            <FormControlLabel
              value={TripType.ONE_WAY}
              control={<Radio data-testid={testIds.flightSearch.tripTypeOneWay} />}
              label="One-way"
            />
            <FormControlLabel
              value={TripType.ROUND_TRIP}
              control={<Radio data-testid={testIds.flightSearch.tripTypeRoundTrip} />}
              label="Round-trip"
            />
            <FormControlLabel
              value={TripType.MULTI_CITY}
              control={<Radio data-testid={testIds.flightSearch.tripTypeMultiCity} />}
              label="Multi-city"
            />
          </RadioGroup>
        </Grid>

        {/* Origin */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Autocomplete
            options={airports}
            getOptionLabel={(option) => `${option.iata} - ${option.city}`}
            value={state.origin}
            onChange={(_, newValue) => {
              setState({ ...state, origin: newValue });
              clearError('origin');
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                label="From"
                error={!!errors.origin}
                helperText={errors.origin}
                inputProps={{
                  ...params.inputProps,
                  'data-testid': testIds.flightSearch.originInput,
                }}
              />
            )}
          />
        </Grid>

        {/* Destination */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Autocomplete
            options={airports}
            getOptionLabel={(option) => `${option.iata} - ${option.city}`}
            value={state.destination}
            onChange={(_, newValue) => {
              setState({ ...state, destination: newValue });
              clearError('destination');
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                label="To"
                error={!!errors.destination}
                helperText={errors.destination}
                inputProps={{
                  ...params.inputProps,
                  'data-testid': testIds.flightSearch.destinationInput,
                }}
              />
            )}
          />
        </Grid>

        {/* Departure Date */}
        <Grid size={{ xs: 12, md: 6 }}>
          <DatePicker
            label="Departure"
            value={state.departDate ? dayjs(state.departDate) : null}
            onChange={(newValue: Dayjs | null) => {
              setState({ ...state, departDate: newValue?.toDate() ?? null });
              clearError('departDate');
            }}
            slotProps={{
              textField: {
                fullWidth: true,
                error: !!errors.departDate,
                helperText: errors.departDate,
                inputProps: {
                  'data-testid': testIds.flightSearch.departDateInput,
                },
              },
            }}
          />
        </Grid>

        {/* Return Date */}
        {state.tripType === TripType.ROUND_TRIP && (
          <Grid size={{ xs: 12, md: 6 }}>
            <DatePicker
              label="Return"
              value={state.returnDate ? dayjs(state.returnDate) : null}
              onChange={(newValue: Dayjs | null) => {
                setState({ ...state, returnDate: newValue?.toDate() ?? null });
                clearError('returnDate');
              }}
              slotProps={{
                textField: {
                  fullWidth: true,
                  error: !!errors.returnDate,
                  helperText: errors.returnDate,
                  inputProps: {
                    'data-testid': testIds.flightSearch.returnDateInput,
                  },
                },
              }}
            />
          </Grid>
        )}

        {/* Adults */}
        <Grid size={{ xs: 6, md: 3 }}>
          <TextField
            fullWidth
            type="number"
            label="Adults"
            value={state.paxAdults}
            onChange={(e) => {
              setState({ ...state, paxAdults: parseInt(e.target.value) || 0 });
              clearError('paxAdults');
            }}
            error={!!errors.paxAdults}
            helperText={errors.paxAdults}
            inputProps={{
              min: 1,
              max: 9,
              'data-testid': testIds.flightSearch.paxAdultsInput,
            }}
          />
        </Grid>

        {/* Children */}
        <Grid size={{ xs: 6, md: 3 }}>
          <TextField
            fullWidth
            type="number"
            label="Children"
            value={state.paxChildren}
            onChange={(e) => {
              setState({ ...state, paxChildren: parseInt(e.target.value) || 0 });
              clearError('paxChildren');
            }}
            error={!!errors.paxChildren}
            helperText={errors.paxChildren}
            inputProps={{
              min: 0,
              max: 9,
              'data-testid': testIds.flightSearch.paxChildrenInput,
            }}
          />
        </Grid>

        {/* Cabin Class */}
        <Grid size={{ xs: 6, md: 3 }}>
          <FormControl fullWidth>
            <InputLabel>Cabin Class</InputLabel>
            <Select
              value={state.cabinClass}
              label="Cabin Class"
              onChange={(e) => setState({ ...state, cabinClass: e.target.value as CabinClass })}
              inputProps={{
                'data-testid': testIds.flightSearch.cabinClassSelect,
              }}
            >
              <MenuItem value={CabinClass.ECONOMY}>Economy</MenuItem>
              <MenuItem value={CabinClass.PREMIUM_ECONOMY}>Premium Economy</MenuItem>
              <MenuItem value={CabinClass.BUSINESS}>Business</MenuItem>
              <MenuItem value={CabinClass.FIRST}>First Class</MenuItem>
            </Select>
          </FormControl>
        </Grid>

        {/* Currency */}
        <Grid size={{ xs: 6, md: 3 }}>
          <FormControl fullWidth>
            <InputLabel>Currency</InputLabel>
            <Select
              value={state.currency}
              label="Currency"
              onChange={(e) => setState({ ...state, currency: e.target.value as Currency })}
              inputProps={{
                'data-testid': testIds.flightSearch.currencySelect,
              }}
            >
              <MenuItem value={Currency.USD}>USD</MenuItem>
              <MenuItem value={Currency.EUR}>EUR</MenuItem>
              <MenuItem value={Currency.GBP}>GBP</MenuItem>
              <MenuItem value={Currency.JPY}>JPY</MenuItem>
            </Select>
          </FormControl>
        </Grid>

        {/* Search Button */}
        <Grid size={{ xs: 12 }}>
          <Button
            fullWidth
            variant="contained"
            size="large"
            onClick={handleSubmit}
            data-testid={testIds.flightSearch.searchButton}
          >
            Search Flights
          </Button>
        </Grid>
      </Grid>
    </Card>
  );
}
