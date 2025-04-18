


const CardRes = ({...props}) => {

    props.data.map(item => console.log(item.name))

    return(
        <div style={{display:'flex', flexDirection:'column', marginTop:"25px"}}> 
            {props.data.map(item => 
                (
                    <div style={{display:"flex", justifyContent:"space-between", background:"purple", borderRadius:"20px", height:"45px", marginBottom:"15px",alignItems:"center", padding:"5px 5px"}}>
                        <span>{item.name}</span>
                        <span>{item.admissionDateUnitedNations}</span>
                    </div>
                )
            )}
        </div>
    )
}

export default CardRes;