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
        "inline-flex items-center rounded-full font-normal",
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
    return "px-2 py-1 text-[10px] leading-none";
  }
  return "px-2.5 py-1 text-[11px] leading-none";
}

function variantClasses(variant: BadgeVariant): string {
  switch (variant) {
    case "skyline":
      return "bg-primary text-white";
    case "success":
      return "border border-primary/30 bg-primary/10 text-primary";
    case "neutral":
      return "bg-parchment text-ink";
    case "warning":
      return "bg-ink text-white";
  }
}
