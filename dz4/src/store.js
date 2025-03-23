// import { createStore } from 'react-redux'


// const initialState = {
//   items: [],
//   total: 0,
//   item: {},
//   show: false
// }

// function books(state = [], action) => {
//   switch (action.type) {
//     case 'ADD_TODO':
//               ...state,
//               items: [
//                   ...state.items,
//                   ...action.payload.items
//               ],
//               total: action.payload.total
//           }
//         }
//       }


// const store = createStore()

// function todos(state = [], action) {
//   switch (action.type) {
//     case 'ADD_TODO':
//       return state.concat([action.text])
//     default:
//       return state
//   }
// }

// const store = createStore(todos, ['Use Redux'])

// store.dispatch({
//   type: 'ADD_TODO',
//   text: 'Read the docs'
// })

// console.log(store.getState())