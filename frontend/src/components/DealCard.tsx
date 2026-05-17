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
        "overflow-hidden rounded-[18px] border bg-canvas transition-colors",
        isRead ? "border-hairline opacity-80" : "border-hairline hover:border-[#c4c4c6]"
      )}
    >
      <a href={deal.url} target="_blank" rel="noreferrer" className="block" onClick={onRead}>
        <div className="grid grid-cols-[88px_1fr] gap-4 p-4 sm:grid-cols-[112px_1fr]">
          <div className="h-[72px] w-[88px] overflow-hidden rounded-[8px] bg-parchment sm:h-[84px] sm:w-[112px]">
            {deal.thumbnailUrl ? (
              <img
                src={deal.thumbnailUrl}
                alt={deal.title}
                className="h-full w-full object-cover shadow-[rgba(0,0,0,0.22)_3px_5px_30px_0]"
                loading="lazy"
                referrerPolicy="no-referrer"
              />
            ) : (
              <div className="flex h-full items-center justify-center text-xs text-[#8d8d92]">NO IMAGE</div>
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
              <span className="ml-auto text-xs text-[#6e6e73]">{formatRelative(deal.postedAt)}</span>
            </div>
            <h2 className="apple-tight mt-2 line-clamp-2 text-[17px] font-semibold leading-[1.24] text-ink">{deal.title}</h2>
            <div className="mt-2 flex flex-wrap items-center gap-2 text-[12px] text-[#6e6e73]">
              {deal.mallName && <span>몰: {deal.mallName}</span>}
              {deal.priceText && <span className="font-semibold text-primary">가격: {deal.priceText}</span>}
              {deal.shippingText && <span>배송: {deal.shippingText}</span>}
            </div>
            <div className="mt-2 flex flex-wrap items-center gap-3 text-[11px] text-[#8d8d92]">
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
