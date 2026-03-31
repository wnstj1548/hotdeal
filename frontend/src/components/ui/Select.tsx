import type { SelectHTMLAttributes } from "react";
import { cn } from "./cn";

type SelectSize = "sm" | "md";

type SelectProps = SelectHTMLAttributes<HTMLSelectElement> & {
  uiSize?: SelectSize;
};

export function Select({ uiSize = "md", className, children, ...props }: SelectProps) {
  return (
    <select
      className={cn(
        "border border-slate-300 bg-white outline-none transition focus:border-skyline dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:focus:border-blue-500",
        sizeClasses(uiSize),
        className
      )}
      {...props}
    >
      {children}
    </select>
  );
}

function sizeClasses(size: SelectSize): string {
  if (size === "sm") {
    return "h-8 rounded-md px-2 text-xs text-slate-700 dark:text-slate-200";
  }
  return "h-11 rounded-xl px-3 text-sm text-slate-900 focus:ring-2 focus:ring-skyline/15 dark:text-slate-100 dark:focus:ring-blue-400/20";
}
