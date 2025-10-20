import React from "react";
import { Modal } from "@/component/modals/Modal";
import { UserForm } from "@/component/modals/UserForm";
import { useModal } from "../../hooks/useModal";
import { UserFormData } from "../../types/UserFormData";
import {Button} from "@chakra-ui/react";
import {Layout} from "@/component/layout";

function App() {
    const { isOpen, open, close } = useModal();

    const handleSubmit = (data: UserFormData) => {
        console.log("User data:", data);
        close();
    };

    return (
        <>
            <Button onClick={open} style={{margin:"15px 15px"}}>Add User</Button>
            <Modal isOpen={isOpen} onClose={close}>
                <h2>Add User</h2>
                <UserForm onSubmit={handleSubmit} />
            </Modal>
        </>
    );
}

export default App;
