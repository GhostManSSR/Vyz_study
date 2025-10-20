import React from "react";
import { Box, Flex } from "@chakra-ui/react";
import { useColorModeValue } from "@/components/ui/color-mode";
import {ThemeToggle} from "@/component/modals/ThemeToggle";

interface LayoutProps {
    children: React.ReactNode;
}

export function Layout({ children }: LayoutProps) {
    const bg = useColorModeValue("bg", "bg");
    const fg = useColorModeValue("fg", "fg");

    return (
        <Flex direction="column" minHeight="100vh" bg={bg} color={fg}>
            <Box p={4} flexShrink={0}>
                <ThemeToggle></ThemeToggle>
            </Box>
            <Box flex="1" p={4}>
                {children}
            </Box>
            <Box p={4} flexShrink={0} textAlign="center" fontSize="sm" color="gray.500">
                © 2025 Company Name
            </Box>
        </Flex>
    );
}
