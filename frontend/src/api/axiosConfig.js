import axios from 'axios';

// ============================================================================
// AXIOS API CLIENT CONFIGURATION
// Purpose: This file creates a customized "axios" instance used for making all
// HTTP requests from the frontend to the backend services (via the API Gateway).
// 
// Why do this?: Instead of writing out the full backend URL and manually attaching
// the auth token to every single request across the app, we configure it once here.
// ============================================================================

// The base URL routes all traffic to the Spring Cloud API Gateway (port 8080).
// The gateway then forwards it to the correct microservice (auth, inventory, requests).
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Create a configured axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000, // If the backend doesn't respond in 15 seconds, fail the request
});

// ============================================================================
// REQUEST INTERCEPTOR (Outgoing traffic)
// Purpose: Runs automatically BEFORE every request is sent to the backend.
// Business Logic: We check if the user is logged in (has a token). If they do,
// we attach that token to the 'Authorization' header.
// Why it's needed: The backend requires this token to verify the user's identity
// and check if they have permission to access protected endpoints.
// ============================================================================
api.interceptors.request.use(
  (config) => {
    // Grab the saved JWT token from browser storage
    const token = localStorage.getItem('sms_token');
    
    // If it exists, append it using the standard Bearer scheme
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    // If something goes wrong before the request even leaves, reject it
    return Promise.reject(error);
  }
);

// ============================================================================
// RESPONSE INTERCEPTOR (Incoming traffic)
// Purpose: Runs automatically AFTER every response arrives from the backend.
// Business Logic: If the backend returns a 401 Unauthorized error (meaning
// the token expired or is invalid), we immediately log the user out and force
// them back to the Login page.
// Why it's needed: Prevents the app from breaking or showing errors if the user's
// session naturally expires while they are using the app.
// ============================================================================
api.interceptors.response.use(
  (response) => {
    // If the request was successful (200 OK), just pass the data through
    return response;
  },
  (error) => {
    // If the request failed, check if it was due to an invalid/expired token (401)
    if (error.response && error.response.status === 401) {
      // Clear out the stale login data
      localStorage.removeItem('sms_token');
      localStorage.removeItem('sms_user');
      localStorage.removeItem('sms_role');
      
      // Redirect the user to the login screen so they can re-authenticate
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    // For all other errors (like 500 server error, 404 not found, etc.), 
    // let the individual page handle it and show an error message
    return Promise.reject(error);
  }
);

export default api;
