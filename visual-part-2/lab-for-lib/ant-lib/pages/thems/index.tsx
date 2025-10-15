import React from 'react';
import ThemedButton from '../../component/thems/MyThemeButton';
import UserTable from '../../component/thems/UserTable';

const users = [
    { id: 1, name: 'Alice', role: 'Admin', isActive: true },
    { id: 2, name: 'Bob', role: 'User', isActive: false },
];

export default function Thems() {
    return (
        <>
            <UserTable
                initialUsers={users}
                onRowClick={(user) => console.log('Row clicked:', user)}
                onSelectionChange={(ids) => console.log('Selected IDs:', ids)}
            />
        </>
    );
}
