import styled from "../assets/style/BookCard.module.css"


function Button({...props}){
    return(
       <button className={styled.btn} onClick={props.onClick}>{props.children}</button>
    )
}

export default Button;