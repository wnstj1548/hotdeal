package com.web.hotdeal.crawler.service.impl;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.crawler.service.AbstractJsoupCrawler;
import com.web.hotdeal.crawler.service.CrawlIncrementalService;
import com.web.hotdeal.crawler.service.PlaywrightFetcher;
import com.web.hotdeal.crawler.service.RobotsPolicyService;
import com.web.hotdeal.crawler.support.CrawlerUtils;
import com.web.hotdeal.crawler.support.DealTextExtractor;
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
public class ClienCrawler extends AbstractJsoupCrawler {
    private static final String LIST_URL = "https://www.clien.net/service/board/jirum";
    private static final String BASE_URL = "https://www.clien.net";
    private static final Pattern ID_PATTERN = Pattern.compile("/service/board/jirum/(\\d+)");
    private static final Pattern TITLE_REPLY_PATTERN = Pattern.compile("\\[(\\d+)]");
    private static final DateTimeFormatter YYYYMMDD_HHMM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MMDD_DASH = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    public ClienCrawler(
            CrawlerProperties crawlerProperties,
            CrawlIncrementalService crawlIncrementalService,
            RobotsPolicyService robotsPolicyService,
            PlaywrightFetcher playwrightFetcher
    ) {
        super(crawlerProperties, crawlIncrementalService, robotsPolicyService, playwrightFetcher);
    }

    @Override
    public DealSource source() {
        return DealSource.CLIEN;
    }

    @Override
    public List<CrawledDeal> crawl() {
        Document document = fetch(LIST_URL);
        LocalDateTime incrementalCutoff = resolveIncrementalCutoff();
        List<CrawledDeal> deals = new ArrayList<>();
        Set<String> seenPostIds = new HashSet<>();

        for (Element item : document.select("div.list_item.symph_row, div.list_item, tr.posting")) {
            if (deals.size() >= maxItems()) {
                break;
            }

            Element titleAnchor = item.selectFirst(
                    "a.subject_fixed[href*='/service/board/jirum/'], " +
                            "a.list_subject[href*='/service/board/jirum/'], " +
                            "a[href*='/service/board/jirum/']"
            );
            if (titleAnchor == null) {
                continue;
            }

            String href = CrawlerUtils.attr(titleAnchor, "href");
            String sourcePostId = extractPostId(href);
            if (sourcePostId == null || !seenPostIds.add(sourcePostId)) {
                continue;
            }

            String rawTitle = CrawlerUtils.cleanText(titleAnchor.text());
            if (rawTitle == null || rawTitle.length() < 2) {
                continue;
            }
            DealTextExtractor.ParsedTitle parsedTitle = DealTextExtractor.parse(rawTitle);
            String title = parsedTitle.title() != null ? parsedTitle.title() : rawTitle;

            String url = CrawlerUtils.absoluteUrl(BASE_URL, href);
            LocalDateTime postedAt = parsePostedAt(item);
            if (shouldStopOnIncrementalWindow(incrementalCutoff, postedAt, sourcePostId, deals.size())) {
                break;
            }

            Integer replyCount = firstNonNull(
                    CrawlerUtils.parseCount(CrawlerUtils.text(item.selectFirst(".list_reply"))),
                    extractReplyCountFromTitle(rawTitle),
                    CrawlerUtils.parseCount(CrawlerUtils.text(item.selectFirst(".comment_count")))
            );
            Integer likeCount = CrawlerUtils.parseCount(CrawlerUtils.text(item.selectFirst(".list_symph, .symph_count")));
            Integer viewCount = CrawlerUtils.parseCount(CrawlerUtils.text(item.selectFirst(".list_hit, .hit")));
            String category = CrawlerUtils.text(item.selectFirst(".list_category, .category"));

            String metaText = CrawlerUtils.cleanText(item.text());
            Boolean ended = containsAny(title, metaText, "종료", "마감", "품절");
            Boolean hot = containsAny(title, metaText, "인기", "HOT");

            deals.add(new CrawledDeal(
                    source(),
                    sourcePostId,
                    title,
                    url,
                    CrawlerUtils.absoluteUrl(BASE_URL, CrawlerUtils.attr(item.selectFirst("img"), "src")),
                    parsedTitle.mallName(),
                    category,
                    parsedTitle.priceText(),
                    parsedTitle.shippingText(),
                    postedAt,
                    likeCount,
                    replyCount,
                    viewCount,
                    hot,
                    ended,
                    truncate(item.outerHtml(), 3000)
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

    private Integer extractReplyCountFromTitle(String title) {
        if (title == null) {
            return null;
        }
        Matcher matcher = TITLE_REPLY_PATTERN.matcher(title);
        if (!matcher.find()) {
            return null;
        }
        return CrawlerUtils.parseCount(matcher.group(1));
    }

    private LocalDateTime parsePostedAt(Element item) {
        String timeText = firstNonNull(
                CrawlerUtils.attr(item.selectFirst("time"), "datetime"),
                CrawlerUtils.text(item.selectFirst("time")),
                CrawlerUtils.text(item.selectFirst(".timestamp, .post_time, .list_time"))
        );
        return parsePostedAtText(timeText);
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
            if (clean.matches("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(clean, YYYYMMDD_HHMM);
            }
            if (clean.matches("\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}")) {
                String[] parts = clean.split("\\s+");
                LocalDate date = LocalDate.parse(parts[0], MMDD_DASH).withYear(LocalDate.now().getYear());
                LocalTime time = LocalTime.parse(parts[1], HHMM);
                return CrawlerUtils.normalizeTodayTime(LocalDateTime.of(date, time));
            }
            if (clean.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(clean, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
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

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
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
