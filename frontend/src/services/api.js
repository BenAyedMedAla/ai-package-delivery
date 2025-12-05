import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export const generateGrid = async (gridRequest) => {
  const response = await axios.post(`${API_BASE_URL}/generate-grid`, gridRequest);
  return response.data;
};

export const chooseStrategy = async (strategyRequest) => {
  const response = await axios.post(`${API_BASE_URL}/choose-strategy`, strategyRequest);
  return response.data;
};