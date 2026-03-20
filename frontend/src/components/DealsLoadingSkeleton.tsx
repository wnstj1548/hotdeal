export function DealsLoadingSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 5 }).map((_, index) => (
        <div key={index} className="h-[118px] animate-pulse rounded-2xl border border-slate-200 bg-white/80" />
      ))}
    </div>
  );
}
