import { useState } from 'react';
import { useDispatch } from 'react-redux';
import { fetchWeather } from '../store/weatherSlice';
import styled2 from  "../assets/style/page/Weather.module.css"
import { getCityCoords } from '../utils/api';  

const CitySelector = ({...props}) => {
  const [city, setCity] = useState('');
  const dispatch = useDispatch();

  const handleSearch = async () => {
    const location = await getCityCoords(city); 
    if (location) dispatch(fetchWeather({ lat: location.lat, lon: location.lon }));
  };

  return (
    <div style={{display:"flex", gap:"15px", justifyContent:"center"}}>
      <input className={styled2.search_input} type="text" value={city} onChange={(e) => setCity(e.target.value)} />
      <button className={styled2.search_btn} onClick={handleSearch}>Search</button>
    </div>
  );
};

export default CitySelector;