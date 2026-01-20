import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Request interceptor for error handling
api.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || 'An error occurred';
    return Promise.reject(new Error(message));
  }
);

export const API = {
  // Upload CSV file with students
  uploadStudents: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/api/students/upload', formData);
    return response.data;
  },

  // Legacy CSV upload (keeping for compatibility)
  uploadCSV: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/api/seating/upload-csv', formData);
    return response.data;
  },

  // Create new exam hall
  createHall: async (hallDTO) => {
    const response = await api.post('/api/halls', hallDTO);
    return response.data;
  },

  // Generate random seating
  generateRandom: async (request) => {
    const response = await api.post('/api/seating/random', request);
    return response.data;
  },

  // Optimize seating allocation
  optimizeSeating: async (request) => {
    const response = await api.post('/api/seating/optimize', request);
    return response.data;
  },

  // Legacy allocate seats (keeping for compatibility)
  allocateSeats: async (request) => {
    const response = await api.post('/api/seating/allocate', request);
    return response.data;
  },

  // Get risk analysis for hall
  getRiskAnalysis: async (hallId) => {
    const response = await api.get(`/api/seating/risk/${hallId}`);
    return response.data;
  },

  // Export seating chart as PDF
  exportPDF: async (hallId) => {
    const response = await api.get(`/api/export/pdf/${hallId}`, {
      responseType: 'blob'
    });
    
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${hallId}_seating_chart.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  },

  // Get all halls
  getAllHalls: async () => {
    const response = await api.get('/api/halls');
    return response.data;
  }
};

export default api;