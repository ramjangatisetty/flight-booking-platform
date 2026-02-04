# Implementation Plan: Airline Booking Frontend

## Overview

This implementation plan covers Phase 0 (project infrastructure and scaffolding) and Phase 1 (home page with flight search functionality). The application is a React 18 + TypeScript + MUI v5 single-page application built with Vite, designed for enterprise-grade airline booking with automation testing support.

## Tasks

- [x] 1. Initialize Vite React TypeScript project
  - Create new Vite project with React + TypeScript template
  - Configure TypeScript with strict mode enabled
  - Set up ESLint with React and TypeScript rules
  - Configure Vite for development and production builds
  - _Requirements: 1.1, 1.2, 1.7, 1.8, 1.9_

- [ ] 2. Install and configure dependencies
  - [x] 2.1 Install core dependencies
    - Install MUI v5 core and icons packages
    - Install MUI X Date Pickers and Day.js
    - Install React Router v6
    - Install TanStack React Query v5
    - Install Axios
    - _Requirements: 1.3, 1.4, 1.5, 1.6_
  
  - [x] 2.2 Install testing dependencies
    - Install Vitest, React Testing Library, and fast-check
    - Create vitest.config.ts with jsdom environment
    - Create test setup file (src/test/setup.ts) for global test configuration
    - _Requirements: 1.9_

- [ ] 3. Create environment configuration
  - [x] 3.1 Create .env.local file with VITE_* variables for all 5 backend services
    - Define VITE_BOOKING_SERVICE_URL=http://localhost:8081
    - Define VITE_INVENTORY_SERVICE_URL=http://localhost:8082
    - Define VITE_PAYMENT_SERVICE_URL=http://localhost:8083
    - Define VITE_LOYALTY_SERVICE_URL=http://localhost:8084
    - Define VITE_BAGGAGE_SERVICE_URL=http://localhost:8085
    - _Requirements: 11.1, 11.2, 11.3, 11.8, 11.9, 11.10, 11.11, 11.12_
  
  - [x] 3.2 Create src/config/env.ts module
    - Implement getEnvVar helper with error handling for missing variables
    - Export typed EnvironmentConfig object with all service URLs
    - Use import.meta.env to read Vite environment variables
    - _Requirements: 11.4, 11.5_

- [ ] 4. Create domain models and constants
  - [x] 4.1 Create src/domain/enums.ts
    - Define TripType enum (ONE_WAY, ROUND_TRIP, MULTI_CITY)
    - Define CabinClass enum (ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST)
    - Define Currency enum (USD, EUR, GBP, JPY)
    - _Requirements: 5.18_
  
  - [x] 4.2 Create src/domain/types.ts
    - Define Airport interface (iata, city, name, country)
    - Define SearchCriteria interface
    - Define QueryParams interface
    - _Requirements: 5.16_
  
  - [x] 4.3 Create src/data/airports.json
    - Add 20 major airports with IATA codes, city names, and full names
    - Include airports: JFK, LAX, LHR, CDG, FRA, DXB, SIN, HND, SYD, ORD, ATL, DFW, AMS, MAD, BCN, MUC, FCO, YYZ, MEX, GRU
    - _Requirements: 5.15, 5.16_
  
  - [x] 4.4 Create src/assets/heroImages.ts
    - Define hero image URLs for home, results, and confirmation pages
    - Use Unsplash placeholder URLs optimized for web
    - _Requirements: 4.8_
  
  - [x] 4.5 Create src/testing/testIds.ts
    - Define test ID constants for all components (appShell, navbar, hero, home, flightSearch, results, health, placeholder, bookingStepperPlaceholder, tripSummaryPlaceholder)
    - Use kebab-case naming convention
    - Export as const for type safety
    - _Requirements: 3.10, 5.17, 7.8, 7.9_
  
  - [x] 4.6 Verify enum imports
    - Ensure TripType, CabinClass, Currency are imported from src/domain/enums.ts in all files that use them
    - Specifically verify imports in src/utils/queryParams.ts
    - _Requirements: 5.18_

- [ ] 5. Create MUI theme configuration
  - [x] 5.1 Create src/theme/theme.ts
    - Configure primary color (#0B2A4A deep blue)
    - Configure secondary color (#1976d2 sky blue)
    - Configure warning color (#FFD700 gold accent)
    - Define typography settings (Roboto font family, heading sizes, button styles)
    - Define spacing (8px base) and shape (8px border radius)
    - Customize MUI component defaults (Button, Card, TextField)
    - _Requirements: 2.1, 2.2, 2.3, 2.5_

- [ ] 6. Set up React Query and Axios
  - [x] 6.1 Create src/config/axios.ts
    - Create Axios instance with 10-second timeout
    - Do NOT set default Content-Type header
    - Add request interceptor for X-Correlation-Id header with crypto.randomUUID fallback
    - Add response interceptor for error logging
    - _Requirements: 1.6_
  
  - [x] 6.2 Configure React Query and theme in src/main.tsx
    - Create QueryClient with staleTime (5 min), gcTime (10 min), retry (1), refetchOnWindowFocus (false)
    - Wrap App with ThemeProvider using theme from src/theme/theme.ts
    - Add CssBaseline component for consistent baseline styles
    - Wrap App with QueryClientProvider
    - Wrap App with LocalizationProvider using AdapterDayjs
    - _Requirements: 1.5, 2.6_

- [ ] 7. Create routing infrastructure
  - [x] 7.1 Create src/App.tsx with React Router
    - Set up BrowserRouter
    - Define routes: / (HomePage), /results (ResultsPage), /health (HealthPage), /bookings (PlaceholderPage), /help (PlaceholderPage), * (NotFound)
    - Use AppShell as layout wrapper for all routes
    - _Requirements: 1.3, 10.1, 10.2, 10.3, 10.4_
  
  - [x] 7.2 Create src/utils/queryParams.ts
    - Implement formatLocalYmd helper to convert Date to YYYY-MM-DD string
    - Implement searchCriteriaToQueryParams function using formatLocalYmd
    - Implement parseYmdToLocalDate helper for timezone-safe date parsing
    - Implement queryParamsToSearchCriteria function with proper imports from src/domain/types and src/domain/enums
    - _Requirements: 5.12, 10.8_

- [ ] 8. Create AppShell and layout components
  - [x] 8.1 Create src/components/layout/AppShell.tsx
    - Render Navbar at top
    - Render main content area with React Router Outlet
    - Conditionally render TripSummaryPlaceholder on desktop (>= md breakpoint)
    - Apply grid layout with responsive columns
    - Assign data-testid from testIds.appShell.root
    - _Requirements: 3.1, 3.4, 3.5, 3.6, 3.9_
  
  - [x] 8.2 Create src/components/layout/Navbar.tsx
    - Render fixed AppBar with primary color background
    - Display brand logo/name on left
    - Render navigation links: Home (/), My Bookings (/bookings), Help (/help)
    - Use Link components from React Router
    - Assign data-testid values from testIds.navbar
    - _Requirements: 3.2, 3.3, 3.7_
  
  - [x] 8.3 Create src/components/layout/BookingStepperPlaceholder.tsx
    - Render empty Box with defined height (40px) and background color
    - Assign data-testid from testIds.bookingStepperPlaceholder.root
    - _Requirements: 3.8_
  
  - [x] 8.4 Create src/components/layout/TripSummaryPlaceholder.tsx
    - Render Paper component with width 320px and sticky positioning
    - Include placeholder text "Trip Summary (Coming Soon)"
    - Hidden on mobile (< md), visible on desktop (>= md)
    - Assign data-testid from testIds.tripSummaryPlaceholder.root
    - _Requirements: 3.9_

- [ ] 9. Create common components
  - [x] 9.1 Create src/components/common/NotFound.tsx
    - Display centered "Page Not Found" message
    - Include link back to home page
    - Use MUI Typography and Button components
    - _Requirements: 10.4_
  
  - [x] 9.2 Create src/components/common/PlaceholderPage.tsx
    - Accept title and message props
    - Display centered card with "Coming Soon" message
    - Include link back to home page
    - Assign data-testid from testIds.placeholder
    - _Requirements: 3.3_

- [x] 10. Create HealthPage component
  - Create src/pages/HealthPage.tsx
  - Display application name and version placeholder ("1.0.0-dev")
  - Display build timestamp placeholder (current date)
  - Display all 5 backend service URLs from env.ts
  - Display status indicator "Healthy" with green color
  - Use simple card layout with Typography components
  - Assign data-testid from testIds.health
  - _Requirements: 12.7, 12.8, 12.9, 12.10_

- [ ] 11. Create HomePage and HeroSection
  - [x] 11.1 Create src/pages/HomePage.tsx
    - Render HeroSection component
    - Assign data-testid from testIds.home.root
    - _Requirements: 4.1, 4.6_
  
  - [x] 11.2 Create src/components/home/HeroSection.tsx
    - Accept backgroundImage, title, and subtitle props
    - Render full-width Box with background image from heroImages.home
    - Apply dark gradient overlay: linear-gradient(rgba(11, 42, 74, 0.7), rgba(11, 42, 74, 0.5))
    - Set minimum height 600px for desktop
    - Display title (h2) and subtitle (h5) with white text
    - Center content with flexbox
    - Render FlightSearchWidget as child
    - Assign data-testid from testIds.hero.root
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7_

- [ ] 12. Create FlightSearchWidget component
  - [x] 12.1 Create src/components/home/FlightSearchWidget.tsx with form structure
    - Create component with internal state for all form fields
    - Use MUI Card with elevation={8}, borderRadius 16px, backdrop blur
    - Arrange form fields in responsive grid (2-3 columns desktop, 1 column mobile)
    - Assign data-testid from testIds.flightSearch.root
    - _Requirements: 5.1, 5.13, 5.14_
  
  - [x] 12.2 Implement form fields
    - Add trip type radio group (One-way, Round-trip, Multi-city) with data-testid
    - Add origin Autocomplete with airports.json data and data-testid on input element
    - Add destination Autocomplete with airports.json data and data-testid on input element
    - Add departure DatePicker with Day.js adapter and data-testid
    - Add return DatePicker (conditional on Round-trip) with data-testid
    - Add adult passenger Select/TextField with data-testid
    - Add children passenger Select/TextField with data-testid
    - Add cabin class Select with enum values and data-testid
    - Add currency Select with enum values and data-testid
    - Add search Button (primary variant) with data-testid
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 5.10, 5.17_
  
  - [x] 12.3 Implement form validation logic
    - Validate origin is required and valid airport
    - Validate destination is required, valid airport, and different from origin
    - Validate departure date is required and not in past
    - Validate return date is required for round-trip and >= departure date
    - Validate adult passengers minimum 1, maximum 9
    - Validate children passengers minimum 0, maximum 9
    - Validate total passengers (adults + children) maximum 9
    - Display error messages using FormHelperText below fields
    - Store errors in component state
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [x] 12.4 Implement form submission and navigation
    - On search button click, validate all fields
    - If validation fails, prevent submission and display errors
    - If validation passes, convert search criteria to query parameters
    - Navigate to /results with query parameters using React Router
    - Clear errors when user modifies invalid field
    - _Requirements: 5.11, 5.12, 6.6, 6.8_

- [ ] 13. Create ResultsPage component
  - [x] 13.1 Create src/pages/ResultsPage.tsx
    - Parse query parameters from URL using useSearchParams
    - Convert query parameters to search criteria using queryParamsToSearchCriteria
    - Generate 3-5 mock flights based on search criteria
    - Display mock flights in card layout
    - Assign data-testid from testIds.results.root
    - _Requirements: 10.9, 10.10_
  
  - [x] 13.2 Implement mock flight generation
    - Create generateMockFlights function
    - Generate realistic flight numbers, airlines, times based on search params
    - Vary prices based on cabin class (Economy < Premium < Business < First)
    - Include flight duration, stops, and other details
    - Return 3-5 mock flights
    - _Requirements: 10.10_
  
  - [x] 13.3 Add Select button to flight cards
    - Add "Select" button to each flight card
    - On click, navigate to /bookings (PlaceholderPage)
    - Assign data-testid from testIds.results.selectFlightButton
    - _Requirements: 10.10_

- [ ] 14. Checkpoint - Verify Phase 0 and Phase 1 implementation
  - [ ] 14.1 Verify application runs without errors
    - Start development server (npm run dev)
    - Confirm application loads at http://localhost:5173
    - Check browser console for no errors or warnings
    - _Requirements: 12.1, 12.2_
  
  - [ ] 14.2 Verify home page functionality
    - Confirm hero section displays with background image and gradient overlay
    - Confirm flight search widget renders with all form fields
    - Test form validation for each field (empty origin, empty destination, past date, invalid return date, invalid passenger counts)
    - Fill valid search criteria and submit form
    - Verify navigation to /results with correct query parameters in URL
    - _Requirements: 4.1, 5.11, 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [ ] 14.3 Verify navigation and routing
    - Test navigation to /health page and verify service URLs display
    - Test navigation to /bookings page and verify placeholder displays
    - Test navigation to /help page and verify placeholder displays
    - Test navigation to undefined route and verify 404 page displays
    - Test browser back button functionality
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [ ] 14.4 Verify results page functionality
    - Navigate to /results with query parameters
    - Confirm 3-5 mock flights display based on search criteria
    - Verify flight cards show flight number, times, duration, price, cabin class
    - Click "Select" button and verify navigation to /bookings
    - _Requirements: 10.10_
  
  - [ ] 14.5 Verify responsive design
    - Test layout on desktop viewport (1920x1080)
    - Test layout on tablet viewport (768x1024)
    - Test layout on mobile viewport (375x667)
    - Verify TripSummaryPlaceholder hidden on mobile, visible on desktop
    - Verify flight search form stacks vertically on mobile
    - _Requirements: 8.3, 8.5_
  
  - [ ] 14.6 Verify test identifiers
    - Inspect DOM and confirm all interactive elements have data-testid attributes
    - Verify test IDs use kebab-case naming
    - Verify test IDs match constants from testIds.ts
    - Verify no dynamically generated test IDs
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_
  
  - [ ] 14.7 Run test suite
    - Execute npm test
    - Confirm all unit tests pass
    - Confirm no test failures or errors
    - _Requirements: 12.5, 12.6_

## Phase 2: Backend Integration and Real Flight Search

- [ ] 15. Create API service layer
  - [ ] 15.1 Create src/services/inventoryService.ts
    - Implement searchFlights function that calls inventory service API
    - Use Axios client from src/config/axios.ts
    - Set Content-Type: application/json for inventory service
    - Parse search criteria and format as API request
    - Handle API errors and return typed response
    - _Requirements: TBD_
  
  - [ ] 15.2 Create src/domain/types.ts additions for API responses
    - Define Flight interface (flightId, flightNumber, airline, origin, destination, departTime, arriveTime, duration, price, currency, cabinClass, availableSeats, stops)
    - Define SearchFlightsRequest interface
    - Define SearchFlightsResponse interface
    - _Requirements: TBD_

- [ ] 16. Integrate React Query for flight search
  - [ ] 16.1 Create src/hooks/useFlightSearch.ts
    - Implement useQuery hook for flight search
    - Use inventoryService.searchFlights as query function
    - Configure query key based on search criteria
    - Handle loading, error, and success states
    - Enable query only when search criteria are valid
    - _Requirements: TBD_
  
  - [ ] 16.2 Update ResultsPage to use useFlightSearch hook
    - Remove mock data generation
    - Use useFlightSearch hook with query parameters
    - Display loading spinner while fetching
    - Display error message if API call fails
    - Display "No flights found" if results are empty
    - Display flight cards when data is available
    - _Requirements: TBD_

- [ ] 17. Enhance flight result cards
  - [ ] 17.1 Create src/components/results/FlightCard.tsx
    - Extract flight card from ResultsPage into reusable component
    - Accept Flight interface as prop
    - Display all flight details (airline logo, flight number, times, duration, stops, price)
    - Add "Select" button with data-testid
    - Apply premium card styling consistent with theme
    - _Requirements: TBD_
  
  - [ ] 17.2 Add flight details expansion
    - Add expandable section for additional flight details
    - Show baggage allowance, aircraft type, meal service
    - Use MUI Accordion or Collapse component
    - Maintain accessibility with proper ARIA attributes
    - _Requirements: TBD_

- [ ] 18. Implement sorting and filtering
  - [ ] 18.1 Create src/components/results/SortFilterBar.tsx
    - Add sort dropdown (Price: Low to High, Price: High to Low, Duration: Shortest, Departure: Earliest, Departure: Latest)
    - Add filter checkboxes (Direct flights only, Specific airlines, Departure time ranges)
    - Apply filters and sorting to flight results
    - Persist filter/sort state in URL query parameters
    - Assign data-testid to all interactive elements
    - _Requirements: TBD_
  
  - [ ] 18.2 Implement client-side filtering and sorting logic
    - Create utility functions for sorting flights
    - Create utility functions for filtering flights
    - Apply filters and sorting to API results
    - Update ResultsPage to use SortFilterBar
    - _Requirements: TBD_

- [ ] 19. Add pagination for large result sets
  - [ ] 19.1 Implement pagination component
    - Use MUI Pagination component
    - Display page numbers and navigation controls
    - Show results count (e.g., "Showing 1-10 of 45 flights")
    - Persist current page in URL query parameters
    - Assign data-testid to pagination controls
    - _Requirements: TBD_
  
  - [ ] 19.2 Implement pagination logic
    - Paginate flight results (10 flights per page)
    - Update ResultsPage to display paginated results
    - Scroll to top when page changes
    - _Requirements: TBD_

- [ ] 20. Error handling and loading states
  - [ ] 20.1 Create src/components/common/LoadingSpinner.tsx
    - Create reusable loading spinner component
    - Use MUI CircularProgress with centered layout
    - Add descriptive text (e.g., "Searching for flights...")
    - Assign data-testid for testing
    - _Requirements: TBD_
  
  - [ ] 20.2 Create src/components/common/ErrorMessage.tsx
    - Create reusable error message component
    - Accept error message and retry callback as props
    - Display user-friendly error message
    - Add "Try Again" button
    - Assign data-testid for testing
    - _Requirements: TBD_
  
  - [ ] 20.3 Update ResultsPage with loading and error states
    - Show LoadingSpinner while fetching flights
    - Show ErrorMessage if API call fails
    - Show "No flights found" message if results are empty
    - Provide retry functionality
    - _Requirements: TBD_

- [ ] 21. Update TripSummaryPlaceholder with real data
  - [ ] 21.1 Create src/components/layout/TripSummary.tsx
    - Replace TripSummaryPlaceholder with real TripSummary component
    - Display search criteria (origin, destination, dates, passengers, cabin class)
    - Display selected flight details (if flight is selected)
    - Display total price
    - Make sticky on desktop (>= md breakpoint)
    - Assign data-testid to all elements
    - _Requirements: TBD_
  
  - [ ] 21.2 Implement trip summary state management
    - Create context or state management for selected flight
    - Update TripSummary when flight is selected
    - Persist selected flight in session storage
    - _Requirements: TBD_

- [ ] 22. Checkpoint - Verify Phase 2 implementation
  - [ ] 22.1 Verify API integration
    - Start backend inventory service
    - Perform flight search from home page
    - Verify API call is made to inventory service
    - Verify results are displayed correctly
    - Check browser network tab for API requests
    - _Requirements: TBD_
  
  - [ ] 22.2 Verify sorting and filtering
    - Apply different sort options and verify results order
    - Apply filters and verify results are filtered correctly
    - Verify filter/sort state persists in URL
    - _Requirements: TBD_
  
  - [ ] 22.3 Verify pagination
    - Search for flights with many results
    - Verify pagination controls appear
    - Navigate between pages and verify results change
    - Verify page state persists in URL
    - _Requirements: TBD_
  
  - [ ] 22.4 Verify error handling
    - Stop backend service and perform search
    - Verify error message is displayed
    - Click "Try Again" and verify retry works
    - Verify loading spinner appears during fetch
    - _Requirements: TBD_
  
  - [ ] 22.5 Verify trip summary
    - Perform search and select a flight
    - Verify trip summary displays search criteria
    - Verify trip summary displays selected flight
    - Verify trip summary is sticky on desktop
    - _Requirements: TBD_

## Notes

- All components use TypeScript with explicit type definitions
- All interactive elements have data-testid attributes from centralized testIds.ts
- Form validation prevents submission when errors exist
- Query parameters use standardized naming (tripType, from, to, departDate, returnDate, paxAdults, paxChildren, cabinClass, currency)
- Date handling uses Day.js with MUI X Date Pickers
- Dates stored as Date objects in component state, converted to/from Day.js for DatePicker
- Query parameter dates parsed as local dates to avoid timezone issues
- TripSummaryPlaceholder hidden on mobile (< md), visible on desktop (>= md)
- ResultsPage displays mock data in Phase 1 (no backend API calls)
- Axios configured without default Content-Type (set per-request in future phases)
- Correlation ID uses crypto.randomUUID with fallback for test environments
- All routes use AppShell layout wrapper for consistent navigation
- Placeholder pages (/bookings, /help) prevent broken navigation links

## Phase 2 Notes

- Phase 2 replaces mock data with real API integration
- Inventory service API endpoint: GET /api/inventory/search
- API request format matches backend SearchFlightsRequest DTO
- API response format matches backend SearchFlightsResponse DTO
- React Query handles caching, loading states, and error handling
- Sorting and filtering are client-side operations on API results
- Pagination is client-side (10 flights per page)
- Trip summary displays search criteria and selected flight
- All Phase 2 components maintain Phase 1 test ID conventions
