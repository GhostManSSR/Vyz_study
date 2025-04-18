import { useEffect, useState } from "react";
import Button from "../../Button";
import styles from "../../../assets/style/layout/ProgressBar/ProgressBar.module.css";

const ProgressBar = ({ title, loading, setloading }) => {
    const [progress, setProgress] = useState(0);
    const [loadings, setLoading] = useState(loading);

    useEffect(() => {
        if (loadings) {
            const interval = setInterval(() => {
                setProgress(prev => {
                    if (prev >= 100) {
                        setloading(false);
                        clearInterval(interval);
                        return 100;
                    }
                    return prev + 1;
                });
            }, 100);

            return () => clearInterval(interval);
        }
    }, [loadings, progress]);

    const toggleLoading = () => {
        if (loadings) {
            setLoading(false); 
        } else {
            setProgress(0); 
            setLoading(true); 
        }
    };

    return (
        <div className={styles.bar__main}>
            <span>{title}</span>
            <div className={styles.bar__block}>
                <div className={styles.bar_box} style={{
                    width: "100%",
                    height: "45px",
                    borderRadius: "20px",
                    border: "2px solid #000",
                    background: `linear-gradient(to right, green ${progress}%, #ddd ${progress}%)`,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontWeight: "bold"
                }}>
                    <span>{progress}%</span>
                </div>
                <Button className={styles.bar__btn} onClick={toggleLoading}>
                    {loadings ? "Stop" : "Start"}
                </Button>
            </div>
        </div>
    );
};

export default ProgressBar;
