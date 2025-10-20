import styles from '../../assets/less/layout/Toggle.module.less';

interface ToggleItem {
    name: string;
    onClick: () => void;
}

interface Props {
    toggleList?: ToggleItem[];
}

const Toggle: React.FC<Props> = ({ toggleList = [] }) => {
    return (
        <div style={{marginTop: "15px", marginLeft:"15px", display:"flex", gap:"10px"}}>
            {toggleList.map((item, index) => (
                <button
                    key={index}
                    onClick={item.onClick}
                    className={styles.toggle__item}
                >
                    {item.name}
                </button>
            ))}
        </div>
    );
};

export default Toggle;
