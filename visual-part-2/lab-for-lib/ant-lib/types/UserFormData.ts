export interface UserFormData {
    name: string;
    email: string;
    role: 'Admin' | 'User' | 'Manager';
    department: string;
}

export interface User {
    id: number;
    name: string;
    role: string;
    isActive: boolean;
}