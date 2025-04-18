import { configureStore } from '@reduxjs/toolkit';
import weatherReducer from './weatherSlice';
import ResurseSlice from "./loading";

export const store = configureStore({ reducer: { weather: weatherReducer} });