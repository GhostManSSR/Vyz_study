import { forwardRef } from "react";

import { Button } from '@chakra-ui/react';
import { useColorMode } from "@/components/ui/color-mode";

export const ColorModeSwitcher: React.FC = () => {
    const { colorMode, toggleColorMode } = useColorMode();
    return (
        <Button
            aria-label="Toggle color mode"
            onClick={toggleColorMode}
            variant="ghost"
        >
            {colorMode === "light" ? "Switch to Dark" : "Switch to Light"}
        </Button>
    );
};

