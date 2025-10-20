import { createSystem, defaultConfig, defineConfig, defineTokens } from "@chakra-ui/react";

const tokens = defineTokens({
    colors: {
        fgLight: { value: "#1A202C" },        // dark gray для light mode
        bgLight: { value: "#FFFFFF" },        // белый для light mode
        inputBorderLight: { value: "#CBD5E0" },
        inputBgLight: { value: "#FFFFFF" },
        inputFocusBorderLight: { value: "#3182CE" },

        fgDark: { value: "#EDF2F7" },          // светлый для dark mode
        bgDark: { value: "#1A202C" },          // темный фон dark mode
        inputBorderDark: { value: "#4A5568" },
        inputBgDark: { value: "#2D3748" },
        inputFocusBorderDark: { value: "#63B3ED" },
    },
    fontSizes: {
        sm: { value: "14px" },
        md: { value: "16px" },
    },
    space: {
        2: { value: "8px" },
        3: { value: "12px" },
        4: { value: "16px" },
    },
    radii: {
        sm: { value: "4px" },
        md: { value: "6px" },
    },
    fontWeights: {
        normal: { value: "400" },
        bold: { value: "700" },
    },
});

const config = defineConfig({
    theme: {
        tokens,
    },
});

export const system = createSystem(defaultConfig, config);
