
type props = {children?: string, onChange?: () => void, defaultValue?: string, value: Array<object>};

const Select: React.FunctionComponent<props> = (props) => {

    return (
        <select onChange={props.onChange} defaultValue={props.defaultValue}>
            {props.value.map((item) => {
                let valueCurrentElement = Object.values(item)
                return <option value={valueCurrentElement}>{valueCurrentElement}</option>;
            })}
        </select>
    )
}

export default Select;