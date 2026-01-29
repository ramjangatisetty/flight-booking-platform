import axios from 'axios';

export const apiClient = axios.create({
  timeout: 10000,
});

apiClient.interceptors.request.use((config) => {
  const correlationId = typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `fallback-${Date.now()}`;
  
  config.headers['X-Correlation-Id'] = correlationId;
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);
