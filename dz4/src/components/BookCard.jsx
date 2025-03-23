import styled from "../assets/style/BookCard.module.css";
import Button from "./Button";

function BookCard({ ...props }) { 
    return (
      <div className={styled.book_card}>
        {props.cover ? (
          <img src={props.cover} alt={props.book.title} className={styled.book_cover} />
        ) : (
          <div className={styled.placeholder}>Нет обложки</div>
        )}
        <h2 className={styled.book_title}>{props.book.title}</h2>
        <p className={styled.book_authors}>{props.book.authors?.join(", ") || "Неизвестный автор"}</p>
        <Button>Купить</Button>
      </div>
    );
  }

export default BookCard;
