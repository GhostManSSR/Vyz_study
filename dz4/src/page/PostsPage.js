// pages/PostsPage.jsx
import { useEffect, useOptimistic, useState } from 'react';
import DataList from '../components/layout/DataList/DataList';
import AddCommentModal from '../components/AddCommentModal';
import Button from '../components/Button';
import styles from "../assets/style/page/Loader.module.css";
import PostForm from "../components/forms/PostForm";

export default function PostsPage() {
    const [posts, setPosts] = useState([]);
    const [localChanges, setLocalChanges] = useState([]);
    const [selectedIds, setSelectedIds] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formData, setFormData] = useState(null);

    const [optimisticPosts, setOptimisticPosts] = useOptimistic(posts);

    const mergeData = (serverData, localChanges) => {
        const ids = new Set(localChanges.map(post => post.id));
        return [
            ...serverData.filter(post => !ids.has(post.id)),
            ...localChanges
        ];
    };

    const fetchPosts = async () => {
        try {
            const res = await fetch('https://jsonplaceholder.typicode.com/posts');
            const data = await res.json();
            const merged = mergeData(data, localChanges);
            setPosts(merged);
            setOptimisticPosts(merged);
        } catch (err) {
            console.error('Ошибка загрузки постов:', err);
        }
    };

    useEffect(() => {
        fetchPosts();
    }, []);

    const handleAddPost = async (newPost) => {
        const tempId = Math.random().toString(36).substring(2, 9);
        const optimisticPost = { ...newPost, id: tempId };

        const optimisticList = [optimisticPost, ...posts];
        setPosts(optimisticList);
        setOptimisticPosts(optimisticList);
        setLocalChanges(prev => [...prev, optimisticPost]);

        try {
            const res = await fetch('https://jsonplaceholder.typicode.com/posts', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newPost)
            });

            if (!res.ok) throw new Error('POST failed');
            const saved = await res.json();

            const updated = optimisticList.map(p => p.id === tempId ? saved : p);
            setPosts(updated);
            setOptimisticPosts(updated);
            setLocalChanges(prev => {
                const filtered = prev.filter(p => p.id !== tempId);
                return [...filtered, saved];
            });
        } catch (err) {
            console.error('Ошибка при добавлении:', err);
            const reverted = posts.filter(p => p.id !== tempId);
            setPosts(reverted);
            setOptimisticPosts(reverted);
        }
    };

    const handleEditPost = async (editedPost) => {
        const { id } = editedPost;
        const prevPosts = [...posts];

        const updated = posts.map(p => p.id === id ? { ...p, ...editedPost } : p);
        setPosts(updated);
        setOptimisticPosts(updated);
        setLocalChanges(prev => {
            const filtered = prev.filter(p => p.id !== id);
            return [...filtered, editedPost];
        });

        try {
            const res = await fetch(`https://jsonplaceholder.typicode.com/posts/${id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(editedPost)
            });

            if (!res.ok) throw new Error("PATCH failed");
        } catch (err) {
            console.error('Ошибка при редактировании:', err);
            setPosts(prevPosts);
            setOptimisticPosts(prevPosts);
        }
    };

    const handleDelete = async () => {
        if (selectedIds.length === 0) return;

        const prevPosts = [...posts];
        const updated = posts.filter(post => !selectedIds.includes(post.id));
        setPosts(updated);
        setOptimisticPosts(updated);
        setLocalChanges(prev => prev.filter(post => !selectedIds.includes(post.id)));

        try {
            await Promise.all(
                selectedIds.map(id =>
                    fetch(`https://jsonplaceholder.typicode.com/posts/${id}`, {
                        method: 'DELETE'
                    })
                )
            );
        } catch (err) {
            console.error('Ошибка при удалении:', err);
            setPosts(prevPosts);
            setOptimisticPosts(prevPosts);
        }

        setSelectedIds([]);
    };

    const handleStartEdit = () => {
        if (selectedIds.length !== 1) {
            alert("Выберите один пост для редактирования!");
            return;
        }

        const postToEdit = posts.find(p => p.id === selectedIds[0]);
        if (postToEdit) {
            setFormData(postToEdit);
            setIsModalOpen(true);
        }
    };

    const handleModalSubmit = (post) => {
        const postWithId = formData ? { ...post, id: formData.id } : post;

        if (formData) {
            handleEditPost(postWithId);
        } else {
            handleAddPost(postWithId);
        }

        setIsModalOpen(false);
        setFormData(null);
    };

    const postFields = [
        { key: 'userId', label: 'ID пользователя', placeholder: 'Введите userId' },
        { key: 'title', label: 'Заголовок', placeholder: 'Введите заголовок' },
        { key: 'body', label: 'Текст', placeholder: 'Введите текст' }
    ];

    return (
        <div>
            <h1 className="text-xl font-bold mb-4">Posts</h1>
            <div style={{ display: "flex", justifyContent: "end", gap: "15px", marginBottom: "15px" }}>
                <Button onClick={() => setIsModalOpen(true)} className={styles.comment_add__btn}>Добавить пост</Button>
                <Button onClick={handleStartEdit} className={styles.comment_add__btn}>Редактировать</Button>
                <Button onClick={handleDelete} className={styles.cooment_delete__btn}>Удалить</Button>
            </div>
            <AddCommentModal
                isOpen={isModalOpen}
                onClose={() => { setIsModalOpen(false); setFormData(null); }}
                onSubmit={handleModalSubmit}
                defaultData={formData}
                fields={postFields}
                titles={'пост'}
            >
                <PostForm
                    fields={postFields}
                    defaultData={formData}
                    onSubmit={handleModalSubmit}
                />
            </AddCommentModal>
            <div className="mt-4">
                <DataList
                    data={optimisticPosts}
                    columns={["userId", "id", "title", "body"]}
                    onSelect={setSelectedIds}
                />
            </div>
        </div>
    );
}
