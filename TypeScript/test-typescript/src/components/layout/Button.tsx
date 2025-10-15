

type props = {children?: string, onclick?: () => void, disabled?: boolean};

const Button: React.FunctionComponent<props> = (props) => {

    return (
        <button onClick={props.onclick} disabled={props.disabled}>{props.children}</button>
    )
}

export default Button;