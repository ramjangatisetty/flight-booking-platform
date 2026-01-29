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
