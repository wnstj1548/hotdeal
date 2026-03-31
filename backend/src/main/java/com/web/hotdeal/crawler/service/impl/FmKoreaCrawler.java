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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FmKoreaCrawler extends AbstractJsoupCrawler {
    private static final String LIST_URL = "https://www.fmkorea.com/hotdeal";
    private static final String BASE_URL = "https://www.fmkorea.com";
    private static final Pattern ID_PATTERN = Pattern.compile("^/(\\d+)$");
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MMDD = DateTimeFormatter.ofPattern("MM.dd");
    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yy.MM.dd");

    public FmKoreaCrawler(
            CrawlerProperties crawlerProperties,
            CrawlIncrementalService crawlIncrementalService,
            RobotsPolicyService robotsPolicyService
    ) {
        super(crawlerProperties, crawlIncrementalService, robotsPolicyService);
    }

    @Override
    public DealSource source() {
        return DealSource.FMKOREA;
    }

    @Override
    public List<CrawledDeal> crawl() {
        Document document = fetch(LIST_URL);
        LocalDateTime incrementalCutoff = resolveIncrementalCutoff();
        List<CrawledDeal> deals = new ArrayList<>();

        for (Element item : selectDealItems(document)) {
            if (deals.size() >= maxItems()) {
                break;
            }

            Element titleAnchor = item.selectFirst("h3.title a[href^=/]");
            if (titleAnchor == null) {
                continue;
            }

            String href = CrawlerUtils.attr(titleAnchor, "href");
            String sourcePostId = extractId(href);
            if (sourcePostId == null) {
                continue;
            }

            String url = CrawlerUtils.absoluteUrl(BASE_URL, href);
            String title = CrawlerUtils.text(titleAnchor.selectFirst(".ellipsis-target"));
            if (title == null) {
                title = CrawlerUtils.text(titleAnchor);
            }

            String thumbnailUrl = firstNonBlank(
                    CrawlerUtils.attr(item.selectFirst("img.thumb"), "data-original"),
                    CrawlerUtils.attr(item.selectFirst("img.thumb"), "src")
            );
            thumbnailUrl = CrawlerUtils.absoluteUrl(BASE_URL, thumbnailUrl);

            String category = CrawlerUtils.text(item.selectFirst("span.category a"));
            String mallName = extractField(item, "쇼핑몰");
            String priceText = extractField(item, "가격");
            String shippingText = extractField(item, "배송");
            Integer likeCount = CrawlerUtils.parseCount(CrawlerUtils.text(item.selectFirst("a.pc_voted_count .count")));
            Integer replyCount = CrawlerUtils.parseCount(CrawlerUtils.text(item.selectFirst("span.comment_count")));
            LocalDateTime postedAt = parsePostedAt(CrawlerUtils.text(item.selectFirst("span.regdate")));
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
                    likeCount,
                    replyCount,
                    null,
                    null,
                    false,
                    truncate(item.outerHtml(), 3000)
            ));
        }

        return deals;
    }

    private List<Element> selectDealItems(Document document) {
        return document.select("li[class*='li_best2_']");
    }

    private String extractId(String href) {
        if (href == null) {
            return null;
        }
        Matcher matcher = ID_PATTERN.matcher(href);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractField(Element item, String keyword) {
        for (Element span : item.select("div.hotdeal_info span")) {
            String text = CrawlerUtils.cleanText(span.text());
            if (text == null || !text.contains(keyword)) {
                continue;
            }
            return CrawlerUtils.cleanText(text.replace(keyword, "").replace(":", "").trim());
        }
        return null;
    }

    private LocalDateTime parsePostedAt(String rawDate) {
        String value = CrawlerUtils.cleanText(rawDate);
        if (value == null) {
            return null;
        }
        try {
            if (value.contains(":")) {
                LocalTime localTime = LocalTime.parse(value, HHMM);
                return CrawlerUtils.normalizeTodayTime(LocalDate.now().atTime(localTime));
            }
            if (value.matches("\\d{2}\\.\\d{2}")) {
                return LocalDate.parse(value, MMDD).withYear(LocalDate.now().getYear()).atStartOfDay();
            }
            if (value.matches("\\d{2}\\.\\d{2}\\.\\d{2}")) {
                return LocalDate.parse(value, YYMMDD).atStartOfDay();
            }
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
