import { useColorMode } from "@/components/ui/color-mode";
import { Button } from "@chakra-ui/react";
import {useRouter} from "next/router";

export function ThemeToggle() {
    const { colorMode, toggleColorMode } = useColorMode();
    const router = useRouter();

    return (
        <div style={{ width: "100%", display: "flex", justifyContent: "space-between" }}>
            <Button onClick={toggleColorMode} size="sm" aria-label="Toggle theme">
                Switch to {colorMode === "dark" ? "Light" : "Dark"} Mode
            </Button>
            <Button onClick={() => router.back()}>
                Back
            </Button>
        </div>
    );
}
