package com.web.hotdeal.deal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DealSource {
    PPOMPPU("뽐뿌", "https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu"),
    FMKOREA("에펨코리아", "https://www.fmkorea.com/hotdeal"),
    EOMISAE("어미새", "https://eomisae.co.kr/os"),
    QUASARZONE("퀘이사존", "https://quasarzone.com/bbs/qb_saleinfo"),
    RULIWEB("루리웹", "https://bbs.ruliweb.com/market/board/1020"),
    CLIEN("클리앙", "https://www.clien.net/service/board/jirum");

    private final String label;
    private final String sourceUrl;
}
