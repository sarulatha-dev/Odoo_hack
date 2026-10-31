// Quotation Service – thin wrapper around the shared Axios client.
// Mirrors the pattern used by other domain services (products, plans, etc.).
// All functions return a Promise that resolves to the Axios response data.

import apiClient from './apiClient.js';

// Base endpoint – the final Spring Boot backend will expose this URL.
// For now it is a placeholder; the path matches the admin route naming.
const BASE = '/api/quotations';

export const fetchQuotations = (params = {}) => {
  // params can include search, status, date range, pagination, etc.
  return apiClient.get(BASE, { params }).then((res) => res.data);
};

export const fetchQuotation = (id) => {
  return apiClient.get(`${BASE}/${id}`).then((res) => res.data);
};

export const createQuotation = (payload) => {
  return apiClient.post(BASE, payload).then((res) => res.data);
};

export const updateQuotation = (id, payload) => {
  return apiClient.put(`${BASE}/${id}`, payload).then((res) => res.data);
};

export const deleteQuotation = (id) => {
  return apiClient.delete(`${BASE}/${id}`).then((res) => res.data);
};

export const changeStatus = (id, status) => {
  // Expected status values: 'draft', 'sent', 'accepted', 'rejected'
  return apiClient
    .patch(`${BASE}/${id}/status`, { status })
    .then((res) => res.data);
};

export const sendQuotation = (id) => changeStatus(id, 'sent');
export const acceptQuotation = (id) => changeStatus(id, 'accepted');
export const rejectQuotation = (id) => changeStatus(id, 'rejected');
