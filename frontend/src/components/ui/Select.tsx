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
        "apple-focus-ring border border-black/10 bg-canvas outline-none transition focus:border-primary",
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
    return "h-8 rounded-lg px-2 text-xs text-ink";
  }
  return "h-11 rounded-full px-4 text-[15px] text-ink";
}
