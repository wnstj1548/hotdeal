export function PageHeader() {
  return (
    <header className="bg-canvas px-4 pb-16 pt-20 text-center sm:px-6 sm:pb-20">
      <p className="apple-tight text-[14px] font-semibold text-primary">Hotdeal Curated</p>
      <h1 className="apple-display mt-2 text-[40px] font-semibold leading-[1.1] text-ink sm:text-[56px]">
        핫딜 모아보기
      </h1>
      <p className="mx-auto mt-4 max-w-3xl text-[21px] font-normal leading-[1.25] text-ink">
        실시간으로 올라오는 딜을 한 화면에서 탐색하고, 조건 필터로 필요한 상품만 빠르게 찾습니다.
      </p>
      <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
        <a
          href="#deals"
          className="apple-focus-ring inline-flex h-11 items-center rounded-full bg-primary px-6 text-[17px] font-normal text-white transition-transform duration-150 active:scale-95"
        >
          딜 보기
        </a>
        <a
          href="#popular"
          className="apple-focus-ring inline-flex h-11 items-center rounded-full border border-primary px-6 text-[17px] font-normal text-primary transition-transform duration-150 active:scale-95"
        >
          인기 차트
        </a>
      </div>
    </header>
  );
}
