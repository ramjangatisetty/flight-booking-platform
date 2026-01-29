# Design Document: Airline Booking Frontend

## Overview

The airline booking frontend is a React 18 + TypeScript single-page application built with Vite and Material-UI v5. The application provides an enterprise-grade user interface for searching flights and managing bookings through integration with existing backend microservices. The design emphasizes automation testing capabilities through stable element identifiers, intentional DOM complexity, and consistent component patterns.

This document covers Phase 0 (project scaffold and infrastructure) and Phase 1 (home page with flight search functionality). Future phases will incrementally add search results, passenger details, add-ons, payment, and confirmation pages.

### Technology Stack

- **Build Tool**: Vite 5.x for fast development and optimized production builds
- **Framework**: React 18 with TypeScript for type-safe component development
- **UI Library**: Material-UI (MUI) v5 for enterprise-grade components and theming
- **Date Pickers**: MUI X Date Pickers (@mui/x-date-pickers) with Day.js adapter
- **Routing**: React Router v6 for client-side navigation
- **Data Fetching**: TanStack React Query v5 for server state management
- **HTTP Client**: Axios for API requests
- **Styling**: MUI's CSS-in-JS solution (Emotion) for component styling

### Design Principles

1. **Automation-First**: All interactive elements have stable data-testid attributes managed through centralized constants
2. **Type Safety**: Comprehensive TypeScript types for all data structures, props, and API responses
3. **Component Composition**: Reusable, single-responsibility components following React best practices
4. **Accessibility**: WCAG 2.1 AA compliance with semantic HTML, ARIA labels, and keyboard navigation
5. **Responsive Design**: Desktop-first approach with mobile adaptations using MUI breakpoints
6. **Enterprise Branding**: Professional airline aesthetic with premium visual design

## Architecture

### Project Structure

```
airline-booking-frontend/
├── public/                          # Static assets
├── src/
│   ├── assets/
│   │   └── heroImages.ts           # Hero image URL constants
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppShell.tsx        # Main layout wrapper
│   │   │   ├── Navbar.tsx          # Top navigation bar
│   │   │   ├── BookingStepperPlaceholder.tsx
│   │   │   └── TripSummaryPlaceholder.tsx
│   │   ├── home/
│   │   │   ├── HeroSection.tsx     # Hero banner with background
│   │   │   └── FlightSearchWidget.tsx  # Search form card
│   │   └── common/
│   │       └── NotFound.tsx        # 404 error page
│   ├── pages/
│   │   ├── HomePage.tsx            # Home page container
│   │   ├── ResultsPage.tsx         # Search results (mock in Phase 1)
│   │   └── HealthPage.tsx          # Health check UI
│   ├── domain/
│   │   ├── enums.ts                # TripType, CabinClass, Currency
│   │   └── types.ts                # Domain type definitions
│   ├── config/
│   │   └── env.ts                  # Environment configuration
│   ├── theme/
│   │   └── theme.ts                # MUI theme configuration
│   ├── testing/
│   │   └── testIds.ts              # Centralized test ID constants
│   ├── data/
│   │   └── airports.json           # Airport autocomplete data
│   ├── hooks/
│   │   └── useFlightSearch.ts      # Search form state management
│   ├── utils/
│   │   └── queryParams.ts          # URL query parameter helpers
│   ├── App.tsx                     # Root component with router
│   ├── main.tsx                    # Application entry point
│   └── vite-env.d.ts               # Vite type declarations
├── .env.local                       # Environment variables
├── .eslintrc.cjs                    # ESLint configuration
├── tsconfig.json                    # TypeScript configuration
├── vite.config.ts                   # Vite configuration
└── package.json                     # Dependencies and scripts
```

### Component Hierarchy

```
App (Router Provider, Theme Provider, Query Client Provider)
└── AppShell
    ├── Navbar
    ├── BookingStepperPlaceholder (conditional)
    ├── Main Content Area (Route Outlet)
    │   ├── HomePage
    │   │   └── HeroSection
    │   │       └── FlightSearchWidget
    │   ├── ResultsPage (mock data in Phase 1)
    │   ├── HealthPage
    │   └── NotFound
    └── TripSummaryPlaceholder (conditional, sticky right sidebar)
```

## Components and Interfaces

### Core Layout Components

#### AppShell Component

The AppShell provides the persistent layout structure for all pages.

**Props**: None (uses React Router's Outlet for child routes)

**Responsibilities**:
- Render Navbar at the top
- Conditionally render BookingStepperPlaceholder on booking flow pages
- Render main content area with React Router Outlet
- Conditionally render TripSummaryPlaceholder as sticky right sidebar
- Apply consistent spacing and layout grid

**Layout Structure**:
```typescript
interface AppShellLayout {
  navbar: {
    position: 'fixed';
    height: '64px';
    zIndex: 1100;
  };
  mainContent: {
    marginTop: '64px';
    display: 'grid';
    gridTemplateColumns: '1fr' | '1fr 320px'; // With/without trip summary
  };
  tripSummary: {
    position: 'sticky';
    top: '80px';
    height: 'fit-content';
    display: 'none' | 'block'; // Hidden on mobile (< md breakpoint), visible on desktop
  };
}
```

#### Navbar Component

**Props**:
```typescript
interface NavbarProps {
  // No props in Phase 0/1, future: user authentication state
}
```

**Elements**:
- Brand logo/name (left-aligned)
- Navigation links: Home (active), My Bookings (placeholder), Help (placeholder)
- All links have data-testid from testIds.ts

**Navigation Link Handling**:
- Home link: Navigates to / (fully functional)
- My Bookings link: Navigates to /bookings (placeholder page with "Coming Soon" message)
- Help link: Navigates to /help (placeholder page with "Coming Soon" message)

**Styling**:
- Background: Primary color (#0B2A4A)
- Text: White
- Height: 64px
- Box shadow for elevation

#### BookingStepperPlaceholder Component

**Purpose**: Reserve space for future multi-step booking progress indicator

**Props**: None in Phase 0/1

**Rendering**: Empty Box component with defined height (40px) and background color

#### TripSummaryPlaceholder Component

**Purpose**: Reserve space for future booking summary sidebar

**Props**: None in Phase 0/1

**Rendering**: 
- Empty Paper component with defined width (320px), sticky positioning, and placeholder text
- Rendered on all routes when viewport >= md breakpoint
- Hidden on mobile/tablet (< md breakpoint)

**Responsive Behavior**:
```typescript
// In AppShell component
<Box sx={{ display: { xs: 'none', md: 'block' } }}>
  <TripSummaryPlaceholder />
</Box>
```

### Home Page Components

#### HomePage Component

**Responsibilities**:
- Container for home page content
- Render HeroSection component
- No direct state management (delegates to child components)

**Props**: None

#### HeroSection Component

**Props**:
```typescript
interface HeroSectionProps {
  backgroundImage: string;  // URL from heroImages.ts
  title: string;
  subtitle: string;
}
```

**Styling**:
- Full-width container
- Minimum height: 600px (desktop), responsive on mobile
- Background image with dark gradient overlay: `linear-gradient(rgba(11, 42, 74, 0.7), rgba(11, 42, 74, 0.5))`
- Optional: Subtle noise texture overlay for depth
- Centered content with flexbox
- Typography: Title (h2, bold, white), Subtitle (h5, white with 90% opacity)

**Children**: FlightSearchWidget positioned in center

#### FlightSearchWidget Component

**Props**: None (manages internal state, uses navigation hook)

**State**:
```typescript
interface FlightSearchState {
  tripType: TripType;
  origin: Airport | null;
  destination: Airport | null;
  departDate: Date | null;  // Store as Date for consistency
  returnDate: Date | null;  // Store as Date for consistency
  paxAdults: number;
  paxChildren: number;
  cabinClass: CabinClass;
  currency: Currency;
  errors: Record<string, string>;
}
```

**Date Picker Integration**:
```typescript
// DatePicker value conversion
<DatePicker
  value={state.departDate ? dayjs(state.departDate) : null}
  onChange={(newValue) => {
    setState({ ...state, departDate: newValue?.toDate() ?? null });
  }}
/>
```

**Form Fields**:
1. Trip Type Radio Group (One-way, Round-trip, Multi-city)
2. Origin Autocomplete (MUI Autocomplete with airports.json data)
3. Destination Autocomplete (MUI Autocomplete with airports.json data)
4. Departure Date Picker (MUI X DatePicker with Day.js adapter)
5. Return Date Picker (MUI X DatePicker with Day.js adapter, conditional on Round-trip)
6. Adult Passenger Count (MUI Select or TextField with number input)
7. Children Passenger Count (MUI Select or TextField with number input)
8. Cabin Class Select (MUI Select with enum values)
9. Currency Select (MUI Select with enum values)
10. Search Button (MUI Button, primary variant)

**Validation Rules**:
- Origin: Required, must be valid airport from airports.json
- Destination: Required, must be valid airport from airports.json, must differ from origin
- Depart Date: Required, must be today or future date
- Return Date: Required if Round-trip, must be >= depart date
- Adult Passengers: Required, minimum 1, maximum 9
- Children Passengers: Optional, minimum 0, maximum 9
- Total passengers (adults + children): Maximum 9

**Styling**:
- MUI Card component with elevation={8}
- Border radius: 16px
- Backdrop filter: blur(10px) with semi-transparent white background
- Strong box shadow for premium feel
- Padding: 32px
- Max width: 900px
- Form fields arranged in responsive grid (2-3 columns on desktop, 1 column on mobile)

**Behavior**:
- On search button click: Validate all fields
- If valid: Navigate to `/results` with query parameters
- If invalid: Display error messages below relevant fields
- Clear errors when user modifies invalid field

**Query Parameter Mapping**:
```typescript
const queryParams = {
  tripType: state.tripType,
  from: state.origin?.iata,
  to: state.destination?.iata,
  departDate: formatDate(state.departDate),
  returnDate: state.returnDate ? formatDate(state.returnDate) : undefined,
  paxAdults: state.paxAdults,
  paxChildren: state.paxChildren,
  cabinClass: state.cabinClass,
  currency: state.currency,
};
```

### Results Page (Phase 1 - Mock Implementation)

#### ResultsPage Component

**Purpose**: Display mock flight results based on search query parameters

**Props**: None (reads from URL query parameters)

**Behavior**:
- Parse query parameters from URL
- Generate 3-5 mock flight results based on search criteria
- Display results in card layout
- No backend API calls in Phase 1
- Each result shows: Flight number, departure/arrival times, duration, price, cabin class
- Each result has a "Select" button

**Select Button Behavior (Phase 1)**:
- Clicking "Select" navigates to /bookings (PlaceholderPage)
- This provides a deterministic, testable behavior
- Future phases will implement actual booking flow

**Mock Data Generation**:
```typescript
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

function generateMockFlights(searchParams: SearchParams): MockFlight[] {
  // Generate 3-5 flights with varying prices and times
  // Use search params to make results feel relevant
  // Prices vary based on cabin class
  // Times are realistic for the route
}
```

### Health Page

#### HealthPage Component

**Purpose**: Display application health status and configuration

**Props**: None

**Display Elements**:
- Application name and version (placeholder: "1.0.0-dev")
- Build timestamp (placeholder: current date)
- Backend service URLs from env.ts:
  - Booking Service: {BOOKING_SERVICE_URL}
  - Inventory Service: {INVENTORY_SERVICE_URL}
  - Payment Service: {PAYMENT_SERVICE_URL}
  - Loyalty Service: {LOYALTY_SERVICE_URL}
  - Baggage Service: {BAGGAGE_SERVICE_URL}
- Status indicator: "Healthy" (green)

**Styling**: Simple card layout with Typography components, no complex styling

### Common Components

#### NotFound Component

**Purpose**: 404 error page for undefined routes

**Display**: Centered message "Page Not Found" with link back to home page

#### PlaceholderPage Component

**Purpose**: Placeholder for future pages (My Bookings, Help)

**Props**:
```typescript
interface PlaceholderPageProps {
  title: string;
  message: string;
}
```

**Display**: Centered card with title, "Coming Soon" message, and link back to home page

## Data Models

### Domain Enums (src/domain/enums.ts)

```typescript
export enum TripType {
  ONE_WAY = 'one-way',
  ROUND_TRIP = 'round-trip',
  MULTI_CITY = 'multi-city',
}

export enum CabinClass {
  ECONOMY = 'economy',
  PREMIUM_ECONOMY = 'premium-economy',
  BUSINESS = 'business',
  FIRST = 'first',
}

export enum Currency {
  USD = 'USD',
  EUR = 'EUR',
  GBP = 'GBP',
  JPY = 'JPY',
}
```

### Domain Types (src/domain/types.ts)

```typescript
export interface Airport {
  iata: string;        // 3-letter code (e.g., "JFK")
  city: string;        // City name (e.g., "New York")
  name: string;        // Full airport name (e.g., "John F. Kennedy International Airport")
  country?: string;    // Optional country code
}

export interface SearchCriteria {
  tripType: TripType;
  origin: Airport;
  destination: Airport;
  departDate: Date;
  returnDate?: Date;
  paxAdults: number;
  paxChildren: number;
  cabinClass: CabinClass;
  currency: Currency;
}

export interface QueryParams {
  tripType: string;
  from: string;
  to: string;
  departDate: string;
  returnDate?: string;
  paxAdults: string;
  paxChildren: string;
  cabinClass: string;
  currency: string;
}
```

### Airport Data (src/data/airports.json)

```json
[
  {
    "iata": "JFK",
    "city": "New York",
    "name": "John F. Kennedy International Airport",
    "country": "US"
  },
  {
    "iata": "LAX",
    "city": "Los Angeles",
    "name": "Los Angeles International Airport",
    "country": "US"
  },
  {
    "iata": "LHR",
    "city": "London",
    "name": "London Heathrow Airport",
    "country": "GB"
  },
  {
    "iata": "CDG",
    "city": "Paris",
    "name": "Charles de Gaulle Airport",
    "country": "FR"
  },
  {
    "iata": "FRA",
    "city": "Frankfurt",
    "name": "Frankfurt Airport",
    "country": "DE"
  },
  {
    "iata": "DXB",
    "city": "Dubai",
    "name": "Dubai International Airport",
    "country": "AE"
  },
  {
    "iata": "SIN",
    "city": "Singapore",
    "name": "Singapore Changi Airport",
    "country": "SG"
  },
  {
    "iata": "HND",
    "city": "Tokyo",
    "name": "Tokyo Haneda Airport",
    "country": "JP"
  },
  {
    "iata": "SYD",
    "city": "Sydney",
    "name": "Sydney Kingsford Smith Airport",
    "country": "AU"
  },
  {
    "iata": "ORD",
    "city": "Chicago",
    "name": "O'Hare International Airport",
    "country": "US"
  },
  {
    "iata": "ATL",
    "city": "Atlanta",
    "name": "Hartsfield-Jackson Atlanta International Airport",
    "country": "US"
  },
  {
    "iata": "DFW",
    "city": "Dallas",
    "name": "Dallas/Fort Worth International Airport",
    "country": "US"
  },
  {
    "iata": "AMS",
    "city": "Amsterdam",
    "name": "Amsterdam Airport Schiphol",
    "country": "NL"
  },
  {
    "iata": "MAD",
    "city": "Madrid",
    "name": "Adolfo Suárez Madrid-Barajas Airport",
    "country": "ES"
  },
  {
    "iata": "BCN",
    "city": "Barcelona",
    "name": "Barcelona-El Prat Airport",
    "country": "ES"
  },
  {
    "iata": "MUC",
    "city": "Munich",
    "name": "Munich Airport",
    "country": "DE"
  },
  {
    "iata": "FCO",
    "city": "Rome",
    "name": "Leonardo da Vinci-Fiumicino Airport",
    "country": "IT"
  },
  {
    "iata": "YYZ",
    "city": "Toronto",
    "name": "Toronto Pearson International Airport",
    "country": "CA"
  },
  {
    "iata": "MEX",
    "city": "Mexico City",
    "name": "Mexico City International Airport",
    "country": "MX"
  },
  {
    "iata": "GRU",
    "city": "São Paulo",
    "name": "São Paulo/Guarulhos International Airport",
    "country": "BR"
  }
]
```

### Test IDs (src/testing/testIds.ts)

```typescript
export const testIds = {
  appShell: {
    root: 'app-shell',
  },
  navbar: {
    root: 'navbar',
    logo: 'navbar-logo',
    homeLink: 'navbar-home-link',
    bookingsLink: 'navbar-bookings-link',
    helpLink: 'navbar-help-link',
  },
  bookingStepperPlaceholder: {
    root: 'booking-stepper-placeholder',
  },
  tripSummaryPlaceholder: {
    root: 'trip-summary-placeholder',
  },
  home: {
    root: 'home-page',
  },
  hero: {
    root: 'hero-section',
  },
  flightSearch: {
    root: 'flight-search-widget',
    tripTypeOneWay: 'trip-type-one-way',
    tripTypeRoundTrip: 'trip-type-round-trip',
    tripTypeMultiCity: 'trip-type-multi-city',
    originInput: 'origin-input',
    destinationInput: 'destination-input',
    departDateInput: 'depart-date-input',
    returnDateInput: 'return-date-input',
    paxAdultsInput: 'pax-adults-input',
    paxChildrenInput: 'pax-children-input',
    cabinClassSelect: 'cabin-class-select',
    currencySelect: 'currency-select',
    searchButton: 'search-button',
  },
  results: {
    root: 'results-page',
    flightCard: 'flight-card',
    selectFlightButton: 'select-flight-button',
  },
  health: {
    root: 'health-page',
    statusIndicator: 'status-indicator',
    versionInfo: 'version-info',
  },
  placeholder: {
    root: 'placeholder-page',
    backHomeLink: 'placeholder-back-home-link',
  },
} as const;

export type TestIds = typeof testIds;
```

### Environment Configuration (src/config/env.ts)

```typescript
interface EnvironmentConfig {
  bookingServiceUrl: string;
  inventoryServiceUrl: string;
  paymentServiceUrl: string;
  loyaltyServiceUrl: string;
  baggageServiceUrl: string;
}

function getEnvVar(key: string, defaultValue?: string): string {
  const value = import.meta.env[key] || defaultValue;
  if (!value) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return value;
}

export const env: EnvironmentConfig = {
  bookingServiceUrl: getEnvVar('VITE_BOOKING_SERVICE_URL', 'http://localhost:8081'),
  inventoryServiceUrl: getEnvVar('VITE_INVENTORY_SERVICE_URL', 'http://localhost:8082'),
  paymentServiceUrl: getEnvVar('VITE_PAYMENT_SERVICE_URL', 'http://localhost:8083'),
  loyaltyServiceUrl: getEnvVar('VITE_LOYALTY_SERVICE_URL', 'http://localhost:8084'),
  baggageServiceUrl: getEnvVar('VITE_BAGGAGE_SERVICE_URL', 'http://localhost:8085'),
};
```

### Hero Images (src/assets/heroImages.ts)

```typescript
export const heroImages = {
  home: 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=1920&q=80',
  results: 'https://images.unsplash.com/photo-1464037866556-6812c9d1c72e?w=1920&q=80',
  confirmation: 'https://images.unsplash.com/photo-1488085061387-422e29b40080?w=1920&q=80',
} as const;
```

### MUI Theme Configuration (src/theme/theme.ts)

```typescript
import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    primary: {
      main: '#0B2A4A',      // Deep blue
      light: '#1a3d5f',
      dark: '#061a2f',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#1976d2',      // Sky blue
      light: '#42a5f5',
      dark: '#1565c0',
      contrastText: '#ffffff',
    },
    warning: {
      main: '#FFD700',      // Gold accent
      light: '#ffe54c',
      dark: '#c7a600',
      contrastText: '#000000',
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
    text: {
      primary: '#212121',
      secondary: '#757575',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '3rem',
      fontWeight: 700,
      lineHeight: 1.2,
    },
    h2: {
      fontSize: '2.5rem',
      fontWeight: 700,
      lineHeight: 1.3,
    },
    h3: {
      fontSize: '2rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h4: {
      fontSize: '1.75rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h5: {
      fontSize: '1.5rem',
      fontWeight: 500,
      lineHeight: 1.5,
    },
    h6: {
      fontSize: '1.25rem',
      fontWeight: 500,
      lineHeight: 1.5,
    },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  spacing: 8,
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: '10px 24px',
        },
        sizeLarge: {
          padding: '14px 32px',
          fontSize: '1.125rem',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
        },
      },
    },
    MuiTextField: {
      defaultProps: {
        variant: 'outlined',
      },
    },
  },
});
```

## Routing Configuration

### Route Definitions (src/App.tsx)

```typescript
import { BrowserRouter, Routes, Route } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppShell />}>
          <Route index element={<HomePage />} />
          <Route path="results" element={<ResultsPage />} />
          <Route path="health" element={<HealthPage />} />
          <Route path="bookings" element={<PlaceholderPage title="My Bookings" message="Booking management coming soon" />} />
          <Route path="help" element={<PlaceholderPage title="Help" message="Help center coming soon" />} />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
```

### Navigation Utilities (src/utils/queryParams.ts)

```typescript
import type { SearchCriteria, QueryParams, Airport } from '../domain/types';
import { TripType, CabinClass, Currency } from '../domain/enums';

export function searchCriteriaToQueryParams(criteria: SearchCriteria): QueryParams {
  return {
    tripType: criteria.tripType,
    from: criteria.origin.iata,
    to: criteria.destination.iata,
    departDate: criteria.departDate.toISOString().split('T')[0], // YYYY-MM-DD format
    returnDate: criteria.returnDate?.toISOString().split('T')[0],
    paxAdults: criteria.paxAdults.toString(),
    paxChildren: criteria.paxChildren.toString(),
    cabinClass: criteria.cabinClass,
    currency: criteria.currency,
  };
}

/**
 * Parse YYYY-MM-DD string to local Date to avoid timezone issues
 * @param ymd Date string in YYYY-MM-DD format
 * @returns Date object in local timezone
 */
function parseYmdToLocalDate(ymd: string): Date {
  const [year, month, day] = ymd.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function queryParamsToSearchCriteria(
  params: URLSearchParams,
  airports: Airport[]
): Partial<SearchCriteria> {
  const findAirport = (iata: string) => airports.find(a => a.iata === iata);
  
  return {
    tripType: params.get('tripType') as TripType,
    origin: findAirport(params.get('from') || ''),
    destination: findAirport(params.get('to') || ''),
    departDate: params.get('departDate') ? parseYmdToLocalDate(params.get('departDate')!) : undefined,
    returnDate: params.get('returnDate') ? parseYmdToLocalDate(params.get('returnDate')!) : undefined,
    paxAdults: parseInt(params.get('paxAdults') || '1'),
    paxChildren: parseInt(params.get('paxChildren') || '0'),
    cabinClass: params.get('cabinClass') as CabinClass,
    currency: params.get('currency') as Currency,
  };
}
```

## React Query Setup

### Query Client Configuration (src/main.tsx)

```typescript
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,      // 5 minutes
      gcTime: 10 * 60 * 1000,        // 10 minutes (formerly cacheTime in v4)
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

// Wrap App with QueryClientProvider and LocalizationProvider
<QueryClientProvider client={queryClient}>
  <LocalizationProvider dateAdapter={AdapterDayjs}>
    <App />
  </LocalizationProvider>
</QueryClientProvider>
```

### Axios Configuration (src/config/axios.ts)

```typescript
import axios from 'axios';

export const apiClient = axios.create({
  timeout: 10000,
  // Note: Content-Type is NOT set here as default
  // Different services require different content types:
  // - Booking/Inventory/Payment: application/json
  // - Baggage: application/xml
  // Set Content-Type per-request as needed
});

// Request interceptor for adding correlation IDs
apiClient.interceptors.request.use((config) => {
  // Use crypto.randomUUID with fallback for test environments
  const correlationId = typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `fallback-${Date.now()}`;
  
  config.headers['X-Correlation-Id'] = correlationId;
  return config;
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);
```

Note: In Phase 1, React Query and Axios are configured but not actively used since ResultsPage displays mock data. Future phases will implement actual API calls.


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Navbar Persistence Across Routes

*For any* route in the application (/, /results, /health, or 404), the navigation bar should be rendered and visible at the top of the page.

**Validates: Requirements 3.1**

### Property 2: SPA Navigation Without Page Reload

*For any* navigation action (clicking navigation links, browser back/forward buttons, or direct URL access), the system should render the correct page content, update the browser URL, and complete the navigation without triggering a full page reload.

**Validates: Requirements 3.5, 3.6, 10.5, 10.6, 10.7**

### Property 3: Airport Data Structure Completeness

*For any* airport entry in src/data/airports.json, the entry should contain all required fields: iata (string), city (string), and name (string).

**Validates: Requirements 5.16**

### Property 4: Search Criteria to Query Parameters Encoding

*For any* valid search criteria (trip type, origin, destination, dates, passengers, cabin class, currency), when the user submits the search form, the system should navigate to /results with all search parameters correctly encoded in the URL query string using the standardized parameter names.

**Validates: Requirements 5.11, 5.12, 10.8**

### Property 5: Form Validation Lifecycle

*For any* form field with validation rules, when the field contains invalid data and the user attempts to submit, the system should prevent form submission, display an error message near the invalid field, and clear the error message when the user corrects the field value.

**Validates: Requirements 6.6, 6.7, 6.8**

### Property 6: Stable Test Identifiers

*For any* interactive element (buttons, links, form inputs, navigation elements), the element should have a data-testid attribute with a descriptive kebab-case value that remains consistent across renders and does not contain dynamically generated values (timestamps, random IDs, etc.).

**Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7**

### Property 7: Responsive Layout Adaptation

*For any* viewport width below the tablet breakpoint (typically 768px), the system should adjust layouts to mobile-appropriate patterns (stacked form fields, adjusted navigation, responsive typography).

**Validates: Requirements 8.3, 8.7**

### Property 8: Semantic HTML Structure

*For any* page in the application, the DOM structure should use semantic HTML elements (header, nav, main, section, article, footer, button, etc.) rather than generic div/span elements for structural content.

**Validates: Requirements 9.1**

### Property 9: ARIA Labels for Icon-Only Elements

*For any* interactive element that lacks visible text content (icon buttons, logo links, etc.), the element should have an appropriate ARIA label (aria-label or aria-labelledby) to provide context for screen readers.

**Validates: Requirements 9.2**

### Property 10: Keyboard Navigation Support

*For any* interactive element in the application, the element should be reachable and operable using only keyboard navigation (Tab, Shift+Tab, Enter, Space, Arrow keys where appropriate).

**Validates: Requirements 9.3, 9.6**

### Property 11: Color Contrast Compliance

*For any* text content in the application, the color contrast ratio between text and background should meet WCAG 2.1 AA standards (4.5:1 for normal text, 3:1 for large text).

**Validates: Requirements 9.4**

### Property 12: Screen Reader Error Announcements

*For any* form validation error, the error message should have appropriate ARIA attributes (aria-live, aria-invalid, aria-describedby) to ensure screen readers announce the error to users.

**Validates: Requirements 9.5**

### Property 13: Mock Results Generation from Query Parameters

*For any* valid set of query parameters on the /results route, the system should generate and display mock flight results that reflect the search criteria (origin, destination, dates, cabin class, passenger counts).

**Validates: Requirements 10.10**

## Error Handling

### Form Validation Errors

**Client-Side Validation**:
- All form validation occurs before navigation
- Validation errors prevent form submission
- Error messages are displayed inline below the relevant field
- Error messages use MUI's FormHelperText component with error prop
- Error state is managed in component state (errors object)

**Validation Error Messages**:
```typescript
const validationMessages = {
  originRequired: 'Please select a departure airport',
  destinationRequired: 'Please select a destination airport',
  destinationSameAsOrigin: 'Destination must be different from origin',
  departDateRequired: 'Please select a departure date',
  departDatePast: 'Departure date cannot be in the past',
  returnDateRequired: 'Please select a return date for round-trip flights',
  returnDateBeforeDepart: 'Return date must be after departure date',
  paxAdultsMin: 'At least 1 adult passenger is required',
  paxAdultsMax: 'Maximum 9 adult passengers allowed',
  paxChildrenMax: 'Maximum 9 child passengers allowed',
  totalPaxMax: 'Total passengers cannot exceed 9',
};
```

### Route Not Found (404)

**Behavior**:
- Any undefined route displays the NotFound component
- NotFound component shows a friendly message
- Provides a link back to the home page
- Maintains the AppShell layout (navbar remains visible)

**Implementation**:
```typescript
<Route path="*" element={<NotFound />} />
```

### Environment Configuration Errors

**Missing Environment Variables**:
- The env.ts module throws an error if required VITE_* variables are missing
- Error is thrown at module load time (fail-fast approach)
- Error message indicates which variable is missing
- Development server will not start if configuration is invalid

**Error Handling**:
```typescript
function getEnvVar(key: string, defaultValue?: string): string {
  const value = import.meta.env[key] || defaultValue;
  if (!value) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
  return value;
}
```

### Future Error Handling (Post-Phase 1)

**API Request Errors**:
- React Query will handle API errors in future phases
- Error boundaries will catch and display component errors
- Toast notifications for transient errors
- Error pages for critical failures

## Testing Strategy

### Dual Testing Approach

The application requires both unit tests and property-based tests for comprehensive coverage:

**Unit Tests**:
- Specific examples of component rendering
- Edge cases (empty states, boundary values)
- Error conditions (validation failures, 404 routes)
- Integration points (router navigation, theme application)
- Mock data generation logic

**Property-Based Tests**:
- Universal properties that hold for all inputs
- Comprehensive input coverage through randomization
- Validation of correctness properties defined above
- Minimum 100 iterations per property test

### Testing Framework

**Technology Stack**:
- **Test Runner**: Vitest (Vite-native test runner)
- **React Testing**: React Testing Library (@testing-library/react)
- **User Interactions**: @testing-library/user-event
- **Property-Based Testing**: fast-check (JavaScript property testing library)
- **Assertions**: Vitest's built-in assertions (Jest-compatible)

### Property-Based Test Configuration

Each property test must:
1. Run minimum 100 iterations with randomized inputs
2. Reference the design document property in a comment
3. Use the tag format: `// Feature: airline-booking-frontend, Property N: [property text]`
4. Generate valid random inputs using fast-check arbitraries

**Example Property Test Structure**:
```typescript
import { test } from 'vitest';
import fc from 'fast-check';

// Feature: airline-booking-frontend, Property 4: Search Criteria to Query Parameters Encoding
test('search form encodes all criteria as query parameters', () => {
  fc.assert(
    fc.property(
      fc.record({
        tripType: fc.constantFrom('one-way', 'round-trip', 'multi-city'),
        origin: fc.constantFrom('JFK', 'LAX', 'LHR'),
        destination: fc.constantFrom('CDG', 'FRA', 'DXB'),
        departDate: fc.date({ min: new Date() }),
        paxAdults: fc.integer({ min: 1, max: 9 }),
        paxChildren: fc.integer({ min: 0, max: 9 }),
        cabinClass: fc.constantFrom('economy', 'business', 'first'),
        currency: fc.constantFrom('USD', 'EUR', 'GBP'),
      }),
      (searchCriteria) => {
        // Test implementation
        // 1. Render FlightSearchWidget
        // 2. Fill form with searchCriteria
        // 3. Submit form
        // 4. Assert URL contains all expected query parameters
      }
    ),
    { numRuns: 100 }
  );
});
```

### Unit Test Patterns

**Component Rendering Tests**:
```typescript
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { theme } from '../theme/theme';

function renderWithProviders(component: React.ReactElement) {
  return render(
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        {component}
      </ThemeProvider>
    </BrowserRouter>
  );
}

test('navbar displays brand logo', () => {
  renderWithProviders(<Navbar />);
  expect(screen.getByTestId(testIds.navbar.logo)).toBeInTheDocument();
});
```

**Form Validation Tests**:
```typescript
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

test('displays error when origin is empty and form is submitted', async () => {
  const user = userEvent.setup();
  renderWithProviders(<FlightSearchWidget />);
  
  const searchButton = screen.getByTestId(testIds.flightSearch.searchButton);
  await user.click(searchButton);
  
  expect(screen.getByText('Please select a departure airport')).toBeInTheDocument();
});
```

**Navigation Tests**:
```typescript
test('clicking home link navigates to home page', async () => {
  const user = userEvent.setup();
  renderWithProviders(<App />);
  
  // Navigate away from home
  const resultsLink = screen.getByText('My Bookings');
  await user.click(resultsLink);
  
  // Navigate back to home
  const homeLink = screen.getByTestId(testIds.navbar.homeLink);
  await user.click(homeLink);
  
  expect(screen.getByText('Find Your Next Adventure')).toBeInTheDocument();
});
```

### Test Coverage Goals

**Minimum Coverage Targets**:
- Statements: 80%
- Branches: 75%
- Functions: 80%
- Lines: 80%

**Priority Areas for Coverage**:
1. Form validation logic (100% coverage)
2. Navigation and routing (100% coverage)
3. Query parameter encoding/decoding (100% coverage)
4. Component rendering with various props (80% coverage)
5. Responsive behavior (manual testing + automated viewport tests)
6. Accessibility features (automated + manual screen reader testing)

### Testing Phases

**Phase 0 Testing**:
- Theme configuration tests
- Router setup tests
- Environment configuration tests
- AppShell layout tests

**Phase 1 Testing**:
- FlightSearchWidget rendering tests
- Form validation tests (all validation rules)
- Navigation to results tests
- Query parameter encoding tests
- Mock results generation tests
- Accessibility tests (keyboard navigation, ARIA labels, color contrast)
- Property-based tests for Properties 1-13

**Future Phase Testing**:
- API integration tests with React Query
- End-to-end booking flow tests
- Payment form tests
- Confirmation page tests

### Manual Testing Checklist

**Browser Compatibility**:
- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

**Responsive Testing**:
- Desktop (1920x1080, 1366x768)
- Tablet (768x1024)
- Mobile (375x667, 414x896)

**Accessibility Testing**:
- Screen reader testing (NVDA, JAWS, VoiceOver)
- Keyboard-only navigation
- Color contrast verification (WebAIM Contrast Checker)
- Focus indicator visibility

**Performance Testing**:
- Lighthouse audit (Performance, Accessibility, Best Practices, SEO)
- Bundle size analysis
- Time to Interactive (TTI) < 3 seconds

## Implementation Notes

### Phase 0 Deliverables

1. Vite project scaffold with TypeScript configuration
2. Package.json with all required dependencies (React 18, MUI v5, MUI X Date Pickers, Day.js, React Router v6, TanStack React Query v5, Axios, Vitest, React Testing Library, fast-check)
3. MUI theme configuration (src/theme/theme.ts)
4. Environment configuration (src/config/env.ts and .env.local with all 5 service URLs)
5. Router setup with route definitions (src/App.tsx) including placeholder routes for /bookings and /help
6. AppShell component with navbar and placeholders
7. Navbar component with Home, My Bookings, and Help links
8. PlaceholderPage component for future pages
9. Test ID constants (src/testing/testIds.ts)
10. Domain enums (src/domain/enums.ts)
11. Hero images constants (src/assets/heroImages.ts)
12. Airport data JSON file (src/data/airports.json)
13. React Query client setup with LocalizationProvider for Day.js
14. Axios client configuration (without default Content-Type header)

### Phase 1 Deliverables

1. HomePage component with HeroSection
2. FlightSearchWidget component with full form functionality
3. Form validation logic
4. Query parameter utilities (src/utils/queryParams.ts)
5. ResultsPage component with mock data generation
6. HealthPage component
7. PlaceholderPage component (already created in Phase 0)
8. All unit tests for Phase 0 and Phase 1 components
9. All property-based tests for Properties 1-13

### Development Workflow

**Starting the Application**:
```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Application will be available at http://localhost:5173
```

**Running Tests**:
```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage
```

**Building for Production**:
```bash
# Create production build
npm run build

# Preview production build
npm run preview
```

### Code Quality Standards

**TypeScript**:
- Strict mode enabled
- No implicit any
- All props interfaces explicitly defined
- All function return types explicitly defined

**ESLint Rules**:
- React Hooks rules enforced
- Unused variables not allowed
- Console statements flagged (except console.error)
- Import order enforced

**Component Standards**:
- Functional components only (no class components)
- Props destructured in function signature
- Default props defined using default parameters
- Event handlers prefixed with "handle" (handleSubmit, handleChange)
- Test IDs imported from testIds.ts (no string literals)

**File Naming**:
- Components: PascalCase (FlightSearchWidget.tsx)
- Utilities: camelCase (queryParams.ts)
- Constants: camelCase (testIds.ts, heroImages.ts)
- Types: PascalCase (types.ts exports PascalCase interfaces)

### Future Enhancements (Post-Phase 1)

**Phase 2: Search Results**:
- Real API integration with inventory service
- Flight result cards with detailed information
- Sorting and filtering options
- Pagination for large result sets

**Phase 3: Passenger Details**:
- Passenger information form
- Contact details form
- Form persistence in session storage

**Phase 4: Add-ons**:
- Baggage selection integration
- Seat selection integration
- Special requests

**Phase 5: Payment**:
- Payment form with validation
- Payment service integration
- Booking confirmation polling

**Phase 6: Confirmation**:
- Booking confirmation display
- Email confirmation (future)
- Booking management features
