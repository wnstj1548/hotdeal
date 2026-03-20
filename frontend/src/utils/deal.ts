import type { DealItem } from "../types";

export function toReadDealKey(deal: Pick<DealItem, "sourceType" | "sourcePostId">): string {
  return `${deal.sourceType}:${deal.sourcePostId}`;
}

export function parsePriceInput(value: string): number | null {
  const clean = value.replace(/,/g, "").trim();
  if (!clean) {
    return null;
  }
  const parsed = Number.parseInt(clean, 10);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : null;
}

export function formatPrice(value: number | null): string {
  if (value === null) {
    return "-";
  }
  return value.toLocaleString();
}

export function formatRelative(dateString: string): string {
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
