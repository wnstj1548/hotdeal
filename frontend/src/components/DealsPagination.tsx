import { Button } from "./ui/Button";
import { Panel } from "./ui/Panel";

type DealsPaginationProps = {
  currentPage: number;
  totalPages: number;
  hasNext: boolean;
  loading: boolean;
  pageNumbers: number[];
  onPrev: () => void;
  onPageSelect: (page: number) => void;
  onNext: () => void;
};

export function DealsPagination({
  currentPage,
  totalPages,
  hasNext,
  loading,
  pageNumbers,
  onPrev,
  onPageSelect,
  onNext
}: DealsPaginationProps) {
  return (
    <Panel as="footer" className="mt-6 px-4 py-4 sm:px-5">
      <p className="mb-3 text-[14px] text-[#6e6e73]">
        페이지 {currentPage + 1} / {Math.max(totalPages, 1)}
      </p>
      <div className="flex flex-wrap items-center gap-2">
        <Button
          variant="outline"
          onClick={onPrev}
          disabled={currentPage <= 0 || loading}
          className="h-8 rounded-lg px-3 py-1.5 text-[13px]"
        >
          이전
        </Button>
        {pageNumbers.map((pageNumber) => (
          <Button
            key={pageNumber}
            variant="page"
            active={pageNumber === currentPage}
            onClick={() => onPageSelect(pageNumber)}
            disabled={loading}
          >
            {pageNumber + 1}
          </Button>
        ))}
        <Button
          variant="primary"
          onClick={onNext}
          disabled={!hasNext || loading}
          className="h-8 rounded-lg px-3 py-1.5 text-[13px]"
        >
          다음
        </Button>
      </div>
    </Panel>
  );
}
