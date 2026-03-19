package com.web.hotdeal.crawler.service.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import com.web.hotdeal.commons.config.CrawlerProperties;
import com.web.hotdeal.crawler.model.CrawledDeal;
import com.web.hotdeal.crawler.service.AbstractJsoupCrawler;
import com.web.hotdeal.crawler.support.CrawlerUtils;
import com.web.hotdeal.deal.model.DealSource;
import org.jsoup.Jsoup;
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
import java.util.Map;
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
    private final CrawlerProperties crawlerProperties;

    public FmKoreaCrawler(CrawlerProperties crawlerProperties) {
        super(crawlerProperties);
        this.crawlerProperties = crawlerProperties;
    }

    @Override
    public DealSource source() {
        return DealSource.FMKOREA;
    }

    @Override
    public List<CrawledDeal> crawl() {
        Document document = fetchWithPlaywright(LIST_URL);
        List<CrawledDeal> deals = new ArrayList<>();

        for (Element item : document.select("li[class*='li_best2_']")) {
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
            String mallName = parseField(item, "쇼핑몰:");
            String priceText = parseField(item, "가격:");
            String shippingText = parseField(item, "배송:");
            Integer likeCount = CrawlerUtils.parseCount(CrawlerUtils.text(item.selectFirst("a.pc_voted_count .count")));
            Integer replyCount = CrawlerUtils.parseCount(CrawlerUtils.text(item.selectFirst("span.comment_count")));
            LocalDateTime postedAt = parsePostedAt(CrawlerUtils.text(item.selectFirst("span.regdate")));

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

    private Document fetchWithPlaywright(String url) {
        double timeoutMs = Math.max(crawlerProperties.getTimeoutMs(), 10_000);
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = launchBrowser(playwright)) {
                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                        .setUserAgent(crawlerProperties.getUserAgent())
                        .setLocale("ko-KR")
                        .setTimezoneId("Asia/Seoul")
                        .setViewportSize(1366, 768)
                        .setExtraHTTPHeaders(Map.of(
                                "Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8",
                                "Referer", "https://www.fmkorea.com/"
                        ));

                try (BrowserContext context = browser.newContext(contextOptions)) {
                    Page page = context.newPage();
                    page.setDefaultNavigationTimeout(timeoutMs);
                    page.setDefaultTimeout(timeoutMs);
                    page.navigate(url, new Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                            .setTimeout(timeoutMs));
                    page.waitForSelector("li[class*='li_best2_']", new Page.WaitForSelectorOptions()
                            .setTimeout(timeoutMs));
                    return Jsoup.parse(page.content(), BASE_URL);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch " + url + " with Playwright", e);
        }
    }

    private Browser launchBrowser(Playwright playwright) {
        BrowserType.LaunchOptions baseOptions = new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage"));
        try {
            return playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setChannel("msedge")
                    .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage")));
        } catch (Exception ignored) {
            return playwright.chromium().launch(baseOptions);
        }
    }

    private String extractId(String href) {
        if (href == null) {
            return null;
        }
        Matcher matcher = ID_PATTERN.matcher(href);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String parseField(Element item, String label) {
        for (Element span : item.select("div.hotdeal_info span")) {
            String text = CrawlerUtils.cleanText(span.text());
            if (text != null && text.startsWith(label)) {
                return CrawlerUtils.cleanText(text.replace(label, "").trim());
            }
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
