

function Input({...props}){
    return(
        <input placeholder={props.placeholder} className={props.className} onChange={props.onChange}></input>
    )
}


export default Input;