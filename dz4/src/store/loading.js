// import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
// import {getResuerse} from "../utils/api"


// const ResurseSlice = createSlice({
//     name: 'resurse',
//     initialState: { data: null, loading: false },
//     reducers: {},
//     extraReducers: (builder) => {
//       builder
//         .addCase(getResuerse.pending, (state) => { state.loading = true; })
//         .addCase(getResuerse.fulfilled, (state, action) => {
//           state.loading = false;
//           state.data = action.payload;
//         });
//     },
//   });

// export default ResurseSlice.reducer;