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
        "border border-slate-300 bg-white outline-none transition focus:border-skyline",
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
    return "h-8 rounded-md px-2 text-xs text-slate-700";
  }
  return "h-11 rounded-xl px-3 text-sm focus:ring-2 focus:ring-skyline/15";
}
