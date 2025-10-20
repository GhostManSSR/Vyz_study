import { ChakraProvider } from "@chakra-ui/react";
import { ThemeProvider } from 'next-themes';
import { ColorModeProvider, useColorMode } from '@/components/ui/color-mode';
import {system} from '../theme';
import "../styles/globals.css"
import {Layout} from "@/component/layout/index";
import type { AppProps } from 'next/app';

function MyApp({ Component, pageProps }: AppProps) {
    return (
        <ChakraProvider value={system}>
            <ColorModeProvider options={{ initialColorMode: "light", useSystemColorMode: false }}>
                <Layout>
                    <Component {...pageProps} />
                </Layout>
            </ColorModeProvider>
        </ChakraProvider>
    );
}

export default MyApp;
