import ButtonAnt from 'antd/es/button';

interface Props {
    children?: string;
    onClick?: () => void;
    disabled?: boolean;
    htmlType?: 'button' | 'submit' | 'reset';
}

const Button: React.FunctionComponent<Props> = (props) => {
    return (
        <ButtonAnt onClick={props.onClick} disabled={props.disabled} htmlType={props.htmlType}>
            {props.children}
        </ButtonAnt>
    );
};

export default Button;
