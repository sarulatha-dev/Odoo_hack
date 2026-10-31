// src/context/AuthContext.jsx
import React, { createContext, useContext } from 'react';

// Simple demo auth context – returns a fixed admin role.
const AuthContext = createContext({ user: { role: 'admin' } });

export const AuthProvider = ({ children }) => (
  <AuthContext.Provider value={{ user: { role: 'admin' } }}>{children}</AuthContext.Provider>
);

export const useAuth = () => useContext(AuthContext);
