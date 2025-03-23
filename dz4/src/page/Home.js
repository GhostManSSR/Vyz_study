import { useEffect, useState, useMemo } from 'react';
import BookCard from '../components/BookCard';
import Select from '../components/Select';
import Input from '../components/Input';
import styled from "../../src/assets/style/BookCard.module.css";

function Home() {
  const [initialBooks, setInitialBooks] = useState([]);
  const [books, setBooks] = useState([]);
  const [covers, setCovers] = useState({});
  const [input, setInput] = useState("");
  const [types, setTypes] = useState("Titles")
  const [sortOrder, setSortOrder] = useState(1);

  useEffect(() => {
    const fetchBooks = async () => {
      try {
        const response = await fetch("https://fakeapi.extendsclass.com/books");
        const data = await response.json();
        setBooks(data);
        setInitialBooks(data);
        fetchCovers(data);
      } catch (error) {
        console.error("Ошибка загрузки книг:", error);
      }
    };

    const fetchCovers = async (books) => {
      const coverMap = {};

      for (const book of books) {
        if (coverMap[book.id]) continue;

        try {
          let query = `isbn:${encodeURIComponent(book.isbn)}`;
          let response = await fetch(`https://www.googleapis.com/books/v1/volumes?q=${query}`);
          let data = await response.json();
          let imageUrl = data.items?.[0]?.volumeInfo?.imageLinks?.thumbnail;

          if (!imageUrl) {
            query = `intitle:${encodeURIComponent(book.title)}`;
            response = await fetch(`https://www.googleapis.com/books/v1/volumes?q=${query}`);
            data = await response.json();
            imageUrl = data.items?.[0]?.volumeInfo?.imageLinks?.thumbnail;
          }

          coverMap[book.id] = imageUrl || null;
        } catch (error) {
          console.error(`Ошибка загрузки обложки для ${book.title}:`, error);
          coverMap[book.id] = null;
        }
      }

      setCovers(coverMap);
    };

    fetchBooks();
  }, []);

  const filteredBooks = useMemo(() => {
    let result = [...books];

    if (input.trim() !== "") {
      result = result.filter(el =>
        el.title?.toLowerCase().includes(input.toLowerCase()) ||
        (Array.isArray(el.author) && el.author.some(name => name?.toLowerCase().includes(input.toLowerCase())))
      );
    }

    if(types === "Titles"){
      if (sortOrder === "asc") {
        result.sort((a, b) => (a.title?.toLowerCase() || "") > (b.title?.toLowerCase() || "") ? 1 : -1);
      } else if (sortOrder === "desc") {
        result.sort((a, b) => (a.title?.toLowerCase() || "") < (b.title?.toLowerCase() || "") ? 1 : -1);
      }
    }

    if(types === "Athor"){
      // result.map(el => console.log(el.author))
      if (sortOrder === "asc") {
        result.sort((a, b) => 
          ((a.authors?.join(" ").toLowerCase() || "") > (b.authors?.join(" ").toLowerCase() || "") ? 1 : -1)
          )
      } else if (sortOrder === "desc") {
        result.sort((a, b) => (a.authors?.join(" ").toLowerCase() || "") < (b.authors?.join(" ").toLowerCase() || "") ? 1 : -1);
      }
    }


    console.log("Отсортированные книги:", JSON.stringify(result, null, 2));
    return result;
  }, [input, books, sortOrder]);

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

    if (selectedId === 1) {
      setTypes("Titles");
    } else if (selectedId === 2) {
      setTypes("Athor");
    } else {
      setTypes(null);
    }
  }
  const selectData = [
    { title: "Без сортировки", id: 0 },
    { title: "Сортировка по возрастанию", id: 1 },
    { title: "Сортировка по убыванию", id: 2 }
  ];

  const typeSelect = [
    { title: "По названию", id: 1 },
    { title: "По автору", id: 2 },
  ]

  return (
    <>
      <div className={styled.search_input_box}>
        <Select
          option={selectData}
          className={styled.select_item}
          onChange={handleSortChange}
        />
        <Select
          option={typeSelect}
          className={styled.select_item}
          onChange={handleTypeChande}
        />
        <Input
          placeholder="Введите название книги или автора"
          className={styled.search_input}
          onChange={(e) => setInput(e.target.value)}
        />
      </div>
      <div className={styled.book_container}>
        {filteredBooks.map((item) => (
          <BookCard key={item.id} book={item} cover={covers[item.id]} />
        ))}
      </div>
    </>
  );
}

export default Home;
