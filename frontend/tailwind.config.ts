import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#1f2937",
        skyline: "#1e3a8a",
        accent: "#f97316"
      }
    }
  },
  plugins: []
} satisfies Config;

