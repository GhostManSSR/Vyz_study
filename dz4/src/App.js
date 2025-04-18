import './App.css';
import BookCard from './components/BookCard';
import {BrowserRouter as Router, Routes, Route, NavLink} from 'react-router-dom';
// import styled from "../src/assets/style/BookCard.module.css"
import { useEffect, useState } from 'react';
import { Provider } from 'react-redux';
import { store } from './store/store';
import styled from  "./assets/style/page/Weather.module.css"
import CitySelector from './components/CitySelector';
import WeatherList from './components/WeatherList';
import Home from './page/Home';
import Data from './page/Data';
import Button from './components/Button';
import Shem from './page/Shem';
import Weather from './page/Weather';
import ProgressBar from './components/layout/PrograssBar/PrograssBar';
import Loader from './page/Loader';
import SelectFirst from './page/SelectFirst';
import Toggle from './components/layout/Toggle/Toggle';
import Formas from "./page/Formas";
import SideBar from "./components/forms/SideBar";
import Albums from './page/Albums';
import PostsPage from "./page/PostsPage";
import Todos from "./page/Todos"
import Users from "./page/Users";
import Pets from "./page/Pets";
import FormPerson from "./components/forms/FormPerson";
import PersonCompanentsPage from "./page/PersonCompanents";

function App() {
  const [tab, setTab] = useState(0)

  return (
    <>
        {/*<Router>*/}
        {/*    <div className="flex">*/}
        {/*       <SideBar/>*/}
        {/*        <main className="ml-48 p-4 w-full">*/}
        {/*            <Routes>*/}
        {/*                <Route path="/posts" element={<PostsPage />} />*/}
        {/*                <Route path="/albums" element={<Albums />} />*/}
        {/*                <Route path="/todos" element={<Todos />} />*/}
        {/*                <Route path="/users" element={<Users />} />*/}
        {/*            </Routes>*/}
        {/*        </main>*/}
        {/*    </div>*/}
        {/*</Router>*/}
        <Pets/>
      {/* <Toggle
        menuLink={["/SelectFirst/", "/Loader/"]} 
        menuStatus={["Первая", "Вторая"]}
      /> */}
     {/* <ProgressBar></ProgressBar> */}
     {/* <Loader></Loader> */}
     {/* <SelectFirst></SelectFirst>*/}
      {/* <Provider store={store}>
        <Weather></Weather>
      </Provider> */}
    {/* <div style={{display:"flex", marginTop:"125px"}}>
      <Button onClick={() => setTab(0)}>Tab1</Button>
      <Button onClick={() => setTab(1)}>Tab2</Button>
    </div>
    {tab==0 ? 
      (
      <Home>
        </Home>
      ):
      (
        <Shem></Shem>
      )
    
    } */}
  {/* <Home></Home> */}
  {/* <Shem></Shem> */}
  </>

  );
}

export default App;
