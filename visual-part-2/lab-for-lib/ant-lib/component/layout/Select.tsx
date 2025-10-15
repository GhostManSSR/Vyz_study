import { Select as SelectAnt } from "antd";
import {useEffect, useState} from "react";
import {RiArrowDownSLine} from "react-icons/ri";

type props = {children?: string, mode?:string, placeholder?: string, onChange?: () => void, defaultValue?: string, options: [], style: object, className?: string,};

const Select: React.FunctionComponent<props> = (props) => {
    const [options, setOptions] = useState<props[]>([]);

    useEffect(() => {
        setOptions(props.options)
    }, [props.options])

    return (
        <SelectAnt
            options={options}
            placeholder={props.placeholder}
            mode={props.mode}
            suffixIcon={<RiArrowDownSLine />}
            className={`select-wrap ${props.className || ''}`}
            style={{ ...props.style}}
            onChange={props.onChange}
        />
    )
}

export default Select;