package com.web.hotdeal.crawler.support;

import org.jsoup.nodes.Element;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CrawlerUtils {
    private static final Pattern DIGITS_ONLY = Pattern.compile("(\\d+)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(-?\\d+(?:[\\.,]\\d+)?)([kKmM]?)");
    private static final DateTimeFormatter YYMMDD_HHMMSS_DOT = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss");
    private static final DateTimeFormatter YYMMDD_SLASH = DateTimeFormatter.ofPattern("yy/MM/dd");
    private static final DateTimeFormatter YYMMDD_DOT = DateTimeFormatter.ofPattern("yy.MM.dd");
    private static final DateTimeFormatter MMDD_DOT = DateTimeFormatter.ofPattern("MM.dd");
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private CrawlerUtils() {
    }

    public static String text(Element element) {
        return element == null ? null : cleanText(element.text());
    }

    public static String attr(Element element, String attrName) {
        if (element == null) {
            return null;
        }
        String value = element.attr(attrName);
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static String cleanText(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public static String absoluteUrl(String baseUrl, String href) {
        if (href == null || href.isBlank()) {
            return null;
        }
        String value = href.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        if (value.startsWith("//")) {
            return "https:" + value;
        }
        if (value.startsWith("/")) {
            URI uri = URI.create(baseUrl);
            return uri.getScheme() + "://" + uri.getHost() + value;
        }
        if (value.startsWith("?")) {
            return baseUrl + value;
        }
        return baseUrl.endsWith("/") ? baseUrl + value : baseUrl + "/" + value;
    }

    public static String firstGroup(String input, Pattern pattern, int group) {
        if (input == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(group);
    }

    public static String digits(String input) {
        return firstGroup(input, DIGITS_ONLY, 1);
    }

    public static Integer parseCount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(value.replace(",", "").trim());
        if (!matcher.find()) {
            return null;
        }

        double parsed = Double.parseDouble(matcher.group(1).replace(",", "."));
        String suffix = matcher.group(2).toLowerCase(Locale.ROOT);
        if ("k".equals(suffix)) {
            parsed *= 1_000;
        } else if ("m".equals(suffix)) {
            parsed *= 1_000_000;
        }
        return (int) Math.round(parsed);
    }

    public static LocalDateTime parseByPatterns(String value, List<DateTimeFormatter> formatters) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String clean = cleanText(value);
        for (DateTimeFormatter formatter : formatters) {
            try {
                if (formatter == YYMMDD_HHMMSS_DOT) {
                    return LocalDateTime.parse(clean, formatter);
                }
                if (formatter == YYMMDD_SLASH) {
                    return LocalDate.parse(clean, formatter).atStartOfDay();
                }
                if (formatter == YYMMDD_DOT) {
                    return LocalDate.parse(clean, formatter).atStartOfDay();
                }
                if (formatter == MMDD_DOT) {
                    LocalDate parsedDate = LocalDate.parse(clean, formatter).withYear(LocalDate.now().getYear());
                    return parsedDate.atStartOfDay();
                }
                if (formatter == HHMM) {
                    LocalTime parsedTime = LocalTime.parse(clean, formatter);
                    return LocalDate.now().atTime(parsedTime);
                }
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    public static LocalDateTime parseRelativeKorean(String value) {
        if (value == null) {
            return null;
        }

        String clean = cleanText(value);
        if (clean == null) {
            return null;
        }

        Integer count = parseCount(clean);
        if (count == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (clean.contains("분 전")) {
            return now.minusMinutes(count);
        }
        if (clean.contains("시간 전")) {
            return now.minusHours(count);
        }
        if (clean.contains("일 전")) {
            return now.minusDays(count);
        }
        return null;
    }

    public static LocalDateTime normalizeTodayTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        if (dateTime.isAfter(LocalDateTime.now().plusMinutes(1))) {
            return dateTime.minusDays(1);
        }
        return dateTime;
    }
}
