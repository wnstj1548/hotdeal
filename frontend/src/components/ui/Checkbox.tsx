import type { InputHTMLAttributes } from "react";
import { cn } from "./cn";

type CheckboxProps = Omit<InputHTMLAttributes<HTMLInputElement>, "type">;

export function Checkbox({ className, ...props }: CheckboxProps) {
  return (
    <input
      type="checkbox"
      className={cn(
        "h-4 w-4 rounded border-slate-300 bg-white text-skyline focus:ring-skyline dark:border-slate-600 dark:bg-slate-900 dark:text-blue-500 dark:focus:ring-blue-500",
        className
      )}
      {...props}
    />
  );
}
