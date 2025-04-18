import { useEffect, useState } from 'react';
import DataList from '../components/layout/DataList/DataList';
import AddCommentModal from '../components/AddCommentModal';
import Button from '../components/Button';
import styles from "../assets/style/page/Loader.module.css";
import PostForm from "../components/forms/PostForm";

export default function UsersPage() {
    const [users, setUsers] = useState([]);
    const [localChanges, setLocalChanges] = useState([]);
    const [selectedIds, setSelectedIds] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formData, setFormData] = useState(null);

    const [optimisticUsers, setOptimisticUsers] = useState(users);

    const mergeData = (serverData, localChanges) => {
        const ids = new Set(localChanges.map(user => user.id));
        return [
            ...serverData.filter(user => !ids.has(user.id)),
            ...localChanges
        ];
    };

    const fetchUsers = async () => {
        try {
            const res = await fetch('https://jsonplaceholder.typicode.com/users');
            const data = await res.json();
            const merged = mergeData(data, localChanges);
            setUsers(merged);
            setOptimisticUsers(merged);
        } catch (err) {
            console.error('Ошибка загрузки пользователей:', err);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleAddUser = async (newUser) => {
        const tempId = Math.random().toString(36).substring(2, 9);
        const optimisticUser = { ...newUser, id: tempId };

        const optimisticList = [optimisticUser, ...users];
        setUsers(optimisticList);
        setOptimisticUsers(optimisticList);
        setLocalChanges(prev => [...prev, optimisticUser]);

        try {
            const res = await fetch('https://jsonplaceholder.typicode.com/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newUser)
            });

            if (!res.ok) throw new Error('POST failed');
            const saved = await res.json();

            const updated = optimisticList.map(u => u.id === tempId ? saved : u);
            setUsers(updated);
            setOptimisticUsers(updated);
            setLocalChanges(prev => {
                const filtered = prev.filter(u => u.id !== tempId);
                return [...filtered, saved];
            });
        } catch (err) {
            console.error('Ошибка при добавлении:', err);
            const reverted = users.filter(u => u.id !== tempId);
            setUsers(reverted);
            setOptimisticUsers(reverted);
        }
    };

    const handleEditUser = async (editedUser) => {
        const { id } = editedUser;
        const prevUsers = [...users];

        const updated = users.map(u => u.id === id ? { ...u, ...editedUser } : u);
        setUsers(updated);
        setOptimisticUsers(updated);
        setLocalChanges(prev => {
            const filtered = prev.filter(u => u.id !== id);
            return [...filtered, editedUser];
        });

        try {
            const res = await fetch(`https://jsonplaceholder.typicode.com/users/${id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(editedUser)
            });

            if (!res.ok) throw new Error("PATCH failed");
        } catch (err) {
            console.error('Ошибка при редактировании:', err);
            setUsers(prevUsers);
            setOptimisticUsers(prevUsers);
        }
    };

    const handleDelete = async () => {
        if (selectedIds.length === 0) return;

        const prevUsers = [...users];
        const updated = users.filter(user => !selectedIds.includes(user.id));
        setUsers(updated);
        setOptimisticUsers(updated);
        setLocalChanges(prev => prev.filter(user => !selectedIds.includes(user.id)));

        try {
            await Promise.all(
                selectedIds.map(id =>
                    fetch(`https://jsonplaceholder.typicode.com/users/${id}`, {
                        method: 'DELETE'
                    })
                )
            );
        } catch (err) {
            console.error('Ошибка при удалении:', err);
            setUsers(prevUsers);
            setOptimisticUsers(prevUsers);
        }

        setSelectedIds([]);
    };

    const handleStartEdit = () => {
        if (selectedIds.length !== 1) {
            alert("Выберите одного пользователя для редактирования!");
            return;
        }

        const userToEdit = users.find(u => u.id === selectedIds[0]);
        if (userToEdit) {
            setFormData(userToEdit);
            setIsModalOpen(true);
        }
    };

    const handleModalSubmit = (user) => {
        const userWithId = formData ? { ...user, id: formData.id } : user;

        if (formData) {
            handleEditUser(userWithId);
        } else {
            handleAddUser(userWithId);
        }

        setIsModalOpen(false);
        setFormData(null);
    };

    const userFields = [
        { key: 'name', label: 'Имя', placeholder: 'Введите имя' },
        { key: 'username', label: 'Username', placeholder: 'Введите username' },
        { key: 'email', label: 'Email', placeholder: 'Введите email' },
        { key: 'address.street', label: 'Улица', placeholder: 'Введите улицу' },
        { key: 'address.suite', label: 'Квартира/Офис', placeholder: 'Введите квартиру/офис' },
        { key: 'address.city', label: 'Город', placeholder: 'Введите город' },
        { key: 'address.zipcode', label: 'Почтовый индекс', placeholder: 'Введите почтовый индекс' },
        { key: 'company.name', label: 'Компания', placeholder: 'Введите компанию' },
        { key: 'company.catchPhrase', label: 'Фраза компании', placeholder: 'Введите фразу компании' },
        { key: 'phone', label: 'Телефон', placeholder: 'Введите телефон' },
        { key: 'website', label: 'Сайт', placeholder: 'Введите сайт' }
    ];

    return (
        <div>
            <h1 className="text-xl font-bold mb-4">Пользователи</h1>
            <div style={{ display: "flex", justifyContent: "end", gap: "15px", marginBottom: "15px" }}>
                <Button onClick={() => setIsModalOpen(true)} className={styles.comment_add__btn}>Добавить пользователя</Button>
                <Button onClick={handleStartEdit} className={styles.comment_add__btn}>Редактировать</Button>
                <Button onClick={handleDelete} className={styles.cooment_delete__btn}>Удалить</Button>
            </div>

            <AddCommentModal
                isOpen={isModalOpen}
                onClose={() => { setIsModalOpen(false); setFormData(null); }}
                onSubmit={handleModalSubmit}
                defaultData={formData}
                fields={userFields}
                titles={'пользователя'}
            >
                <PostForm
                    fields={userFields}
                    defaultData={formData}
                    onSubmit={handleModalSubmit}
                />
            </AddCommentModal>

            <div className="mt-4">
                <DataList
                    data={optimisticUsers}
                    columns={["id", "name", "username", "email", "phone", "website", "address", "company"]}
                    onSelect={setSelectedIds}
                    renderColumnCell={{
                        address: (address) => {
                            if (address && address.street) {
                                return `${address.street}, ${address.suite || ''}, ${address.city || ''}, ${address.zipcode || ''}`;
                            }
                            return 'Нет данных';
                        },
                        company: (company) => {
                            if (company) {
                                return `${company.name || ''} (${company.catchPhrase || ''})`;
                            }
                            return 'Нет данных';
                        },
                    }}
                />
            </div>
        </div>
    );
}
