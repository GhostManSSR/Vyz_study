import styled from "../assets/style/BookCard.module.css"


function Button({...props}){
    return(
       <button disabled={props.disabled} style={{display:"flex",cursor:"pointer",borderRadius:"20px", justifyContent:"center", alignItems:"center", ...props.style}} className={!props.className ? styled.btn : props.className} onClick={props.onClick}>{props.children}</button>
    )
}

export default Button;