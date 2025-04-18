import styles from "../assets/style/layout/Placement/Placement.module.css"

const Placenemt = ({ data, city }) => {
    const cityName = data?.city?.name ?? city;
    const temp = data?.list?.[0]?.main?.temp;

    if (!data || !temp) {
        return <p>Нет данных о погоде</p>;
    }

    return (
        <div className={styles.placement__block}>
            <div className={styles.placement__city}>
                <span>{cityName}</span>
                <span style={{ marginLeft: "55px", marginTop: "55px", fontSize: "36px" }}>
                    {Math.round(temp)}°C
                </span>
            </div>
            <div className={styles.placement__icons}>
                <img
                    src={`https://openweathermap.org/img/wn/${data.list[0].weather[0].icon}.png`}
                    alt="weather icon"
                />
            </div>
        </div>
    );
};

export default Placenemt;
