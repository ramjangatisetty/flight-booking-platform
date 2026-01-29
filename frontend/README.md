# Airline Booking Frontend - Phase 0 & Phase 1

Enterprise-grade airline booking frontend built with React 18, TypeScript, and Material-UI v7.

## ✅ Implementation Complete

Phase 0 (Infrastructure) and Phase 1 (Home Page with Flight Search) are **fully implemented**.

### Completed Features

#### Phase 0: Infrastructure
- ✅ Vite + React 18 + TypeScript project
- ✅ Material-UI v7 with custom airline theme
- ✅ React Router v6 for navigation
- ✅ TanStack React Query v5 for state management
- ✅ Axios HTTP client with correlation ID interceptor
- ✅ MUI X Date Pickers with Day.js
- ✅ Vitest + React Testing Library
- ✅ Environment configuration (.env.local)
- ✅ Centralized test IDs for automation
- ✅ Domain models (enums, types)
- ✅ Airport data (20 major airports)
- ✅ Hero images configuration

#### Phase 1: Home Page & Flight Search
- ✅ Home page with hero section and background image
- ✅ Flight search widget with premium card design
- ✅ Form validation (origin, destination, dates, passengers)
- ✅ Autocomplete for airports
- ✅ Date pickers with timezone-safe handling
- ✅ Trip type selection (One-way, Round-trip, Multi-city)
- ✅ Passenger count inputs (adults, children)
- ✅ Cabin class selector (Economy, Premium, Business, First)
- ✅ Currency selector (USD, EUR, GBP, JPY)
- ✅ Navigation to results page with query parameters
- ✅ Mock results page (4 flights generated from search criteria)
- ✅ Health check page (UI, not JSON)
- ✅ Placeholder pages (My Bookings, Help)
- ✅ 404 Not Found page
- ✅ Responsive layout with sticky trip summary sidebar
- ✅ All interactive elements have stable data-testid attributes

## 🚀 Quick Start

### Prerequisites
- Node.js 18+ 
- npm 9+

### Installation

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies (already done)
npm install

# Start development server
npm run dev
```

The application will be available at **http://localhost:5173**

### Available Scripts

```bash
# Development server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run tests
npm test

# Lint code
npm run lint
```

## 📁 Project Structure

```
frontend/
├── src/
│   ├── assets/
│   │   └── heroImages.ts          # Hero image URLs
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppShell.tsx       # Main layout wrapper
│   │   │   ├── Navbar.tsx         # Top navigation
│   │   │   ├── BookingStepperPlaceholder.tsx
│   │   │   └── TripSummaryPlaceholder.tsx
│   │   ├── home/
│   │   │   ├── HeroSection.tsx    # Hero banner
│   │   │   └── FlightSearchWidget.tsx  # Search form
│   │   └── common/
│   │       ├── NotFound.tsx       # 404 page
│   │       └── PlaceholderPage.tsx
│   ├── pages/
│   │   ├── HomePage.tsx           # Home page
│   │   ├── ResultsPage.tsx        # Search results (mock)
│   │   └── HealthPage.tsx         # Health check UI
│   ├── domain/
│   │   ├── enums.ts               # TripType, CabinClass, Currency
│   │   └── types.ts               # TypeScript interfaces
│   ├── config/
│   │   ├── env.ts                 # Environment configuration
│   │   └── axios.ts               # HTTP client setup
│   ├── theme/
│   │   └── theme.ts               # MUI theme (airline branding)
│   ├── testing/
│   │   └── testIds.ts             # Centralized test IDs
│   ├── data/
│   │   └── airports.json          # 20 major airports
│   ├── utils/
│   │   └── queryParams.ts         # URL query parameter helpers
│   ├── test/
│   │   └── setup.ts               # Test configuration
│   ├── App.tsx                    # Router configuration
│   └── main.tsx                   # Application entry point
├── .env.local                      # Environment variables
├── vitest.config.ts                # Test configuration
├── tsconfig.json                   # TypeScript configuration
└── package.json                    # Dependencies
```

## 🎨 Design Features

### Theme
- **Primary Color**: Deep Blue (#0B2A4A) - Lufthansa-style
- **Secondary Color**: Sky Blue (#1976d2)
- **Accent Color**: Gold (#FFD700) for CTAs
- **Typography**: Roboto font family
- **Spacing**: 8px base unit
- **Border Radius**: 8px (buttons), 16px (cards)

### Hero Section
- Full-width background image with dark gradient overlay
- Minimum height: 600px (desktop)
- Centered content with premium floating search card

### Flight Search Widget
- Elevated card with strong shadow (elevation=8)
- Semi-transparent background with backdrop blur
- Responsive grid layout (2-3 columns desktop, 1 column mobile)
- Real-time validation with error messages
- Conditional return date field (Round-trip only)

### Results Page
- Mock flight generation based on search criteria
- Prices vary by cabin class
- 4 flights with realistic times and durations
- "Select" button navigates to placeholder booking page

## 🧪 Testing

### Test IDs
All interactive elements have stable `data-testid` attributes for automation testing:

```typescript
// Example usage
testIds.flightSearch.originInput
testIds.flightSearch.searchButton
testIds.results.selectFlightButton
```

### Running Tests
```bash
npm test
```

## 🔧 Configuration

### Environment Variables (.env.local)
```
VITE_BOOKING_SERVICE_URL=http://localhost:8081
VITE_INVENTORY_SERVICE_URL=http://localhost:8082
VITE_PAYMENT_SERVICE_URL=http://localhost:8083
VITE_LOYALTY_SERVICE_URL=http://localhost:8084
VITE_BAGGAGE_SERVICE_URL=http://localhost:8085
```

### Backend Services
The frontend is configured to connect to 5 backend microservices:
- **Booking Service** (8081): Booking management
- **Inventory Service** (8082): Seat availability
- **Payment Service** (8083): Payment processing
- **Loyalty Service** (8084): Loyalty points (SOAP)
- **Baggage Service** (8085): Baggage handling

**Note**: Phase 1 does NOT call backend APIs. Results page uses mock data only.

## 📋 Routes

| Route | Component | Description |
|-------|-----------|-------------|
| `/` | HomePage | Home page with flight search |
| `/results` | ResultsPage | Search results (mock data) |
| `/health` | HealthPage | Application health status (UI) |
| `/bookings` | PlaceholderPage | My Bookings (coming soon) |
| `/help` | PlaceholderPage | Help center (coming soon) |
| `*` | NotFound | 404 error page |

## 🎯 Form Validation Rules

### Flight Search Widget
- **Origin**: Required, must be valid airport
- **Destination**: Required, must be valid airport, must differ from origin
- **Departure Date**: Required, cannot be in the past
- **Return Date**: Required for round-trip, must be >= departure date
- **Adults**: Minimum 1, maximum 9
- **Children**: Minimum 0, maximum 9
- **Total Passengers**: Maximum 9 (adults + children)

## 🚫 Phase 1 Limitations (By Design)

- ❌ No backend API calls
- ❌ Results page uses mock data only
- ❌ No actual booking creation
- ❌ No payment processing
- ❌ No loyalty integration
- ❌ No baggage handling
- ❌ Placeholder pages for My Bookings and Help

These features will be implemented in future phases.

## 📦 Dependencies

### Core
- React 18.2
- TypeScript 5.9
- Material-UI v7.3
- React Router v7.13
- TanStack React Query v5.90
- Axios 1.13
- Day.js 1.11

### Development
- Vite 7.2
- Vitest 4.0
- React Testing Library 16.3
- fast-check 4.5 (property-based testing)
- ESLint 9.39

## 🔍 Automation Testing Support

All interactive elements have stable `data-testid` attributes managed through centralized constants in `src/testing/testIds.ts`. This enables:

- Self-healing locators during UI redesign
- Test impact analysis
- Flaky element detection
- Locator drift detection

Example:
```typescript
// In component
<Button data-testid={testIds.flightSearch.searchButton}>
  Search Flights
</Button>

// In test
const searchButton = screen.getByTestId(testIds.flightSearch.searchButton);
```

## 📝 Next Steps (Future Phases)

Phase 2 and beyond will implement:
- Backend API integration
- Real flight search results
- Passenger details form
- Add-ons (baggage, seats)
- Payment processing
- Booking confirmation
- Loyalty points integration
- Baggage tracking

## 🐛 Known Issues

None. Phase 0 and Phase 1 are fully functional.

## 📄 License

Private - Enterprise Project

---

**Built with ❤️ for enterprise-grade airline booking automation testing**
