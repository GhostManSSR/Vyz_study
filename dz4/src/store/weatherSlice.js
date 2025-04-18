import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getWeather } from '../utils/api';

export const fetchWeather = createAsyncThunk(
  'weather/fetchWeather',
  async ({ lat, lon }) => {
    const data = await getWeather(lat, lon);
    return data;
  }
);

const weatherSlice = createSlice({
  name: 'weather',
  initialState: { data: null, loading: false },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchWeather.pending, (state) => { state.loading = true; })
      .addCase(fetchWeather.fulfilled, (state, action) => {
        state.loading = false;
        state.data = action.payload;
      });
  },
});

export default weatherSlice.reducer;