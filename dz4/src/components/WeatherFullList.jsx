import styles from "../assets/style/layout/WeatherFullList/WeatherFullList.module.css";

const WeatherFullList = ({ data }) => {
    const groupByDate = (list) => {
        if (!Array.isArray(list) || list.length === 0) {
            return []; 
        }
        return list.reduce((acc, item) => {
            const date = new Date(item.dt * 1000).toLocaleDateString("en-US", {
                weekday: "long",
                day: "numeric",
            });

            if (!acc[date]) {
                acc[date] = {
                    date,
                    tempMin: item.main.temp_min,
                    tempMax: item.main.temp_max,
                    icon: item.weather[0].icon,
                };
            } else {
                acc[date].tempMin = Math.min(acc[date].tempMin, item.main.temp_min);
                acc[date].tempMax = Math.max(acc[date].tempMax, item.main.temp_max);
            }

            return acc;
        }, {});
    };

    const groupedData = groupByDate(data.list);

    if (!data || !data.list || data.list.length === 0) {
        return <p></p>;
    }

    const ItemList = ({ date, tempFirst, icon, tempLast }) => (
        <div className={styles.list_item}>
            <span>{date}</span>
            <div>
                <img src={`http://openweathermap.org/img/wn/${icon}.png`} alt="weather icon" />
                <div className={styles.temp__block}>
                    <span>{Math.round(tempFirst)}°C</span> /
                    <span style={{ color: "#999" }}>{Math.round(tempLast)}°C</span>
                </div>
            </div>
        </div>
    );

    return (
        <div className={styles.list_items__block}>
            {Object.values(groupedData).map((item) => (
                <ItemList
                    key={item.date}
                    sp={item.sp}
                    date={item.date}
                    tempFirst={item.tempMin}
                    icon={item.icon}
                    tempLast={item.tempMax}
                />
            ))}
        </div>
    );
};

export default WeatherFullList;
