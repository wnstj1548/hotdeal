package com.web.hotdeal.crawler.service.impl;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.crawler.service.AbstractJsoupCrawler;
import com.web.hotdeal.crawler.service.CrawlIncrementalService;
import com.web.hotdeal.crawler.service.RobotsPolicyService;
import com.web.hotdeal.crawler.support.CrawlerUtils;
import com.web.hotdeal.deal.model.DealSource;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class QuasarzoneCrawler extends AbstractJsoupCrawler {
    private static final String LIST_URL = "https://quasarzone.com/bbs/qb_saleinfo";
    private static final String BASE_URL = "https://quasarzone.com";
    private static final Pattern ID_PATTERN = Pattern.compile("/bbs/qb_saleinfo/views/(\\d+)");
    private static final Pattern STYLE_URL_PATTERN = Pattern.compile("url\\((['\"]?)(.+?)\\1\\)");
    private static final DateTimeFormatter YYMMDD_DOT = DateTimeFormatter.ofPattern("yy.MM.dd");
    private static final DateTimeFormatter YYYYMMDD_DOT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public QuasarzoneCrawler(
            CrawlerProperties crawlerProperties,
            CrawlIncrementalService crawlIncrementalService,
            RobotsPolicyService robotsPolicyService
    ) {
        super(crawlerProperties, crawlIncrementalService, robotsPolicyService);
    }

    @Override
    public DealSource source() {
        return DealSource.QUASARZONE;
    }

    @Override
    public List<CrawledDeal> crawl() {
        Document document = fetch(LIST_URL);
        LocalDateTime incrementalCutoff = resolveIncrementalCutoff();
        List<CrawledDeal> deals = new ArrayList<>();

        for (Element row : document.select("div.market-type-list tbody tr")) {
            if (deals.size() >= maxItems()) {
                break;
            }

            Element titleAnchor = row.selectFirst("a.subject-link[href*='/bbs/qb_saleinfo/views/']");
            if (titleAnchor == null) {
                continue;
            }

            String href = CrawlerUtils.attr(titleAnchor, "href");
            String sourcePostId = extractPostId(href);
            if (sourcePostId == null) {
                continue;
            }

            String url = CrawlerUtils.absoluteUrl(BASE_URL, href);
            String title = CrawlerUtils.text(row.selectFirst(".ellipsis-with-reply-cnt"));
            if (title == null) {
                title = CrawlerUtils.text(titleAnchor);
            }

            String thumbnailUrl = extractThumbnail(row);
            String category = CrawlerUtils.text(row.selectFirst("span.category"));
            String mallName = CrawlerUtils.attr(row.selectFirst("span.brand img"), "alt");
            String priceText = extractSubField(row, "가격");
            String shippingText = extractSubField(row, "배송비");

            String labelText = CrawlerUtils.text(row.selectFirst("p.tit .label"));
            Boolean ended = labelText != null && (labelText.contains("종료") || labelText.contains("마감"));
            Boolean hot = labelText != null && (labelText.contains("인기") || labelText.contains("HOT"));

            LocalDateTime postedAt = parsePostedAt(CrawlerUtils.text(row.selectFirst("span.date")));
            if (shouldStopOnIncrementalWindow(incrementalCutoff, postedAt, sourcePostId, deals.size())) {
                break;
            }

            deals.add(new CrawledDeal(
                    source(),
                    sourcePostId,
                    title,
                    url,
                    thumbnailUrl,
                    mallName,
                    category,
                    priceText,
                    shippingText,
                    postedAt,
                    CrawlerUtils.parseCount(CrawlerUtils.text(row.selectFirst("td .num"))),
                    CrawlerUtils.parseCount(CrawlerUtils.text(row.selectFirst(".board-list-comment .ctn-count"))),
                    CrawlerUtils.parseCount(CrawlerUtils.text(row.selectFirst("span.count"))),
                    hot,
                    ended,
                    truncate(row.outerHtml(), 3000)
            ));
        }

        return deals;
    }

    private String extractPostId(String href) {
        if (href == null) {
            return null;
        }
        Matcher matcher = ID_PATTERN.matcher(href);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractThumbnail(Element row) {
        Element thumbStyle = row.selectFirst("a.thumb .img-background-wrap");
        String style = CrawlerUtils.attr(thumbStyle, "style");
        if (style != null) {
            Matcher matcher = STYLE_URL_PATTERN.matcher(style);
            if (matcher.find()) {
                return CrawlerUtils.absoluteUrl(BASE_URL, matcher.group(2));
            }
        }
        return CrawlerUtils.absoluteUrl(BASE_URL, CrawlerUtils.attr(row.selectFirst("a.thumb img.maxImg"), "src"));
    }

    private String extractSubField(Element row, String label) {
        for (Element span : row.select("div.market-info-sub p span")) {
            String text = CrawlerUtils.cleanText(span.text());
            if (text != null && text.startsWith(label)) {
                return CrawlerUtils.cleanText(text.replace(label, "").replace(":", "").trim());
            }
        }
        return null;
    }

    private LocalDateTime parsePostedAt(String rawDate) {
        String clean = CrawlerUtils.cleanText(rawDate);
        if (clean == null) {
            return null;
        }

        LocalDateTime relative = CrawlerUtils.parseRelativeKorean(clean);
        if (relative != null) {
            return relative;
        }

        try {
            if (clean.matches("\\d{2}\\.\\d{2}\\.\\d{2}")) {
                return LocalDate.parse(clean, YYMMDD_DOT).atStartOfDay();
            }
            if (clean.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
                return LocalDate.parse(clean, YYYYMMDD_DOT).atStartOfDay();
            }
        } catch (DateTimeParseException ignored) {
        }

        return null;
    }

    private String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
