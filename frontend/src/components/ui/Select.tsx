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
        "border border-[var(--app-border)] bg-[var(--app-surface)] text-[var(--app-ink)] outline-none transition focus:border-[var(--app-accent)]",
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
    return "h-8 rounded-md px-2 text-xs";
  }
  return "h-10 rounded-lg px-3 text-sm focus:ring-2 focus:ring-[var(--app-focus)]";
}
