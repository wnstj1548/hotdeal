import type { HTMLAttributes } from "react";
import { cn } from "./cn";

type PanelProps = HTMLAttributes<HTMLElement> & {
  as?: "section" | "div" | "header" | "footer";
};

export function Panel({ as = "section", className, ...props }: PanelProps) {
  const Component = as;
  return (
    <Component
      className={cn("rounded-2xl border border-slate-200 bg-white shadow-sm", className)}
      {...props}
    />
  );
}
