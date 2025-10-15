import React from 'react';
import { forwardRef } from 'react';
import {
    FormControl,
    FormLabel,
    Input,
    Select,
    FormErrorMessage,
    Button,
    Stack,
} from '@chakra-ui/react';
import { useForm } from 'react-hook-form';
import { UserFormData } from '../../types/UserFormData';

interface UserFormProps {
    onSubmit: (data: UserFormData) => void;
}

export const UserForm: React.FC<UserFormProps> = ({ onSubmit }) => {
    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<UserFormData>({
        defaultValues: {
            name: '',
            email: '',
            role: 'User',
            department: '',
        },
    });

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={4}>
                <FormControl isInvalid={!!errors.name}>
                    <FormLabel htmlFor="name">Name</FormLabel>
                    <Input
                        id="name"
                        placeholder="Enter name"
                        {...register('name', { required: 'Name is required' })}
                    />
                    <FormErrorMessage>{errors.name && errors.name.message}</FormErrorMessage>
                </FormControl>

                <FormControl isInvalid={!!errors.email}>
                    <FormLabel htmlFor="email">Email</FormLabel>
                    <Input
                        id="email"
                        placeholder="Enter email"
                        type="email"
                        {...register('email', {
                            required: 'Email is required',
                            pattern: {
                                value: /^\S+@\S+$/i,
                                message: 'Invalid email address',
                            },
                        })}
                    />
                    <FormErrorMessage>{errors.email && errors.email.message}</FormErrorMessage>
                </FormControl>

                <FormControl isInvalid={!!errors.role}>
                    <FormLabel htmlFor="role">Role</FormLabel>
                    <Select id="role" {...register('role', { required: 'Role is required' })}>
                        <option value="User">User</option>
                        <option value="Admin">Admin</option>
                        <option value="Manager">Manager</option>
                    </Select>
                    <FormErrorMessage>{errors.role && errors.role.message}</FormErrorMessage>
                </FormControl>

                <FormControl isInvalid={!!errors.department}>
                    <FormLabel htmlFor="department">Department</FormLabel>
                    <Input
                        id="department"
                        placeholder="Enter department"
                        {...register('department', { required: 'Department is required' })}
                    />
                    <FormErrorMessage>
                        {errors.department && errors.department.message}
                    </FormErrorMessage>
                </FormControl>

                <Button isLoading={isSubmitting} type="submit" colorScheme="blue">
                    Add User
                </Button>
            </Stack>
        </form>
    );
};
