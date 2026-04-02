package com.web.hotdeal.crawler.service.impl;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.crawler.service.AbstractJsoupCrawler;
import com.web.hotdeal.crawler.service.CrawlIncrementalService;
import com.web.hotdeal.crawler.service.PlaywrightFetcher;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuliwebCrawler extends AbstractJsoupCrawler {
    private static final String LIST_URL = "https://bbs.ruliweb.com/market/board/1020";
    private static final String BASE_URL = "https://bbs.ruliweb.com";
    private static final Pattern ID_PATTERN = Pattern.compile("/read/(\\d+)");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d[\\d,]*(?:\\.\\d+)?\\s*(?:원|만))");
    private static final DateTimeFormatter YYYYMMDD_HHMM = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final DateTimeFormatter YYYYMMDD_DOT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter YYMMDD_DOT = DateTimeFormatter.ofPattern("yy.MM.dd");
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    public RuliwebCrawler(
            CrawlerProperties crawlerProperties,
            CrawlIncrementalService crawlIncrementalService,
            RobotsPolicyService robotsPolicyService,
            PlaywrightFetcher playwrightFetcher
    ) {
        super(crawlerProperties, crawlIncrementalService, robotsPolicyService, playwrightFetcher);
    }

    @Override
    public DealSource source() {
        return DealSource.RULIWEB;
    }

    @Override
    public List<CrawledDeal> crawl() {
        Document document = fetch(LIST_URL);
        LocalDateTime incrementalCutoff = resolveIncrementalCutoff();
        List<CrawledDeal> deals = new ArrayList<>();
        Set<String> seenPostIds = new HashSet<>();

        for (Element anchor : document.select("a[href*='/market/board/1020/read/'], a[href*='/board/1020/read/']")) {
            if (deals.size() >= maxItems()) {
                break;
            }

            String href = CrawlerUtils.attr(anchor, "href");
            String sourcePostId = extractPostId(href);
            if (sourcePostId == null || !seenPostIds.add(sourcePostId)) {
                continue;
            }

            String title = CrawlerUtils.cleanText(anchor.text());
            if (title == null || title.length() < 2) {
                continue;
            }

            Element row = firstNonNull(anchor.closest("tr"), anchor.closest("li"), anchor.closest("article"), anchor.parent());
            String rowText = CrawlerUtils.cleanText(row == null ? null : row.text());
            String url = CrawlerUtils.absoluteUrl(BASE_URL, href);
            String category = extractCategory(row);
            String priceText = extractPriceText(title, rowText);
            LocalDateTime postedAt = parsePostedAt(row);
            if (shouldStopOnIncrementalWindow(incrementalCutoff, postedAt, sourcePostId, deals.size())) {
                break;
            }

            Boolean ended = containsAny(title, rowText, "종료", "마감", "품절");
            Boolean hot = containsAny(title, rowText, "HOT", "인기");

            deals.add(new CrawledDeal(
                    source(),
                    sourcePostId,
                    title,
                    url,
                    extractThumbnail(row),
                    null,
                    category,
                    priceText,
                    null,
                    postedAt,
                    extractCounter(row, "추천", "up", "like"),
                    extractCounter(row, "댓글", "reply", "comment"),
                    extractCounter(row, "조회", "view", "hit"),
                    hot,
                    ended,
                    truncate(row == null ? anchor.outerHtml() : row.outerHtml(), 3000)
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

    private String extractCategory(Element row) {
        if (row == null) {
            return null;
        }
        String category = CrawlerUtils.text(row.selectFirst("td.divsn, .board_list_type, .board_list_label, .category"));
        if (category == null) {
            return null;
        }
        return category.replace("[", "").replace("]", "").trim();
    }

    private String extractPriceText(String title, String rowText) {
        String merged = (title == null ? "" : title) + " " + (rowText == null ? "" : rowText);
        Matcher matcher = PRICE_PATTERN.matcher(merged);
        if (matcher.find()) {
            return CrawlerUtils.cleanText(matcher.group(1));
        }
        return null;
    }

    private String extractThumbnail(Element row) {
        if (row == null) {
            return null;
        }
        String src = CrawlerUtils.attr(row.selectFirst("img"), "src");
        return CrawlerUtils.absoluteUrl(BASE_URL, src);
    }

    private Integer extractCounter(Element row, String... keywords) {
        if (row == null || keywords.length == 0) {
            return null;
        }

        String rowText = CrawlerUtils.cleanText(row.text());
        if (rowText == null) {
            return null;
        }

        for (String keyword : keywords) {
            int keywordIndex = rowText.indexOf(keyword);
            if (keywordIndex < 0) {
                continue;
            }
            String suffix = rowText.substring(keywordIndex);
            Integer parsed = CrawlerUtils.parseCount(suffix);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private LocalDateTime parsePostedAt(Element row) {
        if (row == null) {
            return null;
        }

        String[] candidates = new String[]{
                CrawlerUtils.attr(row.selectFirst("time"), "datetime"),
                CrawlerUtils.text(row.selectFirst("time")),
                CrawlerUtils.text(row.selectFirst(".time, .regdate, .datetime")),
                CrawlerUtils.text(row.selectFirst("td.time, td.date"))
        };
        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            LocalDateTime parsed = parsePostedAtText(candidate);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private LocalDateTime parsePostedAtText(String value) {
        String clean = CrawlerUtils.cleanText(value);
        if (clean == null) {
            return null;
        }

        LocalDateTime relative = CrawlerUtils.parseRelativeKorean(clean);
        if (relative != null) {
            return relative;
        }

        try {
            if (clean.matches("\\d{4}\\.\\d{2}\\.\\d{2}\\s+\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(clean, YYYYMMDD_HHMM);
            }
            if (clean.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
                return LocalDate.parse(clean, YYYYMMDD_DOT).atStartOfDay();
            }
            if (clean.matches("\\d{2}\\.\\d{2}\\.\\d{2}")) {
                return LocalDate.parse(clean, YYMMDD_DOT).atStartOfDay();
            }
            if (clean.matches("\\d{2}\\.\\d{2}\\s+\\d{2}:\\d{2}")) {
                LocalDate date = LocalDate.parse(clean.substring(0, 5), DateTimeFormatter.ofPattern("MM.dd"))
                        .withYear(LocalDate.now().getYear());
                LocalTime time = LocalTime.parse(clean.substring(6), HHMM);
                return CrawlerUtils.normalizeTodayTime(LocalDateTime.of(date, time));
            }
            if (clean.matches("\\d{2}:\\d{2}")) {
                return CrawlerUtils.normalizeTodayTime(LocalDate.now().atTime(LocalTime.parse(clean, HHMM)));
            }
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private boolean containsAny(String firstText, String secondText, String... keywords) {
        String merged = ((firstText == null ? "" : firstText) + " " + (secondText == null ? "" : secondText)).toLowerCase();
        for (String keyword : keywords) {
            if (merged.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Element firstNonNull(Element... candidates) {
        for (Element candidate : candidates) {
            if (candidate != null) {
                return candidate;
            }
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
