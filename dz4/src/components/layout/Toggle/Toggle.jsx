import styles from "../../../assets/style/layout/Toggle/Toggle.module.css"

const Toggle = ({...props}) => {
    const {
        menuLink,
        menuStatus
    } = props
    
    
    return(
        <div style={styles.toogle_box}>
            {menuStatus.map((item => (
                menuLink.map((items, index) => (
                    <a href={items} key={index} className={styles.toogle__items}>{item}</a>
                ))
            )))}
        </div>
    )
}

export default Toggle;