import { useEffect, useOptimistic, useState } from 'react';
import DataList from '../components/layout/DataList/DataList';
import AddCommentModal from '../components/AddCommentModal';
import Button from '../components/Button';
import styles from "../assets/style/page/Loader.module.css";

export default function GuidePage() {
    const [guides, setGuides] = useState([]);
    const [localChanges, setLocalChanges] = useState([]);
    const [selectedIds, setSelectedIds] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formData, setFormData] = useState(null);

    const [optimisticGuides, setOptimisticGuides] = useOptimistic(guides);

    const mergeData = (serverData, localChanges) => {
        const ids = new Set(localChanges.map(item => item.id));
        return [
            ...serverData.filter(item => !ids.has(item.id)),
            ...localChanges
        ];
    };

    const fetchGuides = async () => {
        try {
            const res = await fetch('https://jsonplaceholder.typicode.com/albums');
            const data = await res.json();
            const merged = mergeData(data, localChanges);
            setGuides(merged);
            setOptimisticGuides(merged);
        } catch (err) {
            console.error('Ошибка загрузки альбомов:', err);
        }
    };

    useEffect(() => {
        fetchGuides();
    }, []);

    const handleAddGuide = async (newGuide) => {
        const tempId = Math.random().toString(36).substring(2, 9);
        const optimisticGuide = { ...newGuide, id: tempId };

        const optimisticList = [optimisticGuide, ...guides];
        setGuides(optimisticList);
        setOptimisticGuides(optimisticList);
        setLocalChanges(prev => [...prev, optimisticGuide]);

        try {
            const res = await fetch('https://jsonplaceholder.typicode.com/todos', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newGuide)
            });

            if (!res.ok) throw new Error('POST failed');
            const saved = await res.json();

            const updated = optimisticList.map(item => item.id === tempId ? saved : item);
            setGuides(updated);
            setOptimisticGuides(updated);
            setLocalChanges(prev => {
                const filtered = prev.filter(item => item.id !== tempId);
                return [...filtered, saved];
            });
        } catch (err) {
            console.error('Ошибка при добавлении:', err);
            const reverted = guides.filter(item => item.id !== tempId);
            setGuides(reverted);
            setOptimisticGuides(reverted);
        }
    };

    const handleEditGuide = async (editedGuide) => {
        const { id } = editedGuide;
        if (!id) {
            console.error("Редактирование: не указан id", editedGuide);
            return;
        }

        const prevState = [...guides];
        const updated = guides.map(item => item.id === id ? { ...item, ...editedGuide } : item);
        setGuides(updated);
        setOptimisticGuides(updated);
        setLocalChanges(prev => {
            const filtered = prev.filter(item => item.id !== id);
            return [...filtered, editedGuide];
        });

        try {
            const res = await fetch(`https://jsonplaceholder.typicode.com/todos/${id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(editedGuide)
            });

            if (!res.ok) throw new Error("PATCH failed");
        } catch (err) {
            console.error('Ошибка при редактировании:', err);
            setGuides(prevState);
            setOptimisticGuides(prevState);
        }
    };

    const handleDelete = async () => {
        if (selectedIds.length === 0) return;

        const prevState = [...guides];
        const updated = guides.filter(item => !selectedIds.includes(item.id));
        setGuides(updated);
        setOptimisticGuides(updated);
        setLocalChanges(prev => prev.filter(item => !selectedIds.includes(item.id)));

        try {
            await Promise.all(
                selectedIds.map(id =>
                    fetch(`https://jsonplaceholder.typicode.com/todos/${id}`, {
                        method: 'DELETE'
                    })
                )
            );
        } catch (err) {
            console.error('Ошибка при удалении:', err);
            setGuides(prevState);
            setOptimisticGuides(prevState);
        }

        setSelectedIds([]);
    };

    const handleStartEdit = () => {
        if (selectedIds.length !== 1) {
            alert("Выберите один альбом для редактирования!");
            return;
        }

        const toEdit = guides.find(item => item.id === selectedIds[0]);
        if (toEdit) {
            setFormData(toEdit);
            setIsModalOpen(true);
        }
    };

    const handleModalSubmit = (guide) => {
        const guideWithId = formData ? { ...guide, id: formData.id } : guide;

        // Преобразуем completed в булевое значение
        guideWithId.completed = guideWithId.completed === 'true' || guideWithId.completed === true;

        if (formData) {
            handleEditGuide(guideWithId);
        } else {
            handleAddGuide(guideWithId);
        }

        setIsModalOpen(false);
        setFormData(null);
    };

    const guideFields = [
        { key: 'userId', label: 'ID пользователя', placeholder: 'Введите userId' },
        { key: 'id', label: 'ID', placeholder: 'Введите Id' },
        { key: 'title', label: 'Название альбома', placeholder: 'Введите название' },
        { key: 'completed', label: 'Выполнение', placeholder: 'Введите Выполнение', type: 'checkbox' } // Учитываем поле как булево значение
    ];

    return (
        <div>
            <h1 className="text-xl font-bold mb-4">Задачи</h1>
            <div style={{ display: "flex", justifyContent: "end", gap: "15px", marginBottom: "15px" }}>
                <Button onClick={() => setIsModalOpen(true)} className={styles.comment_add__btn}>Добавить задачу</Button>
                <Button onClick={handleStartEdit} className={styles.comment_add__btn}>Редактировать</Button>
                <Button onClick={handleDelete} className={styles.cooment_delete__btn}>Удалить</Button>
            </div>

            <AddCommentModal
                isOpen={isModalOpen}
                onClose={() => { setIsModalOpen(false); setFormData(null); }}
                onSubmit={handleModalSubmit}
                defaultData={formData}
                fields={guideFields}
                titles={'задачу'}
            />

            <div className="mt-4">
                <DataList
                    data={optimisticGuides}
                    columns={["userId", "id", "title", "completed"]}
                    onSelect={setSelectedIds}
                />
            </div>
        </div>
    );
}
