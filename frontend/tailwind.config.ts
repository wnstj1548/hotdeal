import type { Config } from "tailwindcss";

export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#1d1d1f",
        body: "#1d1d1f",
        primary: "#0066cc",
        primaryFocus: "#0071e3",
        primaryOnDark: "#2997ff",
        canvas: "#ffffff",
        parchment: "#f5f5f7",
        pearl: "#fafafc",
        tile1: "#272729",
        tile2: "#2a2a2c",
        tile3: "#252527",
        hairline: "#e0e0e0",
        dividerSoft: "#f0f0f0"
      }
    }
  },
  plugins: []
} satisfies Config;
