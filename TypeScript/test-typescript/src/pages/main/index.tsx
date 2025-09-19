import {HttpProvider} from "../../HttpProvider";
import Button from "../../components/layout/Button";
import Select from "../../components/layout/Select";
import React from 'react';

interface props{
    setIsPageMain: (isPageMain: boolean) => void
}

function Main(props: props) {

    let requestCurrent = new HttpProvider("example.local");

    return (
        <div className="App">
            <Button onclick={() => props.setIsPageMain(false)}>Test API</Button>
            <Select value={[{value1: "One"}, {value2: "Two"}, {value3:"Three"}]} defaultValue={"Two"}></Select>
        </div>
    )
}

export default Main;