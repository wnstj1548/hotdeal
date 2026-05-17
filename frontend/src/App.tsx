import { useEffect, useMemo, useState } from "react";
import { fetchCategories, fetchDeals, fetchPopularDeals, fetchSources } from "./api";
import { DealCard } from "./components/DealCard";
import { DealFiltersPanel, type SourceOption } from "./components/DealFiltersPanel";
import { DealsLoadingSkeleton } from "./components/DealsLoadingSkeleton";
import { DealsPagination } from "./components/DealsPagination";
import { PageHeader } from "./components/PageHeader";
import { PopularChart } from "./components/PopularChart";
import {
  DEFAULT_POPULAR_LIMIT,
  PAGE_SIZE,
  POPULAR_LIMIT_OPTIONS,
  READ_DEAL_STORAGE_KEY,
  REFRESH_MS,
  SORT_OPTIONS
} from "./constants";
import type { DealItem, DealPage, DealSortOption, SourceSummary } from "./types";
import { parsePriceInput, toReadDealKey } from "./utils/deal";
import { buildPageNumbers } from "./utils/pagination";

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
  const [source, setSource] = useState<SourceOption["sourceType"]>("ALL");
  const [queryInput, setQueryInput] = useState("");
  const [query, setQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [category, setCategory] = useState("");
  const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
  const [minPriceInput, setMinPriceInput] = useState("");
  const [maxPriceInput, setMaxPriceInput] = useState("");
  const [minPrice, setMinPrice] = useState<number | null>(null);
  const [maxPrice, setMaxPrice] = useState<number | null>(null);
  const [excludeEnded, setExcludeEnded] = useState(false);
  const [sort, setSort] = useState<DealSortOption>("LATEST");
  const [page, setPage] = useState(0);
  const [deals, setDeals] = useState<DealPage>(EMPTY_PAGE);
  const [popularDeals, setPopularDeals] = useState<DealItem[]>([]);
  const [popularLimit, setPopularLimit] = useState(DEFAULT_POPULAR_LIMIT);
  const [readDealKeys, setReadDealKeys] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [popularLoading, setPopularLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [popularError, setPopularError] = useState<string | null>(null);
  const [refreshTick, setRefreshTick] = useState(0);

  useEffect(() => {
    fetchSources()
      .then(setSources)
      .catch((e) => setError(e instanceof Error ? e.message : "소스 목록 로딩에 실패했습니다."));
  }, []);

  useEffect(() => {
    setLoading(true);
    setError(null);

    fetchDeals({
      page,
      size: PAGE_SIZE,
      query,
      category,
      minPrice,
      maxPrice,
      excludeEnded,
      sort,
      source
    })
      .then(setDeals)
      .catch((e) => setError(e instanceof Error ? e.message : "핫딜 목록 로딩에 실패했습니다."))
      .finally(() => setLoading(false));
  }, [page, query, category, minPrice, maxPrice, excludeEnded, sort, source, refreshTick]);

  useEffect(() => {
    setPopularLoading(true);
    setPopularError(null);

    fetchPopularDeals({ limit: popularLimit, source })
      .then(setPopularDeals)
      .catch((e) => {
        setPopularError(e instanceof Error ? e.message : "인기 차트 로딩에 실패했습니다.");
      })
      .finally(() => setPopularLoading(false));
  }, [popularLimit, source, refreshTick]);

  useEffect(() => {
    fetchCategories({ source })
      .then((items) => {
        setCategoryOptions(items);
        setSelectedCategory((prev) => (items.includes(prev) ? prev : ""));
      })
      .catch(() => {
        setCategoryOptions([]);
        setSelectedCategory("");
      });
  }, [source]);

  useEffect(() => {
    const timer = window.setInterval(() => {
      setRefreshTick((prev) => prev + 1);
    }, REFRESH_MS);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    try {
      const raw = window.localStorage.getItem(READ_DEAL_STORAGE_KEY);
      if (!raw) {
        return;
      }
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        setReadDealKeys(parsed.filter((item): item is string => typeof item === "string"));
      }
    } catch {
      setReadDealKeys([]);
    }
  }, []);

  useEffect(() => {
    window.localStorage.setItem(READ_DEAL_STORAGE_KEY, JSON.stringify(readDealKeys));
  }, [readDealKeys]);

  const sourceOptions = useMemo<SourceOption[]>(() => {
    return [{ sourceType: "ALL", sourceLabel: "전체", totalDeals: deals.totalElements }, ...sources];
  }, [deals.totalElements, sources]);

  const pageNumbers = useMemo(() => {
    return buildPageNumbers(deals.totalPages, deals.page, 7);
  }, [deals.page, deals.totalPages]);

  const selectedSourceLabel = useMemo(() => {
    const selected = sourceOptions.find((item) => item.sourceType === source);
    return selected?.sourceLabel ?? "전체";
  }, [source, sourceOptions]);

  const readDealKeySet = useMemo(() => new Set(readDealKeys), [readDealKeys]);

  const onSubmitSearch = () => {
    setPage(0);
    setQuery(queryInput.trim());
    setCategory(selectedCategory.trim());
    setMinPrice(parsePriceInput(minPriceInput));
    setMaxPrice(parsePriceInput(maxPriceInput));
  };

  const onResetFilters = () => {
    setQueryInput("");
    setQuery("");
    setSelectedCategory("");
    setCategory("");
    setMinPriceInput("");
    setMaxPriceInput("");
    setMinPrice(null);
    setMaxPrice(null);
    setExcludeEnded(false);
    setSort("LATEST");
    setPage(0);
  };

  const onSourceChange = (nextSource: SourceOption["sourceType"]) => {
    setSource(nextSource);
    setSelectedCategory("");
    setCategory("");
    setPage(0);
  };

  const onExcludeEndedChange = (value: boolean) => {
    setExcludeEnded(value);
    setPage(0);
  };

  const onSortChange = (nextSort: DealSortOption) => {
    setSort(nextSort);
    setPage(0);
  };

  const markDealAsRead = (deal: DealItem) => {
    const key = toReadDealKey(deal);
    setReadDealKeys((prev) => {
      if (prev.includes(key)) {
        return prev;
      }
      const next = [...prev, key];
      if (next.length > 10_000) {
        return next.slice(next.length - 10_000);
      }
      return next;
    });
  };

  return (
    <div className="min-h-screen bg-parchment text-ink">
      <header className="sticky top-0 z-40 h-11 bg-black">
        <div className="mx-auto flex h-full w-full max-w-[1440px] items-center justify-between px-4 sm:px-6">
          <nav className="flex items-center gap-5 text-[12px] text-white/85">
            <span className="apple-tight text-[14px] font-semibold text-white">Hotdeal</span>
            <a href="#popular" className="transition-colors hover:text-white">인기 차트</a>
            <a href="#deals" className="transition-colors hover:text-white">딜 목록</a>
          </nav>
          <div className="flex items-center gap-4 text-[12px] text-white/75">
            <span>검색</span>
            <span>계정</span>
          </div>
        </div>
      </header>

      <div className="sticky top-11 z-30 border-b border-black/5 bg-[rgba(245,245,247,0.8)] backdrop-blur">
        <div className="mx-auto flex h-[52px] w-full max-w-[1440px] items-center justify-between px-4 sm:px-6">
          <p className="apple-display text-[21px] font-semibold leading-none text-ink">핫딜</p>
          <div className="flex items-center gap-4 text-[14px] text-[#6e6e73]">
            <span className="hidden sm:inline">소스 {selectedSourceLabel}</span>
            <a href="#popular" className="apple-focus-ring transition-colors hover:text-primary">인기</a>
            <a href="#deals" className="apple-focus-ring transition-colors hover:text-primary">목록</a>
            <span className="rounded-full bg-primary px-3 py-1 text-[12px] text-white">
              {deals.totalElements.toLocaleString()}개
            </span>
          </div>
        </div>
      </div>

      <PageHeader />

      <section id="popular" className="bg-tile1 py-14 sm:py-16">
        <div className="mx-auto grid w-full max-w-[1200px] gap-6 px-4 sm:px-6 lg:grid-cols-[300px_minmax(0,1fr)]">
          <aside className="lg:sticky lg:top-[124px] lg:h-fit">
            <p className="apple-tight text-[14px] font-semibold text-primaryOnDark">Trending Deals</p>
            <h2 className="apple-display mt-2 text-[34px] font-semibold leading-[1.15] text-white">
              지금 가장 많이 보는 딜
            </h2>
            <p className="mt-3 text-[17px] leading-[1.47] text-[#cccccc]">
              최근 3시간 조회수를 기준으로 실시간 인기 순위를 제공합니다.
            </p>
          </aside>
          <PopularChart
            popularDeals={popularDeals}
            popularLoading={popularLoading}
            popularError={popularError}
            popularLimit={popularLimit}
            popularLimitOptions={POPULAR_LIMIT_OPTIONS}
            onPopularLimitChange={setPopularLimit}
          />
        </div>
      </section>

      <section id="deals" className="bg-canvas py-14 sm:py-16">
        <div className="mx-auto w-full max-w-[1200px] px-4 sm:px-6">
          <h2 className="apple-display text-[40px] font-semibold leading-[1.1] text-ink">전체 딜 탐색</h2>
          <p className="mt-2 text-[17px] text-[#6e6e73]">
            총 {deals.totalElements.toLocaleString()}개의 딜을 조건별로 필터링해 확인할 수 있습니다.
          </p>

          <div className="mt-8">
            <DealFiltersPanel
              queryInput={queryInput}
              onQueryInputChange={setQueryInput}
              selectedCategory={selectedCategory}
              categoryOptions={categoryOptions}
              onSelectedCategoryChange={setSelectedCategory}
              minPriceInput={minPriceInput}
              onMinPriceInputChange={setMinPriceInput}
              maxPriceInput={maxPriceInput}
              onMaxPriceInputChange={setMaxPriceInput}
              excludeEnded={excludeEnded}
              onExcludeEndedChange={onExcludeEndedChange}
              sort={sort}
              sortOptions={SORT_OPTIONS}
              onSortChange={onSortChange}
              source={source}
              sourceOptions={sourceOptions}
              onSourceChange={onSourceChange}
              query={query}
              category={category}
              minPrice={minPrice}
              maxPrice={maxPrice}
              onSubmitSearch={onSubmitSearch}
              onResetFilters={onResetFilters}
            />
          </div>

          <main className="mt-6 space-y-3">
            {loading && <DealsLoadingSkeleton />}
            {error && !loading && (
              <div className="rounded-[11px] border border-red-200 bg-red-50 px-4 py-3 text-[14px] text-red-700">
                {error}
              </div>
            )}
            {!loading && !error && deals.items.length === 0 && (
              <div className="rounded-[18px] border border-hairline bg-parchment px-4 py-8 text-center text-[14px] text-[#6e6e73]">
                조건에 맞는 딜이 없습니다.
              </div>
            )}
            {!loading && !error && deals.items.map((deal) => (
              <DealCard
                key={deal.id}
                deal={deal}
                isRead={readDealKeySet.has(toReadDealKey(deal))}
                onRead={() => markDealAsRead(deal)}
              />
            ))}
          </main>

          <DealsPagination
            currentPage={deals.page}
            totalPages={deals.totalPages}
            hasNext={deals.hasNext}
            loading={loading}
            pageNumbers={pageNumbers}
            onPrev={() => setPage((prev) => Math.max(prev - 1, 0))}
            onPageSelect={setPage}
            onNext={() => setPage((prev) => (deals.hasNext ? prev + 1 : prev))}
          />
        </div>
      </section>

      <footer className="bg-parchment px-4 py-8 text-[12px] text-[#6e6e73] sm:px-6">
        <div className="mx-auto w-full max-w-[1200px] border-t border-black/5 pt-4">
          데이터는 2분 간격으로 자동 갱신됩니다.
        </div>
      </footer>
    </div>
  );
}
