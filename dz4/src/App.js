import './App.css';
import BookCard from './components/BookCard';
import styled from "../src/assets/style/BookCard.module.css"
import { useEffect, useState } from 'react';
import Home from './page/Home';
import Data from './page/Data';
import Button from './components/Button';
import Shem from './page/Shem';

function App() {
  const [tab, setTab] = useState(0)

  return (
    <>
    <div style={{display:"flex", marginTop:"125px"}}>
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
    
    }
  {/* <Home></Home> */}
  {/* <Shem></Shem> */}
  </>

  );
}

export default App;
