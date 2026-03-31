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
      return "rounded-xl bg-skyline px-5 text-sm font-bold text-white hover:bg-blue-800 dark:bg-blue-600 dark:hover:bg-blue-500";
    case "outline":
      return "rounded-xl border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:border-slate-400 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-200 dark:hover:border-slate-500";
    case "chip":
      return "rounded-full border px-3 py-1 text-xs font-semibold";
    case "page":
      return "h-8 min-w-8 rounded-lg border px-2 text-sm font-semibold";
  }
}

function variantStateClasses(variant: ButtonVariant, active: boolean): string {
  if (variant === "chip") {
    return active
      ? "border-accent bg-accent text-white dark:border-orange-400 dark:bg-orange-500"
      : "border-slate-300 bg-white text-slate-700 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-200";
  }
  if (variant === "page") {
    return active
      ? "border-skyline bg-skyline text-white dark:border-blue-500 dark:bg-blue-600"
      : "border-slate-300 bg-white text-slate-700 hover:border-slate-400 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-200 dark:hover:border-slate-500";
  }
  return "";
}
