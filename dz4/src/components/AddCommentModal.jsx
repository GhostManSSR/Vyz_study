import styles from "../assets/style/layout/AddCommentModal/AddCommentModal.module.css";
import { ImCross } from "react-icons/im";
import PostForm from "./forms/PostForm"; // ← добавляем импорт формы

const AddCommentModal = ({ isOpen, onClose, onSubmit, defaultData = null,fields = [], ...props }) => {
    if (!isOpen) return null;

    const defaultFormData = defaultData ? {
        ...defaultData,
        completed: defaultData.completed ? 'true' : 'false', // Преобразуем булево значение в строку для формы
    } : {};

    return (
        <div className={styles.modalBackdrop}>
            <div className={styles.modal}>
                <div style={{display:'flex', justifyContent:"space-between", width:'100%'}}>
                    <h2>{defaultData ? `Редактировать ${props.titles}` : `Добавить ${props.titles}`}</h2>
                    <span onClick={onClose} style={{cursor:"pointer"}}><ImCross></ImCross></span>
                </div>
                <PostForm
                    defaultData={defaultData}
                    fields={fields}
                    onSubmit={(formData) => {
                        onSubmit(formData);
                        onClose();
                    }}
                    onCancel={onClose}
                />
            </div>
        </div>
    );
};

export default AddCommentModal;
