declare module 'next-with-less' {
    import { NextConfig } from 'next';
    function withLess(config: NextConfig): NextConfig;
    export default withLess;
}
