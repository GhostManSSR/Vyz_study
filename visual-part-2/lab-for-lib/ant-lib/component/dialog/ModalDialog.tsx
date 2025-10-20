import * as Dialog from '@radix-ui/react-dialog';
import { ReactNode } from 'react';


import * as React from "react";
import { Cross2Icon } from "@radix-ui/react-icons";


const ModalDialog: React.FC = () => (
    <Dialog.Root>
<Dialog.Trigger asChild>
<button className="px-4 py-2 bg-purple-600 text-white rounded">
    Edit profile
</button>
</Dialog.Trigger>
<Dialog.Portal>
    <Dialog.Overlay className="fixed inset-0 bg-black/50" />
    <Dialog.Content className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white p-6 rounded shadow-lg max-w-md focus:outline-none">
        <Dialog.Title className="text-lg font-bold mb-4">Edit profile</Dialog.Title>
        <Dialog.Description className="mb-4 text-gray-600">
            Make changes to your profile here. Click save when you're done.
        </Dialog.Description>
        {/* остальное */}
        <Dialog.Close asChild>
            <button className="mt-4 px-3 py-2 bg-green-600 text-white rounded hover:bg-green-700">
                Save changes
            </button>
        </Dialog.Close>
        <Dialog.Close asChild>
            <button className="absolute top-3 right-3 rounded p-1 hover:bg-gray-200" aria-label="Close">
                <Cross2Icon />
            </button>
        </Dialog.Close>
    </Dialog.Content>
</Dialog.Portal>
</Dialog.Root>


);

export default ModalDialog;