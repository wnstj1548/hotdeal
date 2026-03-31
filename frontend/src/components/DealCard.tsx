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
        "overflow-hidden rounded-2xl border bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md dark:bg-slate-900/80 dark:shadow-black/25",
        isRead ? "border-slate-200/70 opacity-85 dark:border-slate-700/80" : "border-slate-200 dark:border-slate-700"
      )}
    >
      <a href={deal.url} target="_blank" rel="noreferrer" className="block" onClick={onRead}>
        <div className="grid grid-cols-[88px_1fr] gap-4 p-4 sm:grid-cols-[112px_1fr]">
          <div className="h-[72px] w-[88px] overflow-hidden rounded-lg bg-slate-100 dark:bg-slate-800 sm:h-[84px] sm:w-[112px]">
            {deal.thumbnailUrl ? (
              <img
                src={deal.thumbnailUrl}
                alt={deal.title}
                className="h-full w-full object-cover"
                loading="lazy"
                referrerPolicy="no-referrer"
              />
            ) : (
              <div className="flex h-full items-center justify-center text-xs text-slate-400 dark:text-slate-500">NO IMAGE</div>
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
              <span className="ml-auto text-xs text-slate-500 dark:text-slate-400">{formatRelative(deal.postedAt)}</span>
            </div>
            <h2 className="mt-2 line-clamp-2 text-sm font-bold leading-5 text-slate-900 dark:text-slate-100 sm:text-base">{deal.title}</h2>
            <div className="mt-2 flex flex-wrap items-center gap-2 text-xs text-slate-600 dark:text-slate-300">
              {deal.mallName && <span>몰: {deal.mallName}</span>}
              {deal.priceText && <span className="font-semibold text-accent">가격: {deal.priceText}</span>}
              {deal.shippingText && <span>배송: {deal.shippingText}</span>}
            </div>
            <div className="mt-2 flex flex-wrap items-center gap-3 text-[11px] text-slate-500 dark:text-slate-400">
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
