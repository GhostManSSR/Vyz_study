import styles from "../assets/style/layout/CardWeather/CardWeather.module.css"


const WeatherCard = ({ weather }) => {
  const dateTime = new Date(weather.dt_txt); 
  const hours = dateTime.getHours().toString().padStart(2, '0');  
  const minutes = dateTime.getMinutes().toString().padStart(2, '0');  
  const formattedTime = `${hours}:${minutes}`; 

  return (
    <div className={styles.card_weather__block}>
      <h3 className={styles.card_weather__text}>{formattedTime}</h3> 
      <p>{weather.main.temp}°C</p>
      <img 
        className={styles.card_weather__img} 
        src={`https://openweathermap.org/img/wn/${weather.weather[0].icon}.png`} 
        alt={weather.weather[0].description} 
      />
    </div>
  );
};


export default WeatherCard;
