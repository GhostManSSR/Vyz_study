import { useState } from "react";
import styles from "../../../assets/style/layout/SelectCustom/SelectCustom.module.css"

const SelectCustom = ({...props}) => {
    const {
        renderHeader,
        header,
        BlockDriwing,
        data
    } = props

    const [clickable, setClickabels] = useState(false)

    const ArrowCustom = ({...props}) => {
        return(
            <>
            {props.isup ? <span className={styles.arrows} onClick={() => setClickabels(prev => !prev)}>&#8593;</span> : 
                <span className={styles.arrows} onClick={() => setClickabels(prev => !prev)}>&#8595;</span>}
            </>
        )
    }

    const SelectInp = ({...props}) => {
        return(
            <>
                <span>{props.item}</span>
                <ArrowCustom/>
            </>
        )
    }

    const Lists = ({...props}) => {
        return(
            <ul>
                {Array.from(props.data).map((items, index) => (
                    <li key={index}></li>
                ))}
            </ul>
        )
    }

    console.log(data)

    return(
        <div style={{display:'flex', flexDirection:"column"}}>
            <div className={styles.block__select}>
                {/* {renderHeader ? renderHeader(header) : header} */}
                {/* <ArrowCustom isup={clickable} /> */}
                {Object.values(data).map((item,index) => {
                        console.log(item);
                        if(typeof(item) !== "object"){
                           return(
                                <SelectInp item={item}/>
                           ) 
                        }
                        return (
                            null
                        )
                        // return(
                        //     <>
                        {/* {renderHeader ? renderHeader(item) : null} */}
                                    {/* <ArrowCustom isup={clickable} /> */}
                        //         <Lists data={item}/>
                        //     </>
                        // )
                })}
            </div>
            {/* {BlockDriwing ? <BlockDriwing isup={clickable}/>: null} */}
        </div>
    )
}


export default SelectCustom;