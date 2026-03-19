import type { DealItem, DealPage, DealSource, SourceSummary } from "./types";

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

export async function fetchDeals(options: {
  page: number;
  size: number;
  query: string;
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
