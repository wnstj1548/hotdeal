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
  SORT_OPTIONS,
  THEME_STORAGE_KEY
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

type ThemeMode = "light" | "dark";

function getInitialThemeMode(): ThemeMode {
  if (typeof window === "undefined") {
    return "light";
  }

  const savedTheme = window.localStorage.getItem(THEME_STORAGE_KEY);
  if (savedTheme === "light" || savedTheme === "dark") {
    return savedTheme;
  }

  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

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
  const [themeMode, setThemeMode] = useState<ThemeMode>(getInitialThemeMode);

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
    const root = window.document.documentElement;
    root.classList.toggle("dark", themeMode === "dark");
    root.style.colorScheme = themeMode;
  }, [themeMode]);

  useEffect(() => {
    window.localStorage.setItem(THEME_STORAGE_KEY, themeMode);
  }, [themeMode]);

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

  const onToggleTheme = () => {
    setThemeMode((prev) => (prev === "dark" ? "light" : "dark"));
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
    <div className="min-h-screen bg-[radial-gradient(circle_at_18%_18%,#dbeafe_0%,#eff6ff_35%,#fff7ed_70%,#ffffff_100%)] text-ink transition-colors dark:bg-[radial-gradient(circle_at_18%_18%,#172554_0%,#0f172a_42%,#111827_70%,#020617_100%)] dark:text-slate-100">
      <div className="mx-auto w-full max-w-6xl px-4 pb-16 pt-10 sm:px-6">
        <PageHeader themeMode={themeMode} onToggleTheme={onToggleTheme} />

        <div className="mt-6 grid gap-6 lg:grid-cols-[300px_minmax(0,1fr)]">
          <aside className="lg:sticky lg:top-6 lg:h-fit">
            <PopularChart
              popularDeals={popularDeals}
              popularLoading={popularLoading}
              popularError={popularError}
              popularLimit={popularLimit}
              popularLimitOptions={POPULAR_LIMIT_OPTIONS}
              onPopularLimitChange={setPopularLimit}
            />
          </aside>

          <div>
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

            <main className="mt-6 space-y-3">
              {loading && <DealsLoadingSkeleton />}
              {error && !loading && (
                <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-500/40 dark:bg-red-950/40 dark:text-red-200">
                  {error}
                </div>
              )}
              {!loading && !error && deals.items.length === 0 && (
                <div className="rounded-xl border border-slate-200 bg-white px-4 py-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:bg-slate-900/70 dark:text-slate-300">
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
        </div>
      </div>
    </div>
  );
}
