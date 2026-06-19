import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from './LoadingSpinner';

// ============================================================================
// PROTECTED ROUTE COMPONENT
// Purpose: This is a wrapper component used in App.js to secure specific pages.
// It acts as a "bouncer" that checks if the user is allowed to enter a page.
// 
// How it fits into the workflow:
// Instead of adding logic to EVERY page to redirect unauthorized users, we
// wrap the page component in <ProtectedRoute>. If the user passes the checks,
// the wrapper renders the page (children). If not, it redirects them away.
// ============================================================================

const ProtectedRoute = ({ children, requiredRole }) => {
  // Grab the current authentication state from our global AuthContext
  const { user, loading, isAuthenticated } = useAuth();

  // CHECK 1: Wait for initial load
  // If the app just booted up, we might still be reading the login token
  // from localStorage. We show a spinner so we don't accidentally redirect
  // a logged-in user to the login screen before we finish checking.
  if (loading) {
    return <LoadingSpinner />;
  }

  // CHECK 2: Is the user logged in at all?
  // If there is no valid token, they are not authenticated.
  // We use the <Navigate> component to instantly redirect them to /login.
  // The 'replace' prop ensures this redirect replaces the current history entry,
  // preventing the user from clicking the back button to return to the blocked page.
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  // CHECK 3: Does the user have the right role?
  // Some pages (like AddItem) are strictly for Admins. We pass a 'requiredRole'
  // prop to enforce this. If a Student tries to visit an Admin page, they
  // are safely redirected back to their dashboard.
  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/dashboard" replace />;
  }

  // SUCCESS: All checks passed!
  // Render the wrapped component (e.g., <Dashboard />, <ManageRequests />).
  return children;
};

export default ProtectedRoute;
