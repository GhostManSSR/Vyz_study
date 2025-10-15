export interface UserFormData {
    name: string;
    email: string;
    role: 'Admin' | 'User' | 'Manager';
    department: string;
}
