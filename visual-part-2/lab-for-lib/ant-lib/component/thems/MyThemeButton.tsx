import React from 'react';
import { createTheme, ThemeProvider, Button, ButtonProps, CircularProgress } from '@mui/material';
import { red } from '@mui/material/colors';

const theme = createTheme({
    palette: {
        danger: {
            main: red[700],
            contrastText: '#fff',
        },
    },
});

type Variant = 'primary' | 'secondary' | 'danger';
type Size = 'small' | 'medium' | 'large';

interface MyButtonProps extends ButtonProps {
    variant?: Variant;
    loading?: boolean;
}

const MyButton: React.FC<MyButtonProps> = ({
                                               variant = 'primary',
                                               loading = false,
                                               disabled = false,
                                               children,
                                               ...props
                                           }) => {
    const colorMap: Record<Variant, ButtonProps['color'] | 'danger'> = {
        primary: 'primary',
        secondary: 'secondary',
        danger: 'danger',
    };

    return (
        <Button
            variant="contained"
            color={colorMap[variant]}
            disabled={disabled || loading}
            {...props}
        >
            {loading ? <CircularProgress size={24} color="inherit" /> : children}
        </Button>
    );
};

const ThemedButton: React.FC<MyButtonProps> = (props) => (
    <ThemeProvider theme={theme}>
        <MyButton {...props} />
    </ThemeProvider>
);

export default ThemedButton;
