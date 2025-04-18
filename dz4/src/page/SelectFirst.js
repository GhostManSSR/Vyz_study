import SelectCustom from "../components/layout/SelectCustom/SelectCustom";
import styles from "../assets/style/layout/SelectCustom/SelectCustom.module.css"

const SelectFirst = ({...props}) => {

    const BlockDriwing = ({...props}) => {
        return(
            <>
            {props.isup ? 
            <div className={styles.block_driwing} style={{height: props.isup ? "auto" : "0px", bottom: props.isup ? "0px" : 'auto'}}>
                <ul>
                    <li>🍏 Appple</li>
                    <li>🍌 Banana</li>
                    <li>🍊 Orange</li>
                </ul>
            </div>
            :
            null
            }
        </>
        )
    }
    
    const structTree= {
        li1: "afsaf",
        li2: "safsaf",
        li3: {
            li5: "dad"
        }
    }

    return(
        <div style={{display:'flex', justifyContent:"center", marginTop:"65px"}}>
            <SelectCustom
                renderHeader={(header) => header.toUpperCase()}
                data={structTree}
            />
        </div>
    )
}


export default SelectFirst;