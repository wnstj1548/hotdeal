import type { HTMLAttributes } from "react";
import { cn } from "./cn";

type BadgeVariant = "skyline" | "success" | "neutral" | "warning";
type BadgeSize = "sm" | "md";

type BadgeProps = HTMLAttributes<HTMLSpanElement> & {
  variant?: BadgeVariant;
  size?: BadgeSize;
};

export function Badge({ variant = "neutral", size = "md", className, ...props }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-md font-semibold",
        sizeClasses(size),
        variantClasses(variant),
        className
      )}
      {...props}
    />
  );
}

function sizeClasses(size: BadgeSize): string {
  if (size === "sm") {
    return "px-1.5 py-0.5 text-[10px]";
  }
  return "px-2 py-1 text-[11px]";
}

function variantClasses(variant: BadgeVariant): string {
  switch (variant) {
    case "skyline":
      return "bg-skyline text-white";
    case "success":
      return "bg-emerald-600 text-white";
    case "neutral":
      return "bg-slate-200 text-slate-700";
    case "warning":
      return "bg-amber-500/90 text-white";
  }
}
