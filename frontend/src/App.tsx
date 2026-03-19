import { FormEvent, useEffect, useMemo, useState } from "react";
import { fetchDeals, fetchPopularDeals, fetchSources } from "./api";
import type { DealItem, DealPage, DealSource, SourceSummary } from "./types";

const PAGE_SIZE = 20;
const POPULAR_LIMIT = 10;
const REFRESH_MS = 30_000;

type SourceFilter = DealSource | "ALL";

const EMPTY_PAGE: DealPage = {
  items: [],
  page: 0,
  size: PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
  hasNext: false
};

export default function App() {
  const [sources, setSources] = useState<SourceSummary[]>([]);
  const [source, setSource] = useState<SourceFilter>("ALL");
  const [queryInput, setQueryInput] = useState("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [deals, setDeals] = useState<DealPage>(EMPTY_PAGE);
  const [popularDeals, setPopularDeals] = useState<DealItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [popularLoading, setPopularLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [popularError, setPopularError] = useState<string | null>(null);
  const [lastSyncedAt, setLastSyncedAt] = useState<Date | null>(null);
  const [refreshTick, setRefreshTick] = useState(0);

  useEffect(() => {
    fetchSources()
      .then(setSources)
      .catch((e) => setError(e instanceof Error ? e.message : "소스 목록 로딩에 실패했습니다."));
  }, []);

  useEffect(() => {
    setLoading(true);
    setError(null);

    fetchDeals({ page, size: PAGE_SIZE, query, source })
      .then((response) => {
        setDeals(response);
        setLastSyncedAt(new Date());
      })
      .catch((e) => setError(e instanceof Error ? e.message : "핫딜 목록 로딩에 실패했습니다."))
      .finally(() => setLoading(false));
  }, [page, query, source, refreshTick]);

  useEffect(() => {
    setPopularLoading(true);
    setPopularError(null);

    fetchPopularDeals({ limit: POPULAR_LIMIT, source })
      .then(setPopularDeals)
      .catch((e) => {
        setPopularError(e instanceof Error ? e.message : "인기 차트 로딩에 실패했습니다.");
      })
      .finally(() => setPopularLoading(false));
  }, [source, refreshTick]);

  useEffect(() => {
    const timer = window.setInterval(() => {
      setRefreshTick((prev) => prev + 1);
    }, REFRESH_MS);
    return () => window.clearInterval(timer);
  }, []);

  const sourceOptions = useMemo(() => {
    return [{ sourceType: "ALL" as const, sourceLabel: "전체", totalDeals: deals.totalElements }, ...sources];
  }, [deals.totalElements, sources]);

  const onSubmitSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setPage(0);
    setQuery(queryInput);
  };

  const changeSource = (nextSource: SourceFilter) => {
    setSource(nextSource);
    setPage(0);
  };

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_18%_18%,#dbeafe_0%,#eff6ff_35%,#fff7ed_70%,#ffffff_100%)] text-ink">
      <div className="mx-auto w-full max-w-6xl px-4 pb-16 pt-10 sm:px-6">
        <header className="rounded-2xl border border-slate-200 bg-white/80 p-6 shadow-sm backdrop-blur">
          <p className="text-sm font-semibold tracking-wide text-skyline">HOTDEAL STREAM</p>
          <h1 className="mt-2 text-2xl font-black sm:text-3xl">핫딜 모아보기</h1>
          <p className="mt-2 text-sm text-slate-600">
            뽐뿌, 에펨코리아, 어미새, 퀘이사존 핫딜을 한 곳에서 확인합니다.
          </p>
          <div className="mt-4 flex flex-wrap items-center gap-2 text-xs text-slate-500">
            <span>자동 갱신 {Math.floor(REFRESH_MS / 1000)}초</span>
            <span>•</span>
            <span>마지막 동기화 {formatSyncTime(lastSyncedAt)}</span>
            <button
              type="button"
              onClick={() => setRefreshTick((prev) => prev + 1)}
              className="rounded-md border border-slate-300 px-2 py-1 font-semibold text-slate-700 transition hover:bg-slate-100"
            >
              지금 새로고침
            </button>
          </div>
        </header>

        <section className="mt-6 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3 sm:px-5">
            <div>
              <h2 className="text-sm font-bold text-slate-900 sm:text-base">상단 인기 차트</h2>
              <p className="text-xs text-slate-500">최근 3시간 조회수 기준 TOP {POPULAR_LIMIT}</p>
            </div>
          </div>

          <div className="p-4 sm:p-5">
            {popularLoading && <PopularLoadingSkeleton />}
            {popularError && !popularLoading && (
              <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{popularError}</div>
            )}
            {!popularLoading && !popularError && popularDeals.length === 0 && (
              <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
                최근 3시간 내 인기 핫딜이 없습니다.
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
                      className="group grid grid-cols-[40px_1fr] gap-3 rounded-xl border border-slate-200 px-3 py-3 transition hover:border-slate-300 hover:bg-slate-50"
                    >
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-900 text-sm font-extrabold text-white">
                        {index + 1}
                      </div>
                      <div>
                        <p className="line-clamp-2 text-sm font-semibold text-slate-900 transition group-hover:text-skyline sm:text-[15px]">
                          {deal.title}
                        </p>
                        <div className="mt-2 flex flex-wrap items-center gap-2 text-[11px] text-slate-600">
                          <span className="rounded-md bg-skyline px-2 py-1 font-semibold text-white">{deal.sourceLabel}</span>
                          <span className="rounded-md bg-slate-100 px-2 py-1">조회수 {deal.viewCount ?? 0}</span>
                          <span className="rounded-md bg-slate-100 px-2 py-1">{formatRelative(deal.postedAt)}</span>
                        </div>
                      </div>
                    </a>
                  </li>
                ))}
              </ol>
            )}
          </div>
        </section>

        <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm sm:p-5">
          <form onSubmit={onSubmitSearch} className="flex flex-col gap-3 sm:flex-row">
            <input
              value={queryInput}
              onChange={(event) => setQueryInput(event.target.value)}
              placeholder="예: 맥북, 4070, 에어팟"
              className="h-11 flex-1 rounded-xl border border-slate-300 px-3 text-sm outline-none transition focus:border-skyline focus:ring-2 focus:ring-skyline/15"
            />
            <button
              type="submit"
              className="h-11 rounded-xl bg-skyline px-5 text-sm font-bold text-white transition hover:bg-blue-800"
            >
              검색
            </button>
          </form>

          <div className="mt-4 flex flex-wrap gap-2">
            {sourceOptions.map((item) => (
              <button
                key={item.sourceType}
                type="button"
                onClick={() => changeSource(item.sourceType)}
                className={[
                  "rounded-full border px-3 py-1 text-xs font-semibold transition",
                  source === item.sourceType
                    ? "border-accent bg-accent text-white"
                    : "border-slate-300 bg-white text-slate-700 hover:border-slate-400"
                ].join(" ")}
              >
                {item.sourceLabel} {item.totalDeals ? `(${item.totalDeals.toLocaleString()})` : ""}
              </button>
            ))}
          </div>
        </section>

        <main className="mt-6 space-y-3">
          {loading && <LoadingSkeleton />}
          {error && !loading && (
            <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
          )}
          {!loading && !error && deals.items.length === 0 && (
            <div className="rounded-xl border border-slate-200 bg-white px-4 py-8 text-center text-sm text-slate-500">
              조건에 맞는 딜이 없습니다.
            </div>
          )}
          {!loading && !error && deals.items.map((deal) => <DealCard key={deal.id} deal={deal} />)}
        </main>

        <footer className="mt-6 flex items-center justify-between rounded-xl border border-slate-200 bg-white px-4 py-3">
          <p className="text-sm text-slate-600">
            페이지 {deals.page + 1} / {Math.max(deals.totalPages, 1)}
          </p>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
              disabled={deals.page <= 0 || loading}
              className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-40"
            >
              이전
            </button>
            <button
              type="button"
              onClick={() => setPage((prev) => (deals.hasNext ? prev + 1 : prev))}
              disabled={!deals.hasNext || loading}
              className="rounded-lg bg-skyline px-3 py-1.5 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-40"
            >
              다음
            </button>
          </div>
        </footer>
      </div>
    </div>
  );
}

function DealCard({ deal }: { deal: DealItem }) {
  return (
    <article className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
      <a href={deal.url} target="_blank" rel="noreferrer" className="block">
        <div className="grid grid-cols-[88px_1fr] gap-4 p-4 sm:grid-cols-[112px_1fr]">
          <div className="h-[72px] w-[88px] overflow-hidden rounded-lg bg-slate-100 sm:h-[84px] sm:w-[112px]">
            {deal.thumbnailUrl ? (
              <img src={deal.thumbnailUrl} alt={deal.title} className="h-full w-full object-cover" loading="lazy" />
            ) : (
              <div className="flex h-full items-center justify-center text-xs text-slate-400">NO IMAGE</div>
            )}
          </div>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <span className="rounded-md bg-skyline px-2 py-1 text-[11px] font-bold text-white">{deal.sourceLabel}</span>
              {deal.category && (
                <span className="rounded-md bg-amber-500/90 px-2 py-1 text-[11px] font-semibold text-white">
                  {deal.category}
                </span>
              )}
              <span className="ml-auto text-xs text-slate-500">{formatRelative(deal.postedAt)}</span>
            </div>
            <h2 className="mt-2 line-clamp-2 text-sm font-bold leading-5 text-slate-900 sm:text-base">{deal.title}</h2>
            <div className="mt-2 flex flex-wrap items-center gap-2 text-xs text-slate-600">
              {deal.mallName && <span>몰: {deal.mallName}</span>}
              {deal.priceText && <span className="font-semibold text-accent">가격: {deal.priceText}</span>}
              {deal.shippingText && <span>배송: {deal.shippingText}</span>}
            </div>
            <div className="mt-2 flex flex-wrap items-center gap-3 text-[11px] text-slate-500">
              <span>댓글 {deal.replyCount ?? 0}</span>
              <span>추천 {deal.likeCount ?? 0}</span>
              <span>조회 {deal.viewCount ?? 0}</span>
            </div>
          </div>
        </div>
      </a>
    </article>
  );
}

function LoadingSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 5 }).map((_, index) => (
        <div key={index} className="h-[118px] animate-pulse rounded-2xl border border-slate-200 bg-white/80" />
      ))}
    </div>
  );
}

function PopularLoadingSkeleton() {
  return (
    <div className="space-y-2">
      {Array.from({ length: 4 }).map((_, index) => (
        <div key={index} className="h-[64px] animate-pulse rounded-xl border border-slate-200 bg-slate-50" />
      ))}
    </div>
  );
}

function formatRelative(dateString: string) {
  const target = new Date(dateString);
  if (Number.isNaN(target.getTime())) {
    return dateString;
  }
  const diffMs = Date.now() - target.getTime();
  const diffMinutes = Math.floor(diffMs / 60_000);
  if (diffMinutes < 1) {
    return "방금";
  }
  if (diffMinutes < 60) {
    return `${diffMinutes}분 전`;
  }
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) {
    return `${diffHours}시간 전`;
  }
  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays}일 전`;
}

function formatSyncTime(date: Date | null) {
  if (!date) {
    return "없음";
  }
  return date.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}
