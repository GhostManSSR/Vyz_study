import React from "react";
import { useForm } from "react-hook-form";
import { UserFormData } from "../../types/UserFormData";
import { Box, Input, Button } from '@chakra-ui/react';
import { Select } from "@chakra-ui/select";
import { FormControl, FormLabel, FormErrorMessage } from "@chakra-ui/form-control";

interface UserFormProps {
    onSubmit: (data: UserFormData) => void;
}

export function UserForm({ onSubmit }: UserFormProps) {
    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<UserFormData>();

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <FormControl isInvalid={!!errors.name} mb={4}>
                <FormLabel fontSize="sm" fontWeight="bold" color="fg">Name</FormLabel>
                <Input
                    {...register("name", { required: "Name is required" })}
                    borderColor="inputBorder"
                    bg="inputBg"
                    _focus={{ borderColor: 'inputFocusBorder' }}
                    fontSize="md"
                    borderRadius="sm"
                />
                <FormErrorMessage>{errors.name?.message}</FormErrorMessage>
            </FormControl>

            <FormControl isInvalid={!!errors.email} mb={4}>
                <FormLabel fontSize="sm" fontWeight="bold" color="fg">Email</FormLabel>
                <Input
                    type="email"
                    {...register("email", { required: "Email is required" })}
                    borderColor="inputBorder"
                    bg="inputBg"
                    _focus={{ borderColor: 'inputFocusBorder' }}
                    fontSize="md"
                    borderRadius="sm"
                />
                <FormErrorMessage>{errors.email?.message}</FormErrorMessage>
            </FormControl>

            <FormControl isInvalid={!!errors.role} mb={4}>
                <FormLabel fontSize="sm" fontWeight="bold" color="fg">Role</FormLabel>
                <Select
                    {...register("role", { required: "Role is required" })}
                    borderColor="inputBorder"
                    bg="inputBg"
                    _focus={{ borderColor: 'inputFocusBorder' }}
                    fontSize="md"
                    borderRadius="sm"
                    placeholder="Select..."
                >
                    <option value="User">User</option>
                    <option value="Admin">Admin</option>
                    <option value="Manager">Manager</option>
                </Select>
                <FormErrorMessage>{errors.role?.message}</FormErrorMessage>
            </FormControl>

            <FormControl isInvalid={!!errors.department} mb={4}>
                <FormLabel fontSize="sm" fontWeight="bold" color="fg">Department</FormLabel>
                <Input
                    {...register("department", { required: "Department is required" })}
                    borderColor="inputBorder"
                    bg="inputBg"
                    _focus={{ borderColor: 'inputFocusBorder' }}
                    fontSize="md"
                    borderRadius="sm"
                />
                <FormErrorMessage>{errors.department?.message}</FormErrorMessage>
            </FormControl>

            <Box>
                <Button type="submit" disabled={isSubmitting}>Submit</Button>
            </Box>
        </form>
    );
}
