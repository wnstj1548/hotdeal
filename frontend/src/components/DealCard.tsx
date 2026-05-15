import type { DealItem } from "../types";
import { formatRelative } from "../utils/deal";
import { Badge } from "./ui/Badge";
import { cn } from "./ui/cn";

type DealCardProps = {
  deal: DealItem;
  isRead: boolean;
  onRead: () => void;
};

export function DealCard({ deal, isRead, onRead }: DealCardProps) {
  return (
    <article
      className={cn(
        "overflow-hidden rounded-lg border bg-[var(--app-surface)] transition hover:border-[var(--app-border-strong)]",
        isRead ? "border-[var(--app-border)] opacity-80" : "border-[var(--app-border)]"
      )}
    >
      <a href={deal.url} target="_blank" rel="noreferrer" className="block" onClick={onRead}>
        <div className="grid grid-cols-[88px_1fr] gap-4 p-4 sm:grid-cols-[112px_1fr]">
          <div className="h-[72px] w-[88px] overflow-hidden rounded-md border border-[var(--app-border)] bg-[var(--app-surface-muted)] sm:h-[84px] sm:w-[112px]">
            {deal.thumbnailUrl ? (
              <img
                src={deal.thumbnailUrl}
                alt={deal.title}
                className="h-full w-full object-cover"
                loading="lazy"
                referrerPolicy="no-referrer"
              />
            ) : (
              <div className="flex h-full items-center justify-center text-xs text-[var(--app-muted)]">NO IMAGE</div>
            )}
          </div>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <Badge variant={isRead ? "neutral" : "success"}>
                {isRead ? "읽음" : "미읽음"}
              </Badge>
              <Badge variant="skyline">{deal.sourceLabel}</Badge>
              {deal.category && (
                <Badge variant="warning">
                  {deal.category}
                </Badge>
              )}
              <span className="ml-auto text-xs text-[var(--app-muted)]">{formatRelative(deal.postedAt)}</span>
            </div>
            <h2 className="mt-2 line-clamp-2 text-sm font-bold leading-5 text-[var(--app-ink)] sm:text-base">{deal.title}</h2>
            <div className="mt-2 flex flex-wrap items-center gap-2 text-xs text-[var(--app-muted)]">
              {deal.mallName && <span>몰: {deal.mallName}</span>}
              {deal.priceText && <span className="font-semibold text-[var(--app-accent)]">가격: {deal.priceText}</span>}
              {deal.shippingText && <span>배송: {deal.shippingText}</span>}
            </div>
            <div className="mt-2 flex flex-wrap items-center gap-3 border-t border-[var(--app-border)] pt-2 text-[11px] text-[var(--app-muted)]">
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
