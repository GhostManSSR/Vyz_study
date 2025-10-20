import {
    Modal,
    ModalOverlay,
    ModalContent,
    ModalHeader,
    ModalCloseButton,
    ModalBody,
    ModalFooter,
    Button,
    FormControl,
    FormLabel,
    Input,
    Select,
    FormErrorMessage,
    useDisclosure,
} from '@chakra-ui/react';
import { useForm } from 'react-hook-form';
import React from 'react';
import { UserFormData } from '../../types/UserFormData';

interface UserModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (data: UserFormData) => void;
}

export const UserModal: React.FC<UserModalProps> = ({ isOpen, onClose, onSubmit }) => {
    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<UserFormData>();

    return (
        <Modal isOpen={isOpen} onClose={onClose} isCentered size="lg">
            <ModalOverlay />
            <ModalContent>
                <ModalHeader>Add User</ModalHeader>
                <ModalCloseButton />
                <ModalBody>
                    <form id="user-form" onSubmit={handleSubmit(onSubmit)}>
                        <FormControl isInvalid={!!errors.name} isRequired mb={4}>
                            <FormLabel>Name</FormLabel>
                            <Input {...register('name', { required: 'Name is required' })} />
                            <FormErrorMessage>{errors.name?.message}</FormErrorMessage>
                        </FormControl>

                        <FormControl isInvalid={!!errors.email} isRequired mb={4}>
                            <FormLabel>Email</FormLabel>
                            <Input
                                type="email"
                                {...register('email', {
                                    required: 'Email is required',
                                    pattern: { value: /^\S+@\S+$/i, message: 'Invalid email address' },
                                })}
                            />
                            <FormErrorMessage>{errors.email?.message}</FormErrorMessage>
                        </FormControl>

                        <FormControl isInvalid={!!errors.role} isRequired mb={4}>
                            <FormLabel>Role</FormLabel>
                            <Select {...register('role', { required: 'Role is required' })} defaultValue="User">
                                <option value="User">User</option>
                                <option value="Admin">Admin</option>
                                <option value="Manager">Manager</option>
                            </Select>
                            <FormErrorMessage>{errors.role?.message}</FormErrorMessage>
                        </FormControl>

                        <FormControl isInvalid={!!errors.department} isRequired mb={4}>
                            <FormLabel>Department</FormLabel>
                            <Input {...register('department', { required: 'Department is required' })} />
                            <FormErrorMessage>{errors.department?.message}</FormErrorMessage>
                        </FormControl>
                    </form>
                </ModalBody>
                <ModalFooter>
                    <Button colorScheme="blue" mr={3} isLoading={isSubmitting} type="submit" form="user-form">
                        Add User
                    </Button>
                    <Button variant="ghost" onClick={onClose}>
                        Cancel
                    </Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
};
