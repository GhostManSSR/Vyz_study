const Select = ({ ...props }) => {
    const handleChange = (event) => {
        const target = event.target; 
        if (target && target.value) {
          const selectedId = Number(target.value); 
          props.onChange(selectedId); 
        }
      };
  
    return (
      <select className={props.className} onChange={handleChange}>
        {props.option.map((item) => (
          <option key={item.id} value={item.id}> 
            {item.title}
          </option>
        ))}
      </select>
    );
  };
  
  export default Select;
  