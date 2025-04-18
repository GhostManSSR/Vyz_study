

const ItemsPet = ({...props}) => {

    const {
        id,
        age,
        email,
        pet
    } = props

    return(
        <div>
            <span>{id}</span>
        </div>
    )
}

export default ItemsPet