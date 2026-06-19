import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';

// ============================================================================
// AUTHENTICATION CONTEXT
// Purpose: This file manages the global authentication state for the entire app.
// It stores the logged-in user's details, handles logging in and out, and
// provides helper functions to check roles (Admin vs. Student).
//
// Authentication Flow:
// 1. User logs in via the Login page.
// 2. The backend sends back a JWT token and user details (username, role).
// 3. We call the `login` function here to store that info in `localStorage`
//    (so they stay logged in if they refresh) and in the `user` state variable.
// 4. Any component can use `useAuth()` to get the current user or check roles.
// ============================================================================

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  // State: `user`
  // Purpose: Holds the current user's token, username, and role.
  // Impact: If null, the user is not logged in and will be redirected to Login.
  const [user, setUser] = useState(null);

  // State: `loading`
  // Purpose: Keeps track of whether we are currently checking for a saved login.
  // Impact: Prevents the app from flashing the login screen before we finish
  // reading the token from localStorage.
  const [loading, setLoading] = useState(true);

  // useEffect Hook: Restore Session on Mount
  // Purpose: When the app first loads (or refreshes), we check if the user
  // already logged in previously by looking in the browser's localStorage.
  // If removed: The user would be forced to log in again every single time
  // they refresh the page.
  useEffect(() => {
    try {
      const token = localStorage.getItem('sms_token');
      const username = localStorage.getItem('sms_user');
      const role = localStorage.getItem('sms_role');

      // If we found all pieces of auth data, restore the user session
      if (token && username && role) {
        setUser({ token, username, role });
      }
    } catch (err) {
      console.error('Error restoring auth state:', err);
      // If something goes wrong (e.g. corrupt data), clear it out to be safe
      localStorage.removeItem('sms_token');
      localStorage.removeItem('sms_user');
      localStorage.removeItem('sms_role');
    } finally {
      // We are done checking, so tell the app to stop loading and render
      setLoading(false);
    }
  }, []);

  // Event Handler: login
  // Triggered by: The user successfully authenticating on the Login page.
  // Business Process: Saves the JWT token and user info into localStorage
  // for persistence, and updates the `user` state so the UI reacts instantly.
  const login = useCallback((token, username, role) => {
    // Standardize role strings from backend (e.g., "ROLE_ADMIN" -> "ADMIN")
    const normalizedRole = role ? role.toUpperCase().replace('ROLE_', '') : 'STUDENT';
    
    localStorage.setItem('sms_token', token);
    localStorage.setItem('sms_user', username);
    localStorage.setItem('sms_role', normalizedRole);
    
    setUser({ token, username, role: normalizedRole });
  }, []);

  // Event Handler: logout
  // Triggered by: The user clicking the "Log out" button in the sidebar.
  // Business Process: Clears the local session data and resets the `user` state.
  // This causes protected routes to automatically kick the user back to the login screen.
  const logout = useCallback(() => {
    localStorage.removeItem('sms_token');
    localStorage.removeItem('sms_user');
    localStorage.removeItem('sms_role');
    setUser(null);
  }, []);

  // Helper: isAdmin
  // Purpose: Checks if the current user has the ADMIN role.
  // Usage: Used to hide/show UI elements (like "Manage Requests" or "Add Item").
  const isAdmin = useCallback(() => {
    return user?.role === 'ADMIN';
  }, [user]);

  // Helper: isStudent
  // Purpose: Checks if the current user has the STUDENT role.
  const isStudent = useCallback(() => {
    return user?.role === 'STUDENT';
  }, [user]);

  // Helper: isAuthenticated
  // Purpose: Checks if anyone is logged in at all (requires a token).
  const isAuthenticated = useCallback(() => {
    return !!user?.token;
  }, [user]);

  // The 'value' object contains everything we want to make available to the rest of the app
  const value = {
    user,
    loading,
    login,
    logout,
    isAdmin,
    isStudent,
    isAuthenticated,
  };

  // Provide this value to all child components (which will be the whole app)
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Custom Hook: useAuth
// Purpose: A simple way for any component to grab the AuthContext.
// Example: const { user, logout } = useAuth();
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
