import {useOptimistic, useState} from "react";
import DataList from "../components/layout/DataList/DataList";
import {person} from "../data/data";


const PersonCompanentsPage = ({...props}) => {
    const [data, setData] = useState(props.person);
    const [selectedIds, setSelectedIds] = useState([]);
    const [localChanges, setLocalChanges] = useState([]);
    const [optimisticGuides, setOptimisticGuides] = useOptimistic(data);

    console.log(props.person);


    return(
        <div style={{display: "flex", flexDirection: "column"}}>
            <h1>Person Companents</h1>

            <DataList
                data={data}
                columns={["age", "email", "name", "pets"]}
                onSelect={setSelectedIds}
                renderColumnCell={{
                    pets: (pets) => {
                        for(pets of pets){
                            return `${pets.name || ''}, ${pets.age || 0}`;
                        }
                        return 'Нет данных';
                    }
                }}
            />
        </div>
    )
}

export default PersonCompanentsPage;