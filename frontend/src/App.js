import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Providers and Wrappers
import { AuthProvider } from './context/AuthContext';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';

// Public Pages
import Login from './pages/Login';
import Register from './pages/Register';

// Protected Pages (Shared, Admin, or Student)
import Dashboard from './pages/Dashboard';
import Inventory from './pages/Inventory';
import AddItem from './pages/AddItem';
import EditItem from './pages/EditItem';
import CreateRequest from './pages/CreateRequest';
import MyRequests from './pages/MyRequests';
import ManageRequests from './pages/ManageRequests';

import './App.css';

// ============================================================================
// MAIN APP COMPONENT (Router)
// Purpose: This is the root component of the React application. It defines
// the "Map" of the entire website. It tells the browser which Component to
// load based on the URL in the address bar.
//
// Architecture / Layout:
// - `AuthProvider`: Wraps the whole app so that every page knows who is logged in.
// - `Router`: Keeps track of browser history and the current URL.
// - `ProtectedRoute`: A bouncer that kicks out unauthenticated users or those
//   with the wrong role.
// - `Layout`: A wrapper that provides the sidebar and header around the main content.
// ============================================================================

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="app">
          <Routes>
            {/* =========================================
                PUBLIC ROUTES
                Anyone can visit these pages. Usually, this is
                just for getting people into the app.
                ========================================= */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* =========================================
                SHARED PROTECTED ROUTES
                Both Students and Admins can view these, but
                they must be logged in. The pages themselves
                change their content based on the user's role.
                ========================================= */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Layout title="Dashboard">
                    <Dashboard />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/inventory"
              element={
                <ProtectedRoute>
                  <Layout title="Inventory">
                    <Inventory />
                  </Layout>
                </ProtectedRoute>
              }
            />

            {/* =========================================
                ADMIN ONLY ROUTES
                Only users with the ADMIN role can access these.
                Students trying to visit these URLs will be redirected.
                ========================================= */}
            <Route
              path="/inventory/add"
              element={
                <ProtectedRoute requiredRole="ADMIN">
                  <Layout title="Add Inventory Item">
                    <AddItem />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/inventory/edit/:id"
              element={
                <ProtectedRoute requiredRole="ADMIN">
                  <Layout title="Edit Inventory Item">
                    <EditItem />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/requests/manage"
              element={
                <ProtectedRoute requiredRole="ADMIN">
                  <Layout title="Manage Requests">
                    <ManageRequests />
                  </Layout>
                </ProtectedRoute>
              }
            />

            {/* =========================================
                STUDENT ONLY ROUTES
                Only users with the STUDENT role can access these.
                Admins don't need to create requests.
                ========================================= */}
            <Route
              path="/requests/new"
              element={
                <ProtectedRoute requiredRole="STUDENT">
                  <Layout title="New Request">
                    <CreateRequest />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/requests/my"
              element={
                <ProtectedRoute requiredRole="STUDENT">
                  <Layout title="My Requests">
                    <MyRequests />
                  </Layout>
                </ProtectedRoute>
              }
            />

            {/* =========================================
                FALLBACK ROUTES
                If the user types a random URL (e.g., /blabla)
                or just visits the root (localhost:3000/),
                we automatically redirect them to the dashboard.
                ========================================= */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
