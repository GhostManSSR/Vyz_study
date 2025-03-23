
// import * as types from './actionTypes';
// import { route } from '../route';
// const fetch = require("node-fetch");

export const fetchBooks = () =>{
    return fetch(" https://fakeapi.extendsclass.com/books")
        .then(res => {
            
            console.log(res)
            res.json()
            return {res}
            }
        )
        // .then(
        //     (result) => {
        //         dispatch({type: types.SCHOOLS_FETCHED, payload: result})
        //         return {result}
        //     }
        // )
}