import Card from "../components/Card";
import { data } from "../data/data";
import styled from "../assets/style/BookCard.module.css"


function Data(){


    return(
        <div className={styled.book_container}>
            {data.map((item) => (
                <Card
                    style={styled.book_card} 
                    key={item.id}
                    id={item.id} 
                    fullName={item.fullName} 
                    firstName={item.name.firstName} 
                    lastName={item.name.lastName}
                    addresLine={item.address.line1}
                    addresTown={item.address.town}
                    addresCounty={item.address.county}
                    addresCountry={item.address.country}
                    email={item.email}
                ></Card>
            ))}
        </div>
    )
}


export default Data;