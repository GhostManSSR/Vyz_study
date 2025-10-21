import React from 'react';
import { UserForm } from '@/component/modals/UserForm';
import { useModal } from '../../hooks/useModal';
import { UserFormData } from '../../types/UserFormData';
import { Button } from '@chakra-ui/react';
import UserTable from '@/component/thems/UserTable';
import {User} from "@/types/UserFormData"

export default function App() {
    const { isOpen, open, close } = useModal();

    const [users, setUsers] = React.useState<User[]>([
        { id: 4, name: 'Arti', role: 'Admin', isActive: true },
    ]);

    return (
        <>
            <UserTable users={users} setUsers={setUsers} />
        </>
    );
}
