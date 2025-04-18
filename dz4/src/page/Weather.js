import { useEffect, useState } from "react";
import styled from "../assets/style/page/Weather.module.css"
import { useDispatch, useSelector } from 'react-redux';
import { fetchWeather } from "../store/weatherSlice";
import CardWeather from "../components/WeatherCard";
import Input from "../components/Input";
import CitySelector from "../components/CitySelector";
import WeatherList from "../components/WeatherList";
import WeatherFullList from "../components/WeatherFullList"
import Placenemt from "../components/Placement";
import StatisticWeather from "../components/StatisticWeather";
import { getCityCoords } from "../utils/api";

const DEFAULT_CITY = "Moscow";

const Weather = () => {
  const dispatch = useDispatch();
  const { data, loading } = useSelector((state) => state.weather);
  const [city, setCity] = useState(DEFAULT_CITY)


  useEffect(() => {
    if (!data) {
      const getCityCoordinates = async (cityName) => {
        try {
          const coordinates = await getCityCoords(cityName); 
          dispatch(fetchWeather(coordinates)); 
        } catch (error) {
          console.error("Error getting city coordinates:", error);
        }
      };

      getCityCoordinates(city);
    }
  }, [city, data, dispatch]);

  console.log(data)

    // const data ={
    //   "cod": "200",
    //   "message": 0,
    //   "cnt": 40,
    //   "list": [
    //     {
    //       "dt": 1661871600,
    //       "main": {
    //         "temp": 296.76,
    //         "feels_like": 296.98,
    //         "temp_min": 296.76,
    //         "temp_max": 297.87,
    //         "pressure": 1015,
    //         "sea_level": 1015,
    //         "grnd_level": 933,
    //         "humidity": 69,
    //         "temp_kf": -1.11
    //       },
    //       "weather": [
    //         {
    //           "id": 500,
    //           "main": "Rain",
    //           "description": "light rain",
    //           "icon": "10d"
    //         }
    //       ],
    //       "clouds": {
    //         "all": 100
    //       },
    //       "wind": {
    //         "speed": 0.62,
    //         "deg": 349,
    //         "gust": 1.18
    //       },
    //       "visibility": 10000,
    //       "pop": 0.32,
    //       "rain": {
    //         "3h": 0.26
    //       },
    //       "sys": {
    //         "pod": "d"
    //       },
    //       "dt_txt": "2022-08-30 15:00:00"
    //     },
    //     {
    //       "dt": 1661882400,
    //       "main": {
    //         "temp": 295.45,
    //         "feels_like": 295.59,
    //         "temp_min": 292.84,
    //         "temp_max": 295.45,
    //         "pressure": 1015,
    //         "sea_level": 1015,
    //         "grnd_level": 931,
    //         "humidity": 71,
    //         "temp_kf": 2.61
    //       },
    //       "weather": [
    //         {
    //           "id": 500,
    //           "main": "Rain",
    //           "description": "light rain",
    //           "icon": "10n"
    //         }
    //       ],
    //       "clouds": {
    //         "all": 96
    //       },
    //       "wind": {
    //         "speed": 1.97,
    //         "deg": 157,
    //         "gust": 3.39
    //       },
    //       "visibility": 10000,
    //       "pop": 0.33,
    //       "rain": {
    //         "3h": 0.57
    //       },
    //       "sys": {
    //         "pod": "n"
    //       },
    //       "dt_txt": "2022-08-30 18:00:00"
    //     },
    //     {
    //       "dt": 1661893200,
    //       "main": {
    //         "temp": 292.46,
    //         "feels_like": 292.54,
    //         "temp_min": 290.31,
    //         "temp_max": 292.46,
    //         "pressure": 1015,
    //         "sea_level": 1015,
    //         "grnd_level": 931,
    //         "humidity": 80,
    //         "temp_kf": 2.15
    //       },
    //       "weather": [
    //         {
    //           "id": 500,
    //           "main": "Rain",
    //           "description": "light rain",
    //           "icon": "10n"
    //         }
    //       ],
    //       "clouds": {
    //         "all": 68
    //       },
    //       "wind": {
    //         "speed": 2.66,
    //         "deg": 210,
    //         "gust": 3.58
    //       },
    //       "visibility": 10000,
    //       "pop": 0.7,
    //       "rain": {
    //         "3h": 0.49
    //       },
    //       "sys": {
    //         "pod": "n"
    //       },
    //       "dt_txt": "2022-08-30 21:00:00"
    //     }
    //   ]}


    return(
        <div style={{display:"flex", flexDirection:"column", justifyContent:"center", alignItems:"center"}}>
          <h1>Weather Forecast</h1>
          <div style={{display:"flex", flexDirection:"column", gap:"25px"}}>
            <CitySelector/>
            {!loading && data ? (
                <>
                    <Placenemt data={data} city={city}/>
                    <WeatherList data={data} loading={loading} />
                    <StatisticWeather data={data} />
                    <WeatherFullList data={data}/>
                </>
            ) : <p>Нет данных</p>}
          </div>
        </div>
    )
}

export default Weather;
