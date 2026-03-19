package com.web.hotdeal.crawler.service.impl;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.crawler.service.AbstractJsoupCrawler;
import com.web.hotdeal.crawler.service.CrawlIncrementalService;
import com.web.hotdeal.crawler.support.CrawlerUtils;
import com.web.hotdeal.crawler.support.DealTextExtractor;
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
public class EomisaeCrawler extends AbstractJsoupCrawler {
    private static final String LIST_URL = "https://eomisae.co.kr/os";
    private static final String BASE_URL = "https://eomisae.co.kr";
    private static final Pattern ID_PATTERN = Pattern.compile("^/os/(\\d+)$");
    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yy.MM.dd");

    public EomisaeCrawler(CrawlerProperties crawlerProperties, CrawlIncrementalService crawlIncrementalService) {
        super(crawlerProperties, crawlIncrementalService);
    }

    @Override
    public DealSource source() {
        return DealSource.EOMISAE;
    }

    @Override
    public List<CrawledDeal> crawl() {
        Document document = fetch(LIST_URL);
        LocalDateTime incrementalCutoff = resolveIncrementalCutoff();
        List<CrawledDeal> deals = new ArrayList<>();

        for (Element item : document.select("div.card_el")) {
            if (deals.size() >= maxItems()) {
                break;
            }

            Element titleAnchor = item.selectFirst("h3 a[href^=/os/]");
            if (titleAnchor == null) {
                continue;
            }

            String href = CrawlerUtils.attr(titleAnchor, "href");
            String sourcePostId = extractId(href);
            if (sourcePostId == null) {
                continue;
            }

            DealTextExtractor.ParsedTitle parsedTitle = DealTextExtractor.parse(CrawlerUtils.text(titleAnchor));
            String title = parsedTitle.title() != null ? parsedTitle.title() : CrawlerUtils.text(titleAnchor);
            String url = CrawlerUtils.absoluteUrl(BASE_URL, href);
            String thumbnailUrl = CrawlerUtils.absoluteUrl(BASE_URL, CrawlerUtils.attr(item.selectFirst("img.tmb"), "src"));
            String category = normalizeCategory(CrawlerUtils.text(item.selectFirst("span.cate")));
            String dateText = parseDateText(item);
            LocalDateTime postedAt = parsePostedAt(dateText);
            if (shouldStopOnIncrementalWindow(incrementalCutoff, postedAt, sourcePostId, deals.size())) {
                break;
            }

            Integer likeCount = extractCounter(item, "ion-ios-heart");
            Integer replyCount = extractCounter(item, "ion-ios-chatbubble");
            Integer viewCount = extractCounter(item, "ion-ios-eye");

            deals.add(new CrawledDeal(
                    source(),
                    sourcePostId,
                    title,
                    url,
                    thumbnailUrl,
                    parsedTitle.mallName(),
                    category,
                    parsedTitle.priceText(),
                    parsedTitle.shippingText(),
                    postedAt,
                    likeCount,
                    replyCount,
                    viewCount,
                    null,
                    false,
                    truncate(item.outerHtml(), 3000)
            ));
        }

        return deals;
    }

    private String extractId(String href) {
        if (href == null) {
            return null;
        }
        Matcher matcher = ID_PATTERN.matcher(href);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String normalizeCategory(String categoryText) {
        if (categoryText == null) {
            return null;
        }
        return categoryText.replace(",", "").trim();
    }

    private String parseDateText(Element item) {
        List<Element> spans = item.select("div.card_content > p > span");
        if (spans.size() < 2) {
            return null;
        }
        return CrawlerUtils.text(spans.get(1));
    }

    private Integer extractCounter(Element item, String iconClass) {
        for (Element span : item.select(".infos span")) {
            if (span.html().contains(iconClass)) {
                return CrawlerUtils.parseCount(CrawlerUtils.cleanText(span.text()));
            }
        }
        return null;
    }

    private LocalDateTime parsePostedAt(String value) {
        String clean = CrawlerUtils.cleanText(value);
        if (clean == null) {
            return null;
        }
        try {
            if (clean.matches("\\d{2}\\.\\d{2}\\.\\d{2}")) {
                return LocalDate.parse(clean, YYMMDD).atStartOfDay();
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
