import { Button } from "./ui/Button";
import { Panel } from "./ui/Panel";

type ThemeMode = "light" | "dark";

type PageHeaderProps = {
  themeMode: ThemeMode;
  onToggleTheme: () => void;
};

export function PageHeader({ themeMode, onToggleTheme }: PageHeaderProps) {
  return (
    <Panel as="header" className="bg-white/80 p-6 backdrop-blur dark:bg-slate-900/65">
      <div className="flex items-center justify-end">
        <Button
          variant="outline"
          onClick={onToggleTheme}
          className="h-9 px-3 text-xs sm:text-sm"
        >
          {themeMode === "dark" ? "라이트 모드" : "다크 모드"}
        </Button>
      </div>
      <h1 className="mt-2 text-2xl font-black sm:text-3xl">핫딜 모아보기</h1>
    </Panel>
  );
}
