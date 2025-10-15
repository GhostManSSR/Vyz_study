import "../../assets/less/layout/Toggle.less";

interface ToggleItem {
    name: string;
    onClick: () => void;
}

interface Props {
    toggleList?: ToggleItem[];
}

const Toggle: React.FC<Props> = ({ toggleList = [] }) => {
    return (
        <>
            {toggleList.map((item, index) => (
                <button
                    key={index}
                    onClick={item.onClick}
                    className="toggle__item"
                >
                    {item.name}
                </button>
            ))}
        </>
    );
};

export default Toggle;
