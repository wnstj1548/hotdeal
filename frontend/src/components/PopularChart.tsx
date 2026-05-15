import type { DealItem } from "../types";
import { Badge } from "./ui/Badge";
import { Panel } from "./ui/Panel";
import { Select } from "./ui/Select";

type PopularChartProps = {
  popularDeals: DealItem[];
  popularLoading: boolean;
  popularError: string | null;
  popularLimit: number;
  popularLimitOptions: readonly number[];
  onPopularLimitChange: (value: number) => void;
};

export function PopularChart({
  popularDeals,
  popularLoading,
  popularError,
  popularLimit,
  popularLimitOptions,
  onPopularLimitChange
}: PopularChartProps) {
  return (
    <Panel className="overflow-hidden">
      <div className="flex items-center justify-between border-b border-[var(--app-border)] bg-[var(--app-surface-muted)] px-4 py-3 sm:px-5">
        <div>
          <h2 className="text-sm font-bold text-[var(--app-ink)] sm:text-base">인기 차트</h2>
          <p className="text-xs text-[var(--app-muted)]">최근 3시간 조회수 TOP {popularLimit}</p>
        </div>
        <Select
          uiSize="sm"
          value={popularLimit}
          onChange={(event) => onPopularLimitChange(Number.parseInt(event.target.value, 10))}
        >
          {popularLimitOptions.map((option) => (
            <option key={option} value={option}>
              TOP {option}
            </option>
          ))}
        </Select>
      </div>

      <div className="max-h-[min(72vh,820px)] overflow-y-auto p-4 sm:p-5">
        {popularLoading && <PopularLoadingSkeleton />}
        {popularError && !popularLoading && (
          <div className="rounded-lg border border-red-300 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-600/60 dark:bg-red-950/30 dark:text-red-200">
            {popularError}
          </div>
        )}
        {!popularLoading && !popularError && popularDeals.length === 0 && (
          <div className="rounded-lg border border-[var(--app-border)] bg-[var(--app-surface-muted)] px-4 py-8 text-center text-sm text-[var(--app-muted)]">
            최근 3시간 인기 딜이 없습니다.
          </div>
        )}
        {!popularLoading && !popularError && popularDeals.length > 0 && (
          <ol className="space-y-2">
            {popularDeals.map((deal, index) => (
              <li key={deal.id}>
                <a
                  href={deal.url}
                  target="_blank"
                  rel="noreferrer"
                  className="group grid grid-cols-[34px_1fr] gap-2 rounded-md border border-[var(--app-border)] px-3 py-3 transition hover:border-[var(--app-border-strong)] hover:bg-[var(--app-surface-muted)]"
                >
                  <div className="flex h-8 w-8 items-center justify-center rounded-md bg-[var(--app-accent)] text-xs font-extrabold text-white">
                    {index + 1}
                  </div>
                  <div>
                    <p className="line-clamp-2 text-[13px] font-semibold text-[var(--app-ink)] transition group-hover:text-[var(--app-accent)]">
                      {deal.title}
                    </p>
                    <div className="mt-2 flex flex-wrap items-center gap-1.5 text-[10px] text-[var(--app-muted)]">
                      <Badge variant="skyline" size="sm">{deal.sourceLabel}</Badge>
                      <Badge size="sm">조회 {deal.viewCount ?? 0}</Badge>
                    </div>
                  </div>
                </a>
              </li>
            ))}
          </ol>
        )}
      </div>
    </Panel>
  );
}

function PopularLoadingSkeleton() {
  return (
    <div className="space-y-2">
      {Array.from({ length: 4 }).map((_, index) => (
        <div key={index} className="h-[64px] animate-pulse rounded-md border border-[var(--app-border)] bg-[var(--app-surface-muted)]" />
      ))}
    </div>
  );
}
