import { useEffect, useState, useOptimistic } from 'react';
import ProgressBar from '../components/layout/PrograssBar/PrograssBar';
import styles from "../assets/style/page/Loader.module.css";
import { getResuerse } from "../utils/api";
import DataList from "../components/layout/DataList/DataList";
import AddCommentModal from '../components/AddCommentModal';
import Button from '../components/Button';

const Loader = () => {
    const [data, setData] = useState([]);
    const [localChanges, setLocalChanges] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [selectedIds, setSelectedIds] = useState([]);
    const [editData, setEditData] = useState(null);

    const [optimisticData, setOptimisticData] = useOptimistic(data);

    const mergeData = (serverData, localChanges) => {
        const ids = new Set(localChanges.map(item => item.id));
        const merged = [
            ...serverData.filter(item => !ids.has(item.id)),
            ...localChanges
        ];
        return merged;
    };

    const fetchData = async () => {
        try {
            const serverData = await getResuerse();
            const merged = mergeData(serverData, localChanges); 
            setData(merged);
            setOptimisticData(merged);
        } catch (error) {
            console.error("Ошибка при обновлении данных:", error);
        }
    };

    // useEffect(() => {
    //     fetchData();
    //     setTimeout(() => setLoading(false), 1000);

    //     const interval = setInterval(() => {
    //         fetchData();
    //     }, 15000); 

    //     return () => clearInterval(interval);
    // }, [localChanges]);

    useEffect(() => {
        fetchData();
        setTimeout(() => setLoading(false), 1000);
    }, []);

    const handleEditComment = async (editedComment) => {
        const { id } = editedComment;
        const oldData = [...data];

        const updated = data.map(item => item.id === id ? { ...item, ...editedComment } : item);
        setData(updated);
        setOptimisticData(updated);

        setLocalChanges(prev => {
            const without = prev.filter(item => item.id !== id);
            return [...without, editedComment];
        });

        try {
            const res = await fetch(`https://jsonplaceholder.typicode.com/comments/${id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(editedComment)
            });

            if (!res.ok) throw new Error("PATCH failed");
        } catch (err) {
            console.error("Ошибка при редактировании:", err);
            setData(oldData);
            setOptimisticData(oldData);
        }
    };

    const handleStartEdit = () => {
        if (selectedIds.length !== 1) {
            alert("Выберите один элемент для редактирования!");
            return;
        }

        const row = data.find(item => item.id === selectedIds[0]);
        if (row) {
            setEditData(row);
            setShowModal(true);
        }
    };

    const handleModalSubmit = (comment) => {
        if (editData) {
            handleEditComment(comment);
        } else {
            handleAddComment(comment);
        }
        setShowModal(false);
        setEditData(null);
    };

    const handleAddComment = async (newComment) => {
        const tempId = Math.random().toString(36).substring(2, 9);
        const optimisticComment = { ...newComment, id: tempId };

        const updated = [...data, optimisticComment];
        setData(updated);
        setOptimisticData(updated);

        setLocalChanges(prev => [...prev, optimisticComment]);

        try {
            const res = await fetch("https://jsonplaceholder.typicode.com/comments", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(newComment)
            });
            const saved = await res.json();

            const replaced = updated.map(item => item.id === tempId ? saved : item);
            setData(replaced);
            setOptimisticData(replaced);

            setLocalChanges(prev => {
                const filtered = prev.filter(item => item.id !== tempId);
                return [...filtered, saved];
            });

        } catch (err) {
            console.error("Ошибка при добавлении", err);
            const reverted = data.filter(item => item.id !== tempId);
            setData(reverted);
            setOptimisticData(reverted);
        }
    };

    const handleDelete = async () => {
        const updated = data.filter(item => !selectedIds.includes(item.id));
        setData(updated);
        setOptimisticData(updated);

        setLocalChanges(prev => prev.filter(item => !selectedIds.includes(item.id)));

        for (const id of selectedIds) {
            try {
                await fetch(`https://jsonplaceholder.typicode.com/comments/${id}`, {
                    method: "DELETE"
                });
            } catch (e) {
                console.error("Ошибка при удалении", e);
            }
        }

        setSelectedIds([]);
    };

    return (
        <div className={styles.box_loader}>
            {loading ? <ProgressBar title="Task" loading={loading} setloading={setLoading} /> : null}
            {!loading && (
                <div style={{ display: "flex", justifyContent: "center", flexDirection: "column" }}>
                    <div style={{ display: "flex", justifyContent: "end", gap: "15px", marginBottom: "15px" }}>
                        <Button onClick={() => setShowModal(true)} className={styles.comment_add__btn}>Добавить комментарий</Button>
                        <Button onClick={handleStartEdit} className={styles.comment_add__btn}>Редактировать</Button>
                        <Button onClick={handleDelete} className={styles.cooment_delete__btn}>Удалить</Button>
                    </div>
                    <DataList
                        data={optimisticData}
                        onSelect={(ids) => setSelectedIds(ids)}
                        onEdit={handleEditComment}
                        renderHeader={(header) => header.toUpperCase()}
                        renderCell={(value, row, key) => key === "name" ? `${value}` : value}
                    />
                    <AddCommentModal
                        isOpen={showModal}
                        onClose={() => {
                            setShowModal(false);
                            setEditData(null);
                        }}
                        onSubmit={handleModalSubmit}
                        defaultData={editData}
                    />
                </div>
            )}
        </div>
    );
};

export default Loader;
