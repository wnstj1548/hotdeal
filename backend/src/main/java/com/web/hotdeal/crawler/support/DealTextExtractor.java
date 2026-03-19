package com.web.hotdeal.crawler.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DealTextExtractor {
    private static final Pattern TITLE_PATTERN = Pattern.compile("^\\s*\\[(.+?)]\\s*(.*?)\\s*(?:\\(([^/\\)]+)\\s*/\\s*([^\\)]+)\\))?\\s*$");

    private DealTextExtractor() {
    }

    public static ParsedTitle parse(String rawTitle) {
        String title = CrawlerUtils.cleanText(rawTitle);
        if (title == null) {
            return new ParsedTitle(null, null, null, null);
        }

        Matcher matcher = TITLE_PATTERN.matcher(title);
        if (!matcher.matches()) {
            return new ParsedTitle(null, title, null, null);
        }
        String mall = CrawlerUtils.cleanText(matcher.group(1));
        String cleanTitle = CrawlerUtils.cleanText(matcher.group(2));
        String price = CrawlerUtils.cleanText(matcher.group(3));
        String shipping = CrawlerUtils.cleanText(matcher.group(4));
        return new ParsedTitle(mall, cleanTitle, price, shipping);
    }

    public record ParsedTitle(
            String mallName,
            String title,
            String priceText,
            String shippingText
    ) {
    }
}
