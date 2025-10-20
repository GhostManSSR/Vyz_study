import * as Tabs from '@radix-ui/react-tabs';
import React from 'react';

export interface Tab {
    id: string;
    title: string;
    content: React.ReactNode;
    disabled?: boolean;
}

interface TabsProps {
    tabs: Tab[];
    defaultValue?: string;
    onTabChange?: (value: string) => void;
}

const TabsComponent: React.FC<TabsProps> = ({ tabs, defaultValue, onTabChange }) => {
    return (
        <Tabs.Root
            defaultValue={defaultValue ?? tabs[0]?.id}
            onValueChange={(value) => onTabChange && onTabChange(value)}
        >
            <Tabs.List className="flex border-b border-gray-300">
                {tabs.map(({ id, title, disabled }) => (
                    <Tabs.Trigger
                        key={id}
                        value={id}
                        disabled={disabled}
                        className={`px-4 py-2 -mb-[1px] border-b-2 ${
                            disabled
                                ? 'text-gray-400 cursor-not-allowed'
                                : 'border-transparent hover:border-blue-500'
                        }`}
                    >
                        {title}
                    </Tabs.Trigger>
                ))}
            </Tabs.List>
            {tabs.map(({ id, content }) => (
                <Tabs.Content key={id} value={id} className="p-4">
                    {content}
                </Tabs.Content>
            ))}
        </Tabs.Root>
    );
};

export default TabsComponent;
