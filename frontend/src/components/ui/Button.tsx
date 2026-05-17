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
        "apple-focus-ring inline-flex items-center justify-center whitespace-nowrap transition-transform duration-150 disabled:cursor-not-allowed disabled:opacity-40",
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
      return "rounded-full bg-primary px-5 py-2.5 text-[15px] font-normal leading-none text-white hover:bg-[#005bb5] active:scale-95";
    case "outline":
      return "rounded-full border border-primary bg-transparent px-5 py-2.5 text-[15px] font-normal leading-none text-primary hover:bg-primary/5 active:scale-95";
    case "chip":
      return "rounded-full border px-3 py-1.5 text-xs font-normal leading-none";
    case "page":
      return "h-8 min-w-8 rounded-lg border px-2 text-[13px] font-normal leading-none";
  }
}

function variantStateClasses(variant: ButtonVariant, active: boolean): string {
  if (variant === "chip") {
    return active
      ? "border-primary bg-primary text-white active:scale-95"
      : "border-hairline bg-canvas text-ink hover:border-[#c4c4c6]";
  }
  if (variant === "page") {
    return active
      ? "border-primary bg-primary text-white active:scale-95"
      : "border-hairline bg-canvas text-ink hover:border-[#c4c4c6]";
  }
  return "";
}
