const Button = ({...props}) => {
    const {
        className,
        children,
    } = props
    
    return(
        <button className={className}>{children}</button>
    )
}

export default Button;