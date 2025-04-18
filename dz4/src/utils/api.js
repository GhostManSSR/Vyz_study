const API_KEY = 'bca2bf4e379fb41ccda465fefc33d72e';

export const getWeather = async (lat, lon) => {
    const response = await fetch(`https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=${API_KEY}&units=metric`);
    return response.json();
  };
  
  export const getCityCoords = async (city) => { 
    const response = await fetch(`https://api.openweathermap.org/geo/1.0/direct?q=${city}&limit=1&appid=${API_KEY}`);
    const data = await response.json();
    return data[0] || null;
  };

  export const getResuerse = async () => { 
    // const response = await fetch(`https://fakeapi.extendsclass.com/countries`);
    const response = await fetch(`https://jsonplaceholder.typicode.com/comments`);
    return response.json();
  };