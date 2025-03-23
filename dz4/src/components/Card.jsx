import styled from "../assets/style/BookCard.module.css"

function Card({...props}){

    return(
        <div className={styled.card}>
            <span className={styled.line_id}><p><b>id:</b> {props.id}</p></span>
            <span><b>FullName: </b>{props.fullName}</span>
            <span>
                <p><b>Name: </b></p> 
                    <p><span className={styled.marign_addres}><b>First Name: </b></span>{props.firstName}</p>
                    <p><span className={styled.marign_addres}><b>First Name: </b></span>{props.firstName}</p>
            </span>
            <span>
                <p><b>Address: </b></p> 
                    <p><span className={styled.marign_addres}><b>Line: </b></span>{props.addresLine}</p>
                    <p><span className={styled.marign_addres}><b>Town: </b></span>{props.addresTown}</p>
                    <p><span className={styled.marign_addres}><b>Country: </b></span>{props.addresCounty}</p>
                    <p><span className={styled.marign_addres}><b>Country: </b></span>{props.addresCountry}</p>
            </span>
            <span className={styled.line_id}><p><b>email:</b> {props.email}</p></span>
        </div>
    )
}

export default Card;