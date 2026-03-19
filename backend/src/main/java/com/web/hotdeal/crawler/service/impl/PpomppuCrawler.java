package com.web.hotdeal.crawler.service.impl;

import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.crawler.service.AbstractJsoupCrawler;
import com.web.hotdeal.crawler.support.CrawlerUtils;
import com.web.hotdeal.crawler.support.DealTextExtractor;
import com.web.hotdeal.deal.model.DealSource;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PpomppuCrawler extends AbstractJsoupCrawler {
    private static final String LIST_URL = "https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu";
    private static final String BASE_URL = "https://www.ppomppu.co.kr/zboard";
    private static final Pattern NO_PATTERN = Pattern.compile("[?&]no=(\\d+)");
    private static final DateTimeFormatter DETAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss");
    private static final DateTimeFormatter YYMMDD_SLASH = DateTimeFormatter.ofPattern("yy/MM/dd");
    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public PpomppuCrawler(CrawlerProperties crawlerProperties) {
        super(crawlerProperties);
    }

    @Override
    public DealSource source() {
        return DealSource.PPOMPPU;
    }

    @Override
    public List<CrawledDeal> crawl() {
        Document document = fetch(LIST_URL);
        List<CrawledDeal> deals = new ArrayList<>();

        for (Element row : document.select("tr.baseList")) {
            if (deals.size() >= maxItems()) {
                break;
            }

            Element titleAnchor = row.selectFirst("a.baseList-title[href*='view.php?id=ppomppu']");
            if (titleAnchor == null) {
                continue;
            }

            String url = CrawlerUtils.absoluteUrl(BASE_URL, CrawlerUtils.attr(titleAnchor, "href"));
            String sourcePostId = extractPostId(row, url);
            if (sourcePostId == null) {
                continue;
            }

            DealTextExtractor.ParsedTitle parsedTitle = DealTextExtractor.parse(CrawlerUtils.text(titleAnchor));
            String title = parsedTitle.title() != null ? parsedTitle.title() : CrawlerUtils.text(titleAnchor);

            String category = normalizeCategory(CrawlerUtils.text(row.selectFirst("small.baseList-small")));
            String thumbnailUrl = CrawlerUtils.absoluteUrl(BASE_URL, CrawlerUtils.attr(row.selectFirst("a.baseList-thumb img"), "src"));
            String fullDateText = CrawlerUtils.attr(row.selectFirst("td[title]:has(time.baseList-time)"), "title");
            String shortDateText = CrawlerUtils.text(row.selectFirst("time.baseList-time"));

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
                    parsePostedAt(fullDateText, shortDateText),
                    null,
                    CrawlerUtils.parseCount(CrawlerUtils.text(row.selectFirst("span.baseList-c"))),
                    CrawlerUtils.parseCount(CrawlerUtils.text(row.selectFirst("td.baseList-views"))),
                    null,
                    false,
                    truncate(row.outerHtml(), 3000)
            ));
        }
        return deals;
    }

    private String extractPostId(Element row, String url) {
        String fromNoColumn = CrawlerUtils.digits(CrawlerUtils.text(row.selectFirst("td.baseList-numb")));
        if (fromNoColumn != null) {
            return fromNoColumn;
        }
        if (url == null) {
            return null;
        }
        Matcher matcher = NO_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String normalizeCategory(String categoryText) {
        if (categoryText == null) {
            return null;
        }
        return categoryText.replace("[", "").replace("]", "").trim();
    }

    private LocalDateTime parsePostedAt(String detailed, String shortDate) {
        if (detailed != null) {
            try {
                return LocalDateTime.parse(detailed, DETAIL_DATE_FORMATTER);
            } catch (DateTimeParseException ignored) {
            }
        }

        if (shortDate == null) {
            return null;
        }
        try {
            if (shortDate.contains(":")) {
                return CrawlerUtils.normalizeTodayTime(LocalDateTime.now().with(java.time.LocalTime.parse(shortDate, HHMMSS)));
            }
            return LocalDateTime.of(java.time.LocalDate.parse(shortDate, YYMMDD_SLASH), java.time.LocalTime.MIDNIGHT);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
