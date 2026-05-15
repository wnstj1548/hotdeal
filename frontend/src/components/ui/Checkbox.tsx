import type { InputHTMLAttributes } from "react";
import { cn } from "./cn";

type CheckboxProps = Omit<InputHTMLAttributes<HTMLInputElement>, "type">;

export function Checkbox({ className, ...props }: CheckboxProps) {
  return (
    <input
      type="checkbox"
      className={cn(
        "h-4 w-4 rounded border-[var(--app-border)] bg-[var(--app-surface)] text-[var(--app-accent)] focus:ring-[var(--app-accent)]",
        className
      )}
      {...props}
    />
  );
}
