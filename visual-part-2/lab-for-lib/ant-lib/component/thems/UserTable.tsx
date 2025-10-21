import React, { useState, useEffect } from 'react';
import {
    DataGrid,
    GridColDef,
    GridRowSelectionModel,
    GridRowParams,
} from '@mui/x-data-grid';
import {
    Chip,
    Switch,
    Stack,
    TextField,
    Select,
    MenuItem,
    Box,
    Modal,
    IconButton,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import ThemedButton from './MyThemeButton';

interface User {
    id: number;
    name: string;
    role: string;
    isActive: boolean;
}

const modalStyle = {
    position: 'absolute' as 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 400,
    bgcolor: 'background.paper',
    borderRadius: 1,
    boxShadow: 24,
    p: 4,
};

const UserTable: React.FC<{
    users: User[];
    setUsers: React.Dispatch<React.SetStateAction<User[]>>;
}> = ({ users, setUsers }) => {
    const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>({
        type: 'include',
        ids: new Set<number>(),
    });

    const [modalOpen, setModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<User | null>(null);

    const [userName, setUserName] = useState('');
    const [userRole, setUserRole] = useState('User');
    const [userActive, setUserActive] = useState(true);

    useEffect(() => {
        if (editingUser) {
            setUserName(editingUser.name);
            setUserRole(editingUser.role);
            setUserActive(editingUser.isActive);
        } else {
            setUserName('');
            setUserRole('User');
            setUserActive(true);
        }
    }, [editingUser]);

    const columns: GridColDef<User>[] = [
        { field: 'name', headerName: 'Name', flex: 1 },
        {
            field: 'role',
            headerName: 'Role',
            flex: 1,
            renderCell: (params) => <Chip label={params.value} color="primary" size="small" />,
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
        {
            field: 'edit',
            headerName: 'Edit',
            sortable: false,
            filterable: false,
            width: 70,
            renderCell: (params: GridRowParams<User>) => (
                <IconButton
                    onClick={() => {
                        setEditingUser(params.row);
                        setModalOpen(true);
                    }}
                    size="small"
                    aria-label="Edit user"
                    title="Edit user"
                >
                    <EditIcon />
                </IconButton>
            ),
        },
    ];

    const handleSelectionChange = (newModel: GridRowSelectionModel) => {
        setSelectionModel({
            type: 'include',
            ids: new Set(newModel.ids),
        });
    };

    const closeModal = () => {
        setModalOpen(false);
        setEditingUser(null);
    };

    const handleSubmit = () => {
        if (!userName.trim()) return;
        if (editingUser) {
            const updatedUser: User = {
                ...editingUser,
                name: userName.trim(),
                role: userRole,
                isActive: userActive,
            };
            setUsers((prev) =>
                prev.map((user) => (user.id === updatedUser.id ? updatedUser : user)),
            );
        }
        closeModal();
    };

    const deleteSelectedUsers = () => {
        setUsers((prevUsers) =>
            prevUsers.filter((user) => !selectionModel.ids.has(user.id)),
        );
        setSelectionModel({ type: 'include', ids: new Set() });
    };

    return (
        <>
            <Stack direction="row" spacing={2} mb={1}>
                <ThemedButton
                    variant="primary"
                    onClick={() => {
                        setEditingUser(null);
                        setModalOpen(true);
                    }}
                >
                    Add User
                </ThemedButton>
                <ThemedButton
                    variant="danger"
                    onClick={deleteSelectedUsers}
                    disabled={selectionModel.ids.size === 0}
                >
                    Delete Selected
                </ThemedButton>
            </Stack>

            <Box sx={{ height: 450, width: '100%' }}>
                <DataGrid
                    rows={users}
                    columns={columns}
                    checkboxSelection
                    rowSelectionModel={selectionModel}
                    onRowSelectionModelChange={handleSelectionChange}
                    disableRowSelectionExcludeModel
                    getRowId={(row) => row.id}
                />
            </Box>

            <Modal
                open={modalOpen}
                onClose={closeModal}
                aria-labelledby="modal-title"
                aria-describedby="modal-description"
            >
                <Box sx={modalStyle}>
                    <h2 id="modal-title">{editingUser ? 'Edit User' : 'Add New User'}</h2>
                    <Stack spacing={2} mt={1}>
                        <TextField
                            label="Name"
                            value={userName}
                            onChange={(e) => setUserName(e.target.value)}
                            autoFocus
                            fullWidth
                        />
                        <Select
                            label="Role"
                            value={userRole}
                            onChange={(e) => setUserRole(e.target.value)}
                            fullWidth
                        >
                            <MenuItem value="User">User</MenuItem>
                            <MenuItem value="Admin">Admin</MenuItem>
                            <MenuItem value="Manager">Manager</MenuItem>
                        </Select>
                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Switch
                                checked={userActive}
                                onChange={(e) => setUserActive(e.target.checked)}
                            />
                            Active
                        </Stack>
                        <Stack direction="row" spacing={1} justifyContent="flex-end" mt={2}>
                            <ThemedButton variant="secondary" onClick={closeModal}>
                                Cancel
                            </ThemedButton>
                            <ThemedButton
                                variant="primary"
                                onClick={handleSubmit}
                                disabled={!userName.trim()}
                            >
                                Save Changes
                            </ThemedButton>
                        </Stack>
                    </Stack>
                </Box>
            </Modal>
        </>
    );
};

export default UserTable;
