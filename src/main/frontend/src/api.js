import axios from 'axios';

const API_URL = '/api';

export const getAllBicycles = () => axios.get(`${API_URL}/bicycles`);
export const createBicycle = (bicycleData) => axios.post(`${API_URL}/bicycles`, bicycleData);
export const updateBicycle = (id, bicycleData) => axios.put(`${API_URL}/bicycles/${id}`, bicycleData);
export const deleteBicycle = (id) => axios.delete(`${API_URL}/bicycles/${id}`);

export const getAllUsers = () => axios.get(`${API_URL}/users`);
export const createUser = (userData) => axios.post(`${API_URL}/users`, userData);
export const updateUser = (id, userData) => axios.put(`${API_URL}/users/${id}`, userData);
export const deleteUser = (id) => axios.delete(`${API_URL}/users/${id}`);

export const getAllRentals = () => axios.get(`${API_URL}/bicycles/rentals`);
export const getRentalsForUser = (userId) => axios.get(`${API_URL}/bicycles/rentals/user/${userId}`);
export const getRentalsForBicycle = (bicycleId) => axios.get(`${API_URL}/bicycles/rentals/bicycle/${bicycleId}`);
export const rentBicycle = (userId, bicycleId) => axios.post(`${API_URL}/bicycles/${bicycleId}/rent/${userId}`);
export const returnBicycle = (userId, bicycleId) => axios.post(`${API_URL}/bicycles/${bicycleId}/return/${userId}`);