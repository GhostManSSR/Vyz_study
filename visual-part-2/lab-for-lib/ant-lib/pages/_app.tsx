import type { AppProps } from "next/app";
// import '@mui/x-data-grid/index.css';
import { ChakraProvider } from '@chakra-ui/react';
import { forwardRef } from 'react';
import {system} from "../theme"

export default function App({ Component, pageProps }: AppProps) {
  return (
      <ChakraProvider value={system}>
        <Component {...pageProps} />
      </ChakraProvider>
  );
}
