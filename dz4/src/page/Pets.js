import {BrowserRouter, Link, Links, NavLink, Route, Router, Routes} from "react-router-dom";
import FormPerson from "../components/forms/FormPerson";
import PersonCompanentsPage from "./PersonCompanents";
import {useState} from "react";

const Pets = () => {
    const [person, setPerson] = useState([]);

    console.log(person);

    return(
        <div style={{display: "flex", justifyContent: "space-between", flexDirection: "column"}}>
            <BrowserRouter>
                <nav style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '50%', padding:"1em 2em"}}>
                    <Link to="/formperson">FormPerson</Link>
                    <Link to="/PersonCompanentsPage">PersonCompanentsPage</Link>
                </nav>
                <Routes>
                    <Route path="formperson" element={<FormPerson person={person} />} />
                    <Route path="personcompanentspage" element={<PersonCompanentsPage person={person} />} />
                </Routes>
            </BrowserRouter>
        </div>
    )
}

export default Pets;