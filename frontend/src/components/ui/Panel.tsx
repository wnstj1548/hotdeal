import type { HTMLAttributes } from "react";
import { cn } from "./cn";

type PanelProps = HTMLAttributes<HTMLElement> & {
  as?: "section" | "div" | "header" | "footer";
};

export function Panel({ as = "section", className, ...props }: PanelProps) {
  const Component = as;
  return (
    <Component
      className={cn(
        "rounded-lg border border-[var(--app-border)] bg-[var(--app-surface)] shadow-[0_1px_0_rgba(17,24,39,0.03)]",
        className
      )}
      {...props}
    />
  );
}
