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
    loadingSpinner: 'results-loading-spinner',
    errorMessage: 'results-error-message',
    retryButton: 'results-retry-button',
    noResultsMessage: 'results-no-results',
    sortSelect: 'results-sort-select',
    filterDirectFlights: 'results-filter-direct-flights',
    filterAirline: 'results-filter-airline',
    filterDepartureTime: 'results-filter-departure-time',
    pagination: 'results-pagination',
    resultsCount: 'results-count',
  },
  tripSummary: {
    root: 'trip-summary',
    origin: 'trip-summary-origin',
    destination: 'trip-summary-destination',
    dates: 'trip-summary-dates',
    passengers: 'trip-summary-passengers',
    cabinClass: 'trip-summary-cabin-class',
    selectedFlight: 'trip-summary-selected-flight',
    totalPrice: 'trip-summary-total-price',
  },
  loading: {
    root: 'loading-spinner',
    text: 'loading-text',
  },
  error: {
    root: 'error-message',
    text: 'error-text',
    retryButton: 'error-retry-button',
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
