import { useEffect, useState } from "react";
import styled from "../assets/style/page/Weather.module.css"
import CardWeather from "../components/CardWeather";
import Input from "../components/Input";

const Weather = () => {
    const [weather, setWeather] = useState()

    useEffect(() => {
        const responce = fetch("https://openweathermap.org/weather-conditions")

    },[])

    return(
        <div>
            <Input className={styled.search_input} placeholder="Введите свой город для отображения пагоды"></Input>
            <CardWeather className={styled.card_wather}></CardWeather>
        </div>
    )
}

export default Weather;
