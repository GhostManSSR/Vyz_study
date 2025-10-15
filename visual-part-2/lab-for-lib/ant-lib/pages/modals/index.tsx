import { Box, Flex } from '@chakra-ui/react';
import React, { ReactNode } from 'react';
import {
    ColorModeButton,
    DarkMode,
    LightMode,
    useColorMode,
    useColorModeValue,
} from "@/components/ui/color-mode"
import { ColorModeSwitcher } from '../../component/modals/ColorModeSwitcher';
import {UserForm} from "@/component/modals/UserForm";

interface LayoutProps {
    children: ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
    const bgColor = useColorModeValue('gray.50', 'gray.900');

    const onSubmit = () => {
        console.log('onSubmit');
    }

    return (
        <Flex direction="column" minHeight="100vh" bg={bgColor}>
            <Box as="header" p={4} boxShadow="md">
                <ColorModeSwitcher />
            </Box>

            {/*<Box>*/}
            {/*    <UserForm onSubmit={onSubmit}></UserForm>*/}
            {/*</Box>*/}

            {/*<Box as="main" flex="1" p={4}>*/}
            {/*    {children}*/}
            {/*</Box>*/}

            <Box as="footer" p={4} textAlign="center" fontSize="sm" color="gray.500">
                © 2025 Company Name
            </Box>
        </Flex>
    );
};

export default Layout;
