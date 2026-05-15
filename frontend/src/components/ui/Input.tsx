import type { InputHTMLAttributes } from "react";
import { cn } from "./cn";

type InputProps = InputHTMLAttributes<HTMLInputElement>;

export function Input({ className, ...props }: InputProps) {
  return (
    <input
      className={cn(
        "h-10 rounded-lg border border-[var(--app-border)] bg-[var(--app-surface)] px-3 text-sm text-[var(--app-ink)] outline-none transition placeholder:text-[var(--app-muted)] focus:border-[var(--app-accent)] focus:ring-2 focus:ring-[var(--app-focus)]",
        className
      )}
      {...props}
    />
  );
}
