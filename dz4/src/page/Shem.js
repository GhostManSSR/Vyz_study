import { useEffect, useState, useMemo } from "react";
import styled from "../../src/assets/style/BookCard.module.css";
import CardPerson from "../components/CardPerson";
import Select from "../components/Select";
import Input from "../components/Input";


const Shem = () => {

    const [shem,setShem] = useState([])
    const [input, setInput] = useState("");
    const [types, setTypes] = useState(null)
    const [sortOrder, setSortOrder] = useState(null);

    useEffect(() => {
        const fetchBooks = async () => {
            try {
              const response = await fetch("https://jsonplaceholder.typicode.com/posts");
              const data = await response.json();
              setShem(data);
              console.log(data)
            } catch (error) {
              console.error("Ошибка загрузки книг:", error);
            }
        };
        fetchBooks();
    },[])

    const handleSortChange = (selectedId) => {
        if (selectedId === 1) {
            setSortOrder("asc");
          } else if (selectedId === 2) {
            setSortOrder("desc");
          } else {
            setSortOrder(null);
          }
      };
    
    const handleTypeChande = (selectedId) => {
        console.log(selectedId)

        if (selectedId === 1) {
            setTypes("titles");
          } else if (selectedId === 2) {
            setTypes("body");
          } else {
            setTypes(null);
          }
      }

        const filteredShem = useMemo(() => {
          let result = [...shem];

          if (input.trim() !== "") {
            result = result.filter(el =>
                (
                    el.title?.toLowerCase().includes(input.toLowerCase()) ||
                    Array.isArray(el.title) && el.title.some(name => name?.toLowerCase().includes(input.toLowerCase()))
                )
            );
          }
          console.log(types)
          if(types === "titles"){
            if (sortOrder === "asc") {
              result.sort((a, b) => (a.title?.toLowerCase() || "") > (b.title?.toLowerCase() || "") ? 1 : -1);
            } else if (sortOrder === "desc") {
              result.sort((a, b) => (a.title?.toLowerCase() || "") < (b.title?.toLowerCase() || "") ? 1 : -1);
            }
          }
      
          if(types === "body"){
            if (sortOrder === "asc") {
                result.sort((a, b) => (a.body?.toLowerCase() || "") > (b.body?.toLowerCase() || "") ? 1 : -1);
              } else if (sortOrder === "desc") {
                result.sort((a, b) => (a.body?.toLowerCase() || "") < (b.body?.toLowerCase() || "") ? 1 : -1);
              }
          }
      
      
        //   console.log("Отсортированные книги:", JSON.stringify(result, null, 2));
          return result;
        }, [input, shem, sortOrder]);


      const selectData = [
        { title: "Без сортировки", id: 0 },
        { title: "Сортировка по возрастанию", id: 1 },
        { title: "Сортировка по убыванию", id: 2 }
      ];
    
      const typeSelect = [
        { title: "По заглавию", id: 1 },
        { title: "По содержинию", id: 2 },
      ]

    return(
        <>
            <div className={styled.search_input_box}>
                <Select
                    option={typeSelect}
                    className={styled.select_item}
                    onChange={handleTypeChande}
                />
                <Select
                    option={selectData}
                    className={styled.select_item}
                    onChange={handleSortChange}
                />
                <Input
                    placeholder="Введите заглавие или содержание"
                    className={styled.search_input}
                    onChange={(e) => setInput(e.target.value)}
                />
            </div>
            <div style={{display:"flex", flexWrap:"wrap" , marginTop: "150px", justifyContent:"center", gap:"15px"}}>
                {filteredShem.map((item) => (
                    <CardPerson key={item.id} id={item.id} userId={item.userId} title={item.title} body={item.body}/>
                )
                )}
            </div>
        </>
    )
}

export default Shem