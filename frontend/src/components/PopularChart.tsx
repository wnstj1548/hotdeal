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
      <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3 dark:border-slate-700 sm:px-5">
        <div>
          <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 sm:text-base">인기 차트</h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">최근 3시간 조회수 TOP {popularLimit}</p>
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
          <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-500/40 dark:bg-red-950/40 dark:text-red-200">
            {popularError}
          </div>
        )}
        {!popularLoading && !popularError && popularDeals.length === 0 && (
          <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:bg-slate-900/70 dark:text-slate-300">
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
                  className="group grid grid-cols-[34px_1fr] gap-2 rounded-xl border border-slate-200 px-3 py-3 transition hover:border-slate-300 hover:bg-slate-50 dark:border-slate-700 dark:hover:border-slate-600 dark:hover:bg-slate-800/70"
                >
                  <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-900 text-xs font-extrabold text-white dark:bg-slate-100 dark:text-slate-900">
                    {index + 1}
                  </div>
                  <div>
                    <p className="line-clamp-2 text-[13px] font-semibold text-slate-900 transition group-hover:text-skyline dark:text-slate-100 dark:group-hover:text-blue-400">
                      {deal.title}
                    </p>
                    <div className="mt-2 flex flex-wrap items-center gap-1.5 text-[10px] text-slate-600 dark:text-slate-300">
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
        <div key={index} className="h-[64px] animate-pulse rounded-xl border border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800/70" />
      ))}
    </div>
  );
}
