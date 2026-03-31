import type { FormEvent } from "react";
import type { DealSortOption, DealSource } from "../types";
import { formatPrice } from "../utils/deal";
import { Button } from "./ui/Button";
import { Checkbox } from "./ui/Checkbox";
import { Input } from "./ui/Input";
import { Panel } from "./ui/Panel";
import { Select } from "./ui/Select";

export type SourceFilter = DealSource | "ALL";

export type SourceOption = {
  sourceType: SourceFilter;
  sourceLabel: string;
  totalDeals: number;
};

type DealFiltersPanelProps = {
  queryInput: string;
  onQueryInputChange: (value: string) => void;
  selectedCategory: string;
  categoryOptions: string[];
  onSelectedCategoryChange: (value: string) => void;
  minPriceInput: string;
  onMinPriceInputChange: (value: string) => void;
  maxPriceInput: string;
  onMaxPriceInputChange: (value: string) => void;
  excludeEnded: boolean;
  onExcludeEndedChange: (value: boolean) => void;
  sort: DealSortOption;
  sortOptions: Array<{ value: DealSortOption; label: string }>;
  onSortChange: (value: DealSortOption) => void;
  source: SourceFilter;
  sourceOptions: SourceOption[];
  onSourceChange: (value: SourceFilter) => void;
  query: string;
  category: string;
  minPrice: number | null;
  maxPrice: number | null;
  onSubmitSearch: () => void;
  onResetFilters: () => void;
};

export function DealFiltersPanel({
  queryInput,
  onQueryInputChange,
  selectedCategory,
  categoryOptions,
  onSelectedCategoryChange,
  minPriceInput,
  onMinPriceInputChange,
  maxPriceInput,
  onMaxPriceInputChange,
  excludeEnded,
  onExcludeEndedChange,
  sort,
  sortOptions,
  onSortChange,
  source,
  sourceOptions,
  onSourceChange,
  query,
  category,
  minPrice,
  maxPrice,
  onSubmitSearch,
  onResetFilters
}: DealFiltersPanelProps) {
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSubmitSearch();
  };

  return (
    <Panel className="p-4 sm:p-5">
      <form onSubmit={handleSubmit} className="space-y-3">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Input
            value={queryInput}
            onChange={(event) => onQueryInputChange(event.target.value)}
            placeholder="검색어 (예: 맥북, 4070)"
          />
          <Select
            value={selectedCategory}
            onChange={(event) => onSelectedCategoryChange(event.target.value)}
          >
            <option value="">카테고리 전체</option>
            {categoryOptions.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </Select>
          <Input
            value={minPriceInput}
            onChange={(event) => onMinPriceInputChange(event.target.value)}
            placeholder="최소 가격"
          />
          <Input
            value={maxPriceInput}
            onChange={(event) => onMaxPriceInputChange(event.target.value)}
            placeholder="최대 가격"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <label className="inline-flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
            <Checkbox
              checked={excludeEnded}
              onChange={(event) => onExcludeEndedChange(event.target.checked)}
            />
            품절/종료 제외
          </label>
          <label className="inline-flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
            정렬
            <Select
              uiSize="sm"
              value={sort}
              onChange={(event) => onSortChange(event.target.value as DealSortOption)}
            >
              {sortOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </Select>
          </label>
          <div className="ml-auto flex gap-2">
            <Button
              variant="outline"
              onClick={onResetFilters}
              className="h-10 px-4"
            >
              초기화
            </Button>
            <Button
              type="submit"
              variant="primary"
              className="h-10"
            >
              검색
            </Button>
          </div>
        </div>
      </form>

      <div className="mt-4 flex flex-wrap gap-2">
        {sourceOptions.map((item) => (
          <Button
            key={item.sourceType}
            variant="chip"
            active={source === item.sourceType}
            onClick={() => onSourceChange(item.sourceType)}
          >
            {item.sourceLabel} {item.totalDeals ? `(${item.totalDeals.toLocaleString()})` : ""}
          </Button>
        ))}
      </div>

      <div className="mt-3 text-xs text-slate-600 dark:text-slate-400">
        적용 필터:
        <span className="ml-1">
          {query ? `검색어 "${query}"` : "검색어 없음"}
          {" · "}
          {category ? `카테고리 "${category}"` : "카테고리 없음"}
          {" · "}
          {minPrice !== null || maxPrice !== null
            ? `${formatPrice(minPrice)} ~ ${formatPrice(maxPrice)}`
            : "가격대 전체"}
          {" · "}
          {excludeEnded ? "품절/종료 제외" : "품절/종료 포함"}
        </span>
      </div>
    </Panel>
  );
}
