import styled from "../assets/style/CardPerson/CardPerson.module.css"

const CardPerson = ({...props}) => {

    return(
        <>  
            <div className={styled.cardperson__block}>
                <span className={styled.cardperson__item}>{props.id}</span>
                <span className={styled.cardperson__item}>{props.userId}</span>
                <span className={styled.cardperson__item}>{props.title}</span>
                <span className={styled.cardperson__item}>{props.body}</span>
            </div>
        </>
    )
}

export default CardPerson;