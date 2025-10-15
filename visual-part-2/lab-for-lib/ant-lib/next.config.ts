import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  transpilePackages: ['@mui/x-data-grid'],
};

export default nextConfig;
