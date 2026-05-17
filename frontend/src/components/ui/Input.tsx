import type { InputHTMLAttributes } from "react";
import { cn } from "./cn";

type InputProps = InputHTMLAttributes<HTMLInputElement>;

export function Input({ className, ...props }: InputProps) {
  return (
    <input
      className={cn(
        "apple-focus-ring h-11 rounded-full border border-black/10 bg-canvas px-4 text-[15px] text-ink outline-none transition placeholder:text-[#6e6e73] focus:border-primary",
        className
      )}
      {...props}
    />
  );
}
