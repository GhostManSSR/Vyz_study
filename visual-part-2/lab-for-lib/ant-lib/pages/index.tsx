import Toggle from "@/component/layout/Toggle";
import {useRouter} from "next/router";


const Index:React.FC = () => {
    const router = useRouter();

    return(
        <div>
            <Toggle
                toggleList={
                    [
                        {name:"Form", onClick: () => router.push("/form")},
                        {name: "Modals", onClick: () => router.push("/modals")},
                        {name: "Dialogs", onClick: () => router.push("/dialog")},
                    ]
                }
            />
        </div>
    )
}

export default Index;