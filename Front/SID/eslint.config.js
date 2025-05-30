import js from "@eslint/js"
import prettierConfig from "eslint-config-prettier"
import prettier from "eslint-plugin-prettier" // prettierPlugin → prettier
import react from "eslint-plugin-react"
import reactHooks from "eslint-plugin-react-hooks"
import reactRefresh from "eslint-plugin-react-refresh"
import unusedImports from "eslint-plugin-unused-imports"
import globals from "globals"

export default [
	{
		ignores: ["dist"],
	},
	{
		files: ["**/*.{js,jsx}"],
		languageOptions: {
			ecmaVersion: 2020,
			globals: globals.browser,
			parserOptions: {
				ecmaVersion: "latest",
				ecmaFeatures: { jsx: true },
				sourceType: "module",
			},
		},
		settings: {
			react: {
				version: "18.3",
			},
		},
		plugins: {
			react,
			"react-hooks": reactHooks,
			"react-refresh": reactRefresh,
			prettier, // prettierPlugin → prettier
			"unused-imports": unusedImports,
		},
		rules: {
			...js.configs.recommended.rules,
			...react.configs.recommended.rules,
			...react.configs["jsx-runtime"].rules,
			...reactHooks.configs.recommended.rules,
			"prettier/prettier": ["warn", { endOfLine: "auto" }],
			...prettierConfig.rules,
			"react/jsx-no-target-blank": "off",
			"react-refresh/only-export-components": ["warn", { allowConstantExport: true }],
			"react/prop-types": "off",
			"unused-imports/no-unused-imports": "error",
			"unused-imports/no-unused-vars": [
				"warn",
				{
					vars: "all",
					varsIgnorePattern: "^_",
					args: "after-used",
					argsIgnorePattern: "^_",
				},
			], // 추가: 사용되지 않는 변수 경고 설정
			"no-unused-vars": "off",
		},
	},
]
