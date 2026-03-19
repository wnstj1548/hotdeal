package com.web.hotdeal.deal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DealSource {
    PPOMPPU("뽐뿌", "https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu"),
    FMKOREA("에펨코리아", "https://www.fmkorea.com/hotdeal"),
    EOMISAE("어미새", "https://eomisae.co.kr/os"),
    QUASARZONE("퀘이사존", "https://quasarzone.com/bbs/qb_saleinfo");

    private final String label;
    private final String sourceUrl;
}
