import React, { useState } from 'react';
import {
    DataGrid,
    GridColDef,
    GridRowParams,
} from '@mui/x-data-grid';
import {GridRowSelectionModel } from '@mui/x-data-grid';
import {
    Chip,
    Switch,
    Stack,
    TextField,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Select,
    MenuItem,
    Box,
} from '@mui/material';
import ThemedButton from './MyThemeButton';

interface User {
    id: number;
    name: string;
    role: string;
    isActive: boolean;
}

const UserTable: React.FC<{ initialUsers: User[] }> = ({ initialUsers }) => {
    const [users, setUsers] = useState<User[]>(initialUsers);
    const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>({
        type: 'include',
        ids: new Set<number>(),
    });
    const [dialogOpen, setDialogOpen] = useState(false);
    const [newUserName, setNewUserName] = useState('');
    const [newUserRole, setNewUserRole] = useState('User');
    const [newUserActive, setNewUserActive] = useState(true);

    console.log(selectionModel)

    const columns: GridColDef<User>[] = [
        { field: 'name', headerName: 'Name', flex: 1 },
        {
            field: 'role',
            headerName: 'Role',
            flex: 1,
            renderCell: (params) => (
                <Chip label={params.value} color="primary" size="small" />
            ),
        },
        {
            field: 'isActive',
            headerName: 'Active',
            flex: 1,
            renderCell: (params) => {
                const handleToggle = () => {
                    setUsers((prev) =>
                        prev.map((user) =>
                            user.id === params.row.id ? { ...user, isActive: !user.isActive } : user,
                        ),
                    );
                };
                return (
                    <Switch
                        checked={Boolean(params.value)}
                        onChange={handleToggle}
                        color="primary"
                        inputProps={{ 'aria-label': 'activation status' }}
                    />
                );
            },
        },
    ];

    const handleSelectionChange = (newSelectionModel: GridRowSelectionModel) => {
        setSelectionModel({
            type: 'include',
            ids: new Set<number>(newSelectionModel.ids), // newSelectionModel.ids is an iterable array
        });
    };

    const openAddDialog = () => {
        setNewUserName('');
        setNewUserRole('User');
        setNewUserActive(true);
        setDialogOpen(true);
    };

    const closeAddDialog = () => setDialogOpen(false);

    const addUser = () => {
        if (!newUserName.trim()) return;
        const maxId = users.reduce((max, u) => (u.id > max ? u.id : max), 0);
        const newUser: User = {
            id: maxId + 1,
            name: newUserName.trim(),
            role: newUserRole,
            isActive: newUserActive,
        };
        setUsers([...users, newUser]);
        setDialogOpen(false);
    };

    const deleteSelectedUsers = () => {
        if (!selectionModel.ids) return;
        setUsers((prevUsers) =>
            prevUsers.filter((user) => !selectionModel.ids.has(user.id))
        );
        setSelectionModel({ type: 'include', ids: new Set() });
    };

    return (
        <>
            <Stack direction="row" spacing={2} mb={1}>
                <ThemedButton variant="primary" onClick={openAddDialog}>
                    Add User
                </ThemedButton>
                <ThemedButton
                    variant="danger"
                    onClick={deleteSelectedUsers}
                    // disabled={selectionModel.length === 0}
                >
                    Delete Selected
                </ThemedButton>
            </Stack>

            <Box sx={{ height: 450, width: '100%' }}>
                <DataGrid
                    rows={users}
                    columns={columns}
                    rowSelectionModel={selectionModel}
                    onRowSelectionModelChange={handleSelectionChange}
                    disableRowSelectionExcludeModel
                    checkboxSelectionv
                    getRowId={(row) => row.id}
                />
            </Box>

            <Dialog open={dialogOpen} onClose={closeAddDialog}>
                <DialogTitle>Add New User</DialogTitle>
                <DialogContent>
                    <Stack spacing={2} mt={1} minWidth={300}>
                        <TextField
                            label="Name"
                            value={newUserName}
                            onChange={(e) => setNewUserName(e.target.value)}
                            autoFocus
                            fullWidth
                        />
                        <Select
                            label="Role"
                            value={newUserRole}
                            onChange={(e) => setNewUserRole(e.target.value)}
                            fullWidth
                        >
                            <MenuItem value="User">User</MenuItem>
                            <MenuItem value="Admin">Admin</MenuItem>
                            <MenuItem value="Manager">Manager</MenuItem>
                        </Select>
                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Switch
                                checked={newUserActive}
                                onChange={(e) => setNewUserActive(e.target.checked)}
                            />
                            Active
                        </Stack>
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <ThemedButton variant="secondary" onClick={closeAddDialog}>
                        Cancel
                    </ThemedButton>
                    <ThemedButton
                        variant="primary"
                        onClick={addUser}
                        disabled={!newUserName.trim()}
                    >
                        Add
                    </ThemedButton>
                </DialogActions>
            </Dialog>
        </>
    );
};

export default UserTable;
