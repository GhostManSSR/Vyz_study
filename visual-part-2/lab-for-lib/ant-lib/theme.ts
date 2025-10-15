// theme.ts
import { createSystem, defaultConfig, defineConfig } from "@chakra-ui/react";

const colors = {
    brand: {
        50: '#e3f2f9',
        100: '#c5e4f3',
        200: '#a2d4ec',
        300: '#7ac1e4',
        400: '#47a9da',
        500: '#0088cc',
        600: '#007ab8',
        700: '#006ba1',
        800: '#005885',
        900: '#003f5e',
    },
};

const config = defineConfig({
    theme: {
        tokens: {
            colors: {
                50: {value: '#e3f2f9'},
                100: {value: '#c5e4f3'},
                200: {value: '#a2d4ec'},
                300: {value: '#7ac1e4'},
                400: {value: '#47a9da'},
                500: {value: '#0088cc'},
                600: {value: '#007ab8'},
                700: {value: '#006ba1'},
                800: {value: '#005885'},
                900: {value: '#003f5e'},
            },
        },
    },
})

// const theme = createSystem({
//     ...defaultConfig,
//     colors,
//     styles: {
//         global: (props: any) => ({
//             body: {
//                 bg: props.colorMode === 'dark' ? 'gray.800' : 'gray.50',
//                 color: props.colorMode === 'dark' ? 'whiteAlpha.900' : 'gray.800',
//             },
//         }),
//     },
// });

export const system = createSystem(defaultConfig, config)
