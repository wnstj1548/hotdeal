export function buildPageNumbers(totalPages: number, currentPage: number, maxButtons: number): number[] {
  if (totalPages <= 0) {
    return [0];
  }

  const safeMaxButtons = Math.max(1, maxButtons);
  const safeCurrentPage = Math.max(0, Math.min(currentPage, totalPages - 1));
  const half = Math.floor(safeMaxButtons / 2);

  let start = Math.max(0, safeCurrentPage - half);
  let end = start + safeMaxButtons - 1;
  if (end > totalPages - 1) {
    end = totalPages - 1;
    start = Math.max(0, end - safeMaxButtons + 1);
  }

  const pages: number[] = [];
  for (let page = start; page <= end; page++) {
    pages.push(page);
  }
  return pages;
}
