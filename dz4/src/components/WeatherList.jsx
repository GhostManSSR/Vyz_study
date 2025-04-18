import { useSelector } from 'react-redux';
import WeatherCard from './WeatherCard';
import styled3 from "../assets/style/layout/CardWeather/CardWeather.module.css"
import Loader from './Loader';

const WeatherList = ({...props}) => {
  // const { data, loading } = useSelector((state) => state.weather);
  const {
    data,
    loading
  } = props

  if (loading) {
    return <p>Загрузка...</p>;
}

if (!data || !data.list || data.list.length === 0) {
    return <p></p>;
}

  return (
    <div className={styled3.weather_list}>
      {data.list.slice(0, 5).map((item) => (
        <WeatherCard key={item.dt} weather={item} />
      ))}
    </div>
  );
};

export default WeatherList;