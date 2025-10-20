import React, { useState } from "react";
import UserDropdown from "@/component/dialog/UserDropdown";
import ModalDialog  from "@/component/dialog/ModalDialog";
import TabsComponent  from "@/component/dialog/TabsComponent";
import {Tab}  from "@/component/dialog/TabsComponent";
import { DropdownItem } from "@/component/dialog/UserDropdown"
import {Button} from "@chakra-ui/react";

const Dialog: React.FC = () => {
    const [isDialogOpen, setDialogOpen] = useState(false);

    const dropdownItems: DropdownItem[] = [
        { id: "profile", label: "Profile", onSelect: () => alert("Profile selected") },
        { id: "settings", label: "Settings", onSelect: () => alert("Settings selected") },
        { id: "logout", label: "Logout", onSelect: () => alert("Logout selected"), disabled: false },
    ];

    const tabsData: Tab[] = [
        { id: "tab1", title: "Home", content: <p>Welcome to the home tab!</p> },
        { id: "tab2", title: "Profile", content: <p>User profile content here.</p> },
        { id: "tab3", title: "Settings", content: <p>Settings content.</p>, disabled: true },
    ];

    const handleTabChange = (value: string) => {
        console.log("Tab changed to:", value);
    };

    return (
        <div className="p-8 space-y-8 max-w-3xl mx-auto">
            <h1 className="text-2xl font-bold mb-4">Radix UI + Tailwind CSS Components</h1>

            <section>
                <h2 className="text-xl font-semibold mb-2">Dialog</h2>
                <ModalDialog></ModalDialog>
            </section>

            <section>
                <h2 className="text-xl font-semibold mb-2">User Dropdown</h2>
                <UserDropdown items={dropdownItems} />
            </section>

            <section>
                <h2 className="text-xl font-semibold mb-2">Tabs Component</h2>
                <TabsComponent tabs={tabsData} onTabChange={handleTabChange} />
            </section>
        </div>
    );
};

export default Dialog;