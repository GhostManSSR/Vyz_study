import { useColorMode } from "@/components/ui/color-mode";
import { Button } from "@chakra-ui/react";

export function ThemeToggle() {
    const { colorMode, toggleColorMode } = useColorMode();

    return (
        <Button onClick={toggleColorMode} size="sm" aria-label="Toggle theme">
            Switch to {colorMode === "dark" ? "Light" : "Dark"} Mode
        </Button>
    );
}
