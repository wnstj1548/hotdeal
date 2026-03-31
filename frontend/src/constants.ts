import type { DealSortOption } from "./types";

export const PAGE_SIZE = 20;
export const DEFAULT_POPULAR_LIMIT = 20;
export const POPULAR_LIMIT_OPTIONS = [10, 20, 30, 50];
export const REFRESH_MS = 120_000;
export const READ_DEAL_STORAGE_KEY = "hotdeal.readDealKeys.v1";
export const THEME_STORAGE_KEY = "hotdeal.theme.v1";

export const SORT_OPTIONS: Array<{ value: DealSortOption; label: string }> = [
  { value: "LATEST", label: "최신순" },
  { value: "POPULAR", label: "인기순" },
  { value: "COMMENTS", label: "댓글순" }
];
