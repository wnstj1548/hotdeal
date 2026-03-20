export type DealSource = "PPOMPPU" | "FMKOREA" | "EOMISAE" | "QUASARZONE" | "RULIWEB" | "CLIEN";

export type DealSortOption = "LATEST" | "POPULAR" | "COMMENTS";

export interface DealItem {
  id: number;
  sourceType: DealSource;
  sourceLabel: string;
  sourcePostId: string;
  title: string;
  url: string;
  thumbnailUrl: string | null;
  mallName: string | null;
  category: string | null;
  priceText: string | null;
  priceValue: number | null;
  shippingText: string | null;
  postedAt: string;
  likeCount: number | null;
  replyCount: number | null;
  viewCount: number | null;
  hot: boolean | null;
  ended: boolean | null;
}

export interface DealPage {
  items: DealItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface SourceSummary {
  sourceType: DealSource;
  sourceLabel: string;
  sourceUrl: string;
  totalDeals: number;
}
