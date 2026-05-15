import { Button } from "./ui/Button";
import { Panel } from "./ui/Panel";

type ThemeMode = "light" | "dark";

type PageHeaderProps = {
  themeMode: ThemeMode;
  onToggleTheme: () => void;
  activeSourceLabel: string;
  totalDeals: number;
  readCount: number;
};

export function PageHeader({
  themeMode,
  onToggleTheme,
  activeSourceLabel,
  totalDeals,
  readCount
}: PageHeaderProps) {
  return (
    <Panel as="header" className="border-[var(--app-border-strong)] p-5 sm:p-6">
      <div className="flex flex-wrap items-start gap-4">
        <div>
          <p className="text-xs font-semibold text-[var(--app-muted)]">실시간 딜 대시보드</p>
          <h1 className="mt-1 text-2xl font-black text-[var(--app-ink)] sm:text-3xl">핫딜 모아보기</h1>
          <p className="mt-2 text-sm text-[var(--app-muted)]">
            2분 주기 자동 새로고침 · {activeSourceLabel}
          </p>
        </div>

        <div className="ml-auto min-w-[220px] space-y-2">
          <div className="flex justify-end">
            <Button
              variant="outline"
              onClick={onToggleTheme}
              className="h-9 px-3 text-xs sm:text-sm"
            >
              {themeMode === "dark" ? "라이트 모드" : "다크 모드"}
            </Button>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div className="rounded-md border border-[var(--app-border)] bg-[var(--app-surface-muted)] px-2.5 py-2 text-right">
              <p className="text-[11px] text-[var(--app-muted)]">현재 딜</p>
              <p className="text-sm font-bold text-[var(--app-ink)]">{totalDeals.toLocaleString()}</p>
            </div>
            <div className="rounded-md border border-[var(--app-border)] bg-[var(--app-surface-muted)] px-2.5 py-2 text-right">
              <p className="text-[11px] text-[var(--app-muted)]">읽은 딜</p>
              <p className="text-sm font-bold text-[var(--app-ink)]">{readCount.toLocaleString()}</p>
            </div>
          </div>
        </div>
      </div>
    </Panel>
  );
}
