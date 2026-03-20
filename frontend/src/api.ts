import type { DealItem, DealPage, DealSortOption, DealSource, SourceSummary } from "./types";

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "";

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`);
  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export async function fetchSources(): Promise<SourceSummary[]> {
  return request<SourceSummary[]>("/api/sources");
}

export async function fetchCategories(options: {
  source: DealSource | "ALL";
}): Promise<string[]> {
  const params = new URLSearchParams();
  if (options.source !== "ALL") {
    params.set("source", options.source);
  }
  const query = params.toString();
  return request<string[]>(query ? `/api/categories?${query}` : "/api/categories");
}

export async function fetchDeals(options: {
  page: number;
  size: number;
  query: string;
  category: string;
  minPrice: number | null;
  maxPrice: number | null;
  excludeEnded: boolean;
  sort: DealSortOption;
  source: DealSource | "ALL";
}): Promise<DealPage> {
  const params = new URLSearchParams();
  params.set("page", String(options.page));
  params.set("size", String(options.size));
  if (options.query.trim()) {
    params.set("q", options.query.trim());
  }
  if (options.source !== "ALL") {
    params.set("source", options.source);
  }
  if (options.category.trim()) {
    params.set("category", options.category.trim());
  }
  if (options.minPrice !== null) {
    params.set("minPrice", String(options.minPrice));
  }
  if (options.maxPrice !== null) {
    params.set("maxPrice", String(options.maxPrice));
  }
  if (options.excludeEnded) {
    params.set("excludeEnded", "true");
  }
  params.set("sort", options.sort);
  return request<DealPage>(`/api/deals?${params.toString()}`);
}

export async function fetchPopularDeals(options: {
  limit: number;
  source: DealSource | "ALL";
}): Promise<DealItem[]> {
  const params = new URLSearchParams();
  params.set("limit", String(options.limit));
  if (options.source !== "ALL") {
    params.set("source", options.source);
  }
  return request<DealItem[]>(`/api/deals/popular?${params.toString()}`);
}
