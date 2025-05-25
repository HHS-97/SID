import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { NodeGlobalsPolyfillPlugin } from "@esbuild-plugins/node-globals-polyfill";
import rollupNodePolyFill from "rollup-plugin-polyfill-node";
import { defineConfig, loadEnv } from "vite";

// https://vite.dev/config/
// Vite config는 함수로 내보내야 하며, mode를 통해 .env 파일을 로드할 수 있습니다.
export default defineConfig(({ mode }) => {
	// process.cwd()는 프로젝트 루트 디렉토리를 나타냅니다.
	const env = loadEnv(mode, process.cwd());
	return {
		plugins: [react(), tailwindcss()],
		server: {
		host: "0.0.0.0",
		port: 3000,
		strictPort: true,
		allowedHosts: ["sidproject.duckdns.org", "i12c110.p.ssafy.io"],
		proxy: {
			"/api/chat-ws": {
			target: env.VITE_BASE_URL, // 이제 환경 변수를 올바르게 불러옵니다.
			ws: true,
			changeOrigin: true,
			},
		},
		},
		optimizeDeps: {
		esbuildOptions: {
			define: {
			global: "globalThis",
			},
			plugins: [
			NodeGlobalsPolyfillPlugin({
				buffer: true,
			}),
			],
		},
		},
		build: {
		rollupOptions: {
			plugins: [rollupNodePolyFill()],
		},
		},
		define: {
		global: {},
		},
	};
});
