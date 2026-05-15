import type { ButtonHTMLAttributes } from "react";
import { cn } from "./cn";

type ButtonVariant = "primary" | "outline" | "chip" | "page";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  active?: boolean;
};

export function Button({
  variant = "outline",
  active = false,
  className,
  type = "button",
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={cn(
        "transition disabled:cursor-not-allowed disabled:opacity-40",
        variantBaseClasses(variant),
        variantStateClasses(variant, active),
        className
      )}
      {...props}
    />
  );
}

function variantBaseClasses(variant: ButtonVariant): string {
  switch (variant) {
    case "primary":
      return "rounded-lg bg-[var(--app-accent)] px-5 text-sm font-semibold text-white hover:brightness-95";
    case "outline":
      return "rounded-lg border border-[var(--app-border)] bg-[var(--app-surface)] text-sm font-semibold text-[var(--app-ink)] hover:border-[var(--app-border-strong)]";
    case "chip":
      return "rounded-full border px-3 py-1 text-xs font-semibold";
    case "page":
      return "h-8 min-w-8 rounded-md border px-2 text-sm font-semibold";
  }
}

function variantStateClasses(variant: ButtonVariant, active: boolean): string {
  if (variant === "chip") {
    return active
      ? "border-[var(--app-accent)] bg-[var(--app-accent)] text-white"
      : "border-[var(--app-border)] bg-[var(--app-surface)] text-[var(--app-ink)] hover:border-[var(--app-border-strong)]";
  }
  if (variant === "page") {
    return active
      ? "border-[var(--app-accent)] bg-[var(--app-accent)] text-white"
      : "border-[var(--app-border)] bg-[var(--app-surface)] text-[var(--app-ink)] hover:border-[var(--app-border-strong)]";
  }
  return "";
}
