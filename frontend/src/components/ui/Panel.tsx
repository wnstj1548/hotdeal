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
        "rounded-[18px] border border-hairline bg-canvas",
        className
      )}
      {...props}
    />
  );
}
