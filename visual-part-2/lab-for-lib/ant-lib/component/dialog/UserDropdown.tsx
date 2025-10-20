import * as DropdownMenu from '@radix-ui/react-dropdown-menu';
import React from 'react';

export interface DropdownItem {
    id: string;
    label: string;
    disabled?: boolean;
    onSelect: () => void;
}

interface UserDropdownProps {
    items: DropdownItem[];
}

const UserDropdown: React.FC<UserDropdownProps> = ({ items }) => (
    <DropdownMenu.Root>
        <DropdownMenu.Trigger className="px-3 py-1 bg-gray-800 text-white rounded">
            User Menu
        </DropdownMenu.Trigger>

        <DropdownMenu.Portal>
            <DropdownMenu.Content className="bg-white rounded shadow-lg py-1 min-w-[150px] focus:outline-none">
                {items.map(({ id, label, disabled, onSelect }) => (
                    <DropdownMenu.Item
                        key={id}
                        onSelect={onSelect}
                        disabled={disabled}
                        className={`px-4 py-2 cursor-pointer select-none
              ${disabled ? 'opacity-50 cursor-not-allowed' : 'hover:bg-gray-100'}
              data-[highlighted]:bg-gray-200`}
                    >
                        {label}
                    </DropdownMenu.Item>
                ))}
            </DropdownMenu.Content>
        </DropdownMenu.Portal>
    </DropdownMenu.Root>
);

export default UserDropdown;
