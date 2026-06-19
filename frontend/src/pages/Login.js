/**
 * Login.js
 * 
 * Component Purpose: 
 * Handles user authentication (sign in) for the Stationery Management System.
 * 
 * Target User: 
 * All users (Students, Admins) who need to access the system to browse or manage inventory.
 * 
 * General Workflow:
 * 1. User enters their username and password.
 * 2. Form validation ensures both fields are populated.
 * 3. On submission, credentials are sent to the backend authentication API.
 * 4. On success, a token is received, stored globally via context, and the user is redirected to the dashboard.
 * 5. On failure, an appropriate error message is displayed.
 */
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Lock, Eye, EyeOff, AlertCircle, ArrowRight, PackageCheck, ClipboardCheck, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import './Auth.css';

const Login = () => {
  // --- State Variables ---
  // `username`: Stores the text typed by the user in the Username field. Updates as user types.
  const [username, setUsername] = useState('');
  
  // `password`: Stores the text typed by the user in the Password field. Updates as user types.
  const [password, setPassword] = useState('');
  
  // `error`: Stores error messages (from validation or API rejection). If truthy, an error banner renders in the UI.
  const [error, setError] = useState('');
  
  // `loading`: Tracks if the login API request is currently in flight. Disables form inputs and shows a spinner if true.
  const [loading, setLoading] = useState(false);
  
  // `showPassword`: Boolean that toggles the input type of the password field between 'text' and 'password', allowing the user to see what they typed.
  const [showPassword, setShowPassword] = useState(false);

  // Authentication context provides global login functionality and current status
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // --- Authentication Flow / Protected Routes ---
  // If the user is already authenticated (token exists and is valid), 
  // bypass the login page and automatically redirect them to the protected Dashboard.
  if (isAuthenticated()) {
    navigate('/dashboard', { replace: true });
    return null;
  }

  /**
   * handleSubmit
   * 
   * Event Handler for the login form submission.
   * User Action: Clicks the "Sign in" button or presses Enter inside the form.
   * Business Process:
   * 1. Prevent default form submission reload.
   * 2. Validate input fields to prevent unnecessary API calls.
   * 3. Send credentials to the backend.
   * 4. Handle token storage and redirection on success.
   */
  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevents the browser from refreshing the page on form submit
    setError(''); // Clear any previous errors

    // Form Validation: Ensure both fields have content before making the request
    if (!username.trim() || !password.trim()) {
      setError('Please fill in all fields');
      return;
    }

    setLoading(true); // Trigger UI loading state
    try {
      // --- API Call ---
      // Backend Service: Authentication Controller
      // Purpose: Verifies the provided credentials against the database.
      // Expected Data: An object containing 'username' and 'password'.
      const response = await api.post('/api/auth/login', {
        username: username.trim(),
        password,
      });

      const data = response.data;
      // Depending on the backend configuration, the token could be named differently.
      // We check a few common names to be robust.
      const token = data.token || data.jwt || data.accessToken;
      const role = data.role || 'STUDENT';
      const user = data.username || username.trim();

      // Success Handling
      if (token) {
        // --- Token Storage ---
        // The `login` function from AuthContext saves the token in localStorage or cookies,
        // and updates the global application state so other components know the user is logged in.
        login(token, user, role);
        // Redirect to the dashboard, replacing the history entry so they don't hit "Back" to return to login.
        navigate('/dashboard', { replace: true });
      } else {
        setError('Invalid response from server');
      }
    } catch (err) {
      // --- API Failure Handling ---
      // Provide actionable feedback to the user based on the error type
      if (err.response) {
        // The request was made and the server responded with a status code outside of the 2xx range (e.g., 401 Unauthorized)
        const msg = err.response.data?.message || err.response.data?.error;
        setError(msg || `Login failed (${err.response.status})`);
      } else if (err.request) {
        // The request was made but no response was received (e.g., network error or server down)
        setError('Unable to reach server. Please check your connection.');
      } else {
        // Something happened in setting up the request that triggered an Error
        setError('An unexpected error occurred');
      }
    } finally {
      // Always remove the loading state, regardless of success or failure
      setLoading(false);
    }
  };

  // --- Form & UI Render ---
  return (
    <div className="auth-page">
      <div className="auth-brand-panel">
        <div className="auth-brand-decor">
          <span className="decor-ring decor-ring-1" />
          <span className="decor-ring decor-ring-2" />
        </div>
        <div className="auth-brand-content">
          <svg viewBox="0 0 40 40" className="auth-brand-mark" aria-hidden="true">
            <rect width="40" height="40" rx="10" fill="#F6F1FB" />
            <path d="M11 10h17v20H11z" fill="#241B36" />
            <path d="M28 10v6.2L21.8 10z" fill="#5B4488" />
            <rect x="14.5" y="19" width="11" height="2.3" rx="1.1" fill="#F6F1FB" />
            <rect x="14.5" y="23.6" width="11" height="2.3" rx="1.1" fill="#F6F1FB" />
            <circle cx="28.5" cy="28.5" r="6" fill="#2F8B5F" />
            <path d="M25.7 28.6l1.9 1.9 3.8-3.8" stroke="#F6F1FB" strokeWidth="1.6" fill="none" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
          <h2>Stationery Management System</h2>
          <p>One hub for browsing supplies, submitting requests, and keeping campus inventory accurate.</p>

          <ul className="auth-feature-list">
            <li><PackageCheck size={18} /> Real-time inventory with low-stock alerts</li>
            <li><ClipboardCheck size={18} /> Request approvals tracked end to end</li>
            <li><ShieldCheck size={18} /> Role-based access for admins and students</li>
          </ul>
        </div>
      </div>

      <div className="auth-form-panel">
        <div className="auth-card">
          <div className="auth-header">
            <h1>Welcome back</h1>
            <p>Sign in to your account to continue.</p>
          </div>

          {/* Conditional rendering of the error message banner */}
          {error && (
            <div className="auth-error">
              <AlertCircle size={17} />
              <span>{error}</span>
            </div>
          )}

          {/* Login Form: Triggers handleSubmit when submitted */}
          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username</label>
              <div className="input-icon-wrap">
                <User size={17} />
                <input
                  id="username"
                  type="text"
                  className="form-input"
                  placeholder="Enter your username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  disabled={loading}
                  autoComplete="username"
                  autoFocus
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="password">Password</label>
              <div className="input-icon-wrap">
                <Lock size={17} />
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  className="form-input"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={loading}
                  autoComplete="current-password"
                />
                {/* Event Handler: Toggles password visibility by changing the input type */}
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  tabIndex={-1}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
              {loading ? (
                <span className="btn-spinner" />
              ) : (
                <>
                  <span>Sign in</span>
                  <ArrowRight size={16} />
                </>
              )}
            </button>
          </form>

          <div className="auth-footer">
            <p>
              Don't have an account?{' '}
              <Link to="/register" className="auth-link">Create one</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
