import type { InputHTMLAttributes } from "react";
import { cn } from "./cn";

type InputProps = InputHTMLAttributes<HTMLInputElement>;

export function Input({ className, ...props }: InputProps) {
  return (
    <input
      className={cn(
        "h-11 rounded-xl border border-slate-300 px-3 text-sm outline-none transition focus:border-skyline focus:ring-2 focus:ring-skyline/15",
        className
      )}
      {...props}
    />
  );
}
