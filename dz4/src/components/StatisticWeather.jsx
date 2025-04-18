import styles from "../assets/style/layout/StatisticWeather/StatisticWeather.module.css"

const StatisticWeather = ({...props}) => {
    const {
        data
    } = props
    
    const ColumnBlock = ({...props}) => {
        const {
            name,
            value
        } = props
        return (
            <div className={styles.item__column}>
                <span>{name}</span>
                <span>{value}</span>
            </div>
        )
    }

    if (!data || !data.list || data.list.length === 0) {
        return <p></p>;
    }

    return(
        <div className={styles.block_stats}>
            <ColumnBlock name={"Humidity"} value={data.list[0].main.humidity}/>
            <ColumnBlock name={"Wind"} value={data.list[0].wind.speed}/>
            <ColumnBlock name={"Air pressure"} value={data.list[0].main.pressure}/>
            <ColumnBlock name={"Feels like"} value={data.list[0].main.feels_like}/>
        </div>
    )
}

export default StatisticWeather;