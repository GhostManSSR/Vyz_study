import React, {useState} from 'react';
import logo from './logo.svg';
import './App.css';
import Button from './components/layout/Button'
import Select from './components/layout/Select'
import {HttpProvider} from "./HttpProvider";
import Main from "../src/pages/main/index"

function App() {
    const [isPageMain, setIsPageMain] = React.useState(false);
    let requestCurrent = new HttpProvider("example.local");

    return (
        <>
            {isPageMain ? <Main setIsPageMain={setIsPageMain}/> : <div className="App">
                    <Button onclick={() => setIsPageMain(true)}>Test API</Button>
            </div>
            }</>
  );
}

export default App;
