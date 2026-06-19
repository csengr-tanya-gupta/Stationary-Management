/**
 * Register.js
 * 
 * Component Purpose: 
 * Handles new user account creation for the system.
 * 
 * Target User: 
 * Unregistered users who want to create a Student or Admin account.
 * 
 * General Workflow:
 * 1. User fills out username, email, password, confirm password, and selects a role.
 * 2. Form validation ensures format correctness and password matching.
 * 3. Data is sent to the backend registration API.
 * 4. On success, a confirmation screen appears, then redirects to the login page.
 */
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, AlertCircle, ArrowRight, CheckCircle2, GraduationCap, ShieldCheck } from 'lucide-react';
import api from '../api/axiosConfig';
import './Auth.css';

const Register = () => {
  // --- State Variables ---
  // `formData`: Centralized state object holding all user inputs for the form.
  // Grouping these into an object makes updates easier and keeps state clean.
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'STUDENT',
  });
  
  // `errors`: Stores specific validation errors mapped to field names (e.g., { username: 'Too short' }).
  // Controls the appearance of inline error messages and red borders on inputs.
  const [errors, setErrors] = useState({});
  
  // `serverError`: Stores global error messages returned by the API (e.g., "Username already exists").
  const [serverError, setServerError] = useState('');
  
  // `loading`: True when waiting for the registration API. Used to disable the form.
  const [loading, setLoading] = useState(false);
  
  // `success`: Tracks if registration completed successfully. If true, the UI swaps to a success view.
  const [success, setSuccess] = useState(false);
  
  const navigate = useNavigate();

  /**
   * validate
   * 
   * Form Validation Logic:
   * Checks the current `formData` against business rules before attempting an API call.
   * - Username: Required, >= 3 chars.
   * - Email: Required, must match basic email regex.
   * - Password: Required, >= 6 chars.
   * - Confirm Password: Must exactly match Password.
   * 
   * Returns true if valid, false if any errors are found.
   */
  const validate = () => {
    const newErrors = {};

    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
    } else if (formData.username.trim().length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    // Object.keys(newErrors).length === 0 implies no errors were found
    return Object.keys(newErrors).length === 0;
  };

  /**
   * handleChange
   * 
   * Event Handler for input fields typing.
   * Dynamically updates the `formData` object based on the input's `name` attribute.
   * Also clears any specific validation error for that field as the user begins typing to fix it.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  /**
   * selectRole
   * 
   * Event Handler for custom role selection buttons.
   * Sets the role field in the `formData` state object.
   */
  const selectRole = (role) => {
    setFormData((prev) => ({ ...prev, role }));
  };

  /**
   * handleSubmit
   * 
   * Event Handler for form submission.
   * Business Process: Runs validation, then makes the API call to create the user account.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');

    // Abort if form validation fails
    if (!validate()) return;

    setLoading(true);
    try {
      // --- API Call ---
      // Endpoint: POST /api/auth/register
      // Purpose: Creates a new user record in the backend database.
      // Expected Data: Username, email, password, and requested role.
      await api.post('/api/auth/register', {
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password,
        role: formData.role,
      });

      // --- Success Handling ---
      setSuccess(true); // Triggers conditional render of success UI
      
      // Wait for 2 seconds so the user can read the success message, then navigate to login
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      // --- API Failure Handling ---
      // Extracts backend error (e.g. 409 Conflict if email already taken)
      if (err.response) {
        const msg = err.response.data?.message || err.response.data?.error;
        setServerError(msg || `Registration failed (${err.response.status})`);
      } else if (err.request) {
        setServerError('Unable to reach server. Please check your connection.');
      } else {
        setServerError('An unexpected error occurred');
      }
    } finally {
      setLoading(false);
    }
  };

  // Reusable sub-component for the left-side branding panel
  const BrandPanel = () => (
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
        <h2>Join the supply hub</h2>
        <p>Create an account to browse stationery, submit requests, or manage inventory for your campus.</p>
      </div>
    </div>
  );

  // --- UI Render ---
  // If registration is successful, render a dedicated success screen instead of the form.
  if (success) {
    return (
      <div className="auth-page">
        <BrandPanel />
        <div className="auth-form-panel">
          <div className="auth-card auth-success">
            <div className="auth-success-icon">
              <CheckCircle2 size={28} />
            </div>
            <h2>Account created</h2>
            <p>Redirecting you to the sign-in page...</p>
          </div>
        </div>
      </div>
    );
  }

  // Primary rendering for the Registration form
  return (
    <div className="auth-page">
      <BrandPanel />

      <div className="auth-form-panel">
        <div className="auth-card">
          <div className="auth-header">
            <h1>Create your account</h1>
            <p>Join the Stationery Management System.</p>
          </div>

          {/* Displays API/Server errors globally */}
          {serverError && (
            <div className="auth-error">
              <AlertCircle size={17} />
              <span>{serverError}</span>
            </div>
          )}

          {/* Registration Form */}
          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="reg-username">Username</label>
              <div className="input-icon-wrap">
                <User size={17} />
                <input
                  id="reg-username"
                  type="text"
                  name="username"
                  className={`form-input ${errors.username ? 'input-error' : ''}`}
                  placeholder="Choose a username"
                  value={formData.username}
                  onChange={handleChange}
                  disabled={loading}
                  autoFocus
                />
              </div>
              {/* Field-level error validation message */}
              {errors.username && <span className="field-error">{errors.username}</span>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="reg-email">Email</label>
              <div className="input-icon-wrap">
                <Mail size={17} />
                <input
                  id="reg-email"
                  type="email"
                  name="email"
                  className={`form-input ${errors.email ? 'input-error' : ''}`}
                  placeholder="Enter your email"
                  value={formData.email}
                  onChange={handleChange}
                  disabled={loading}
                />
              </div>
              {errors.email && <span className="field-error">{errors.email}</span>}
            </div>

            <div className="form-row">
              <div className="form-group">
                <label className="form-label" htmlFor="reg-password">Password</label>
                <div className="input-icon-wrap">
                  <Lock size={17} />
                  <input
                     id="reg-password"
                    type="password"
                    name="password"
                    className={`form-input ${errors.password ? 'input-error' : ''}`}
                    placeholder="Create a password"
                    value={formData.password}
                    onChange={handleChange}
                    disabled={loading}
                  />
                </div>
                {errors.password && <span className="field-error">{errors.password}</span>}
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="reg-confirm">Confirm password</label>
                <div className="input-icon-wrap">
                  <Lock size={17} />
                  <input
                    id="reg-confirm"
                    type="password"
                    name="confirmPassword"
                    className={`form-input ${errors.confirmPassword ? 'input-error' : ''}`}
                    placeholder="Confirm password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    disabled={loading}
                  />
                </div>
                {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">I am registering as</label>
              <div className="role-select-group">
                {/* Custom radio-style buttons for Role selection */}
                <div
                  className={`role-option ${formData.role === 'STUDENT' ? 'selected' : ''}`}
                  onClick={() => !loading && selectRole('STUDENT')}
                >
                  <GraduationCap size={18} />
                  <span>Student</span>
                </div>
                <div
                  className={`role-option ${formData.role === 'ADMIN' ? 'selected' : ''}`}
                  onClick={() => !loading && selectRole('ADMIN')}
                >
                  <ShieldCheck size={18} />
                  <span>Admin</span>
                </div>
              </div>
            </div>

            <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
              {loading ? (
                <span className="btn-spinner" />
              ) : (
                <>
                  <span>Create account</span>
                  <ArrowRight size={16} />
                </>
              )}
            </button>
          </form>

          <div className="auth-footer">
            <p>
              Already have an account?{' '}
              <Link to="/login" className="auth-link">Sign in</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
