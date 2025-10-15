import React, { ReactNode } from 'react';
import { forwardRef } from 'react';
import {
    Modal,
    ModalOverlay,
    ModalContent,
    ModalHeader,
    ModalCloseButton,
    ModalBody,
    ModalFooter,
    Button,
} from '@chakra-ui/react';


interface ModalWrapperProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: ReactNode;
    onConfirm?: () => void;
    confirmText?: string;
}

export const ModalWrapper: React.FC<ModalWrapperProps> = ({
                                                              isOpen,
                                                              onClose,
                                                              title,
                                                              children,
                                                              onConfirm,
                                                              confirmText = 'Confirm',
                                                          }) => (
    <Modal isOpen={isOpen} onClose={onClose} isCentered>
        <ModalOverlay />
        <ModalContent>
            <ModalHeader>{title}</ModalHeader>
            <ModalCloseButton />
            <ModalBody>{children}</ModalBody>
            {onConfirm && (
                <ModalFooter>
                    <Button variant="ghost" mr={3} onClick={onClose}>
                        Cancel
                    </Button>
                    <Button colorScheme="blue" onClick={onConfirm}>
                        {confirmText}
                    </Button>
                </ModalFooter>
            )}
        </ModalContent>
    </Modal>
);
