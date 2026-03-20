package com.web.hotdeal.deal.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PriceParser {
    private static final Pattern MAN_UNIT_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*만");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*([kKmM]?)");

    private PriceParser() {
    }

    public static Integer parsePriceValue(String rawPrice) {
        if (rawPrice == null) {
            return null;
        }

        String clean = rawPrice
                .replace('\u00A0', ' ')
                .replace(",", "")
                .trim();
        if (clean.isBlank()) {
            return null;
        }
        if (clean.contains("무료")) {
            return 0;
        }

        Matcher manMatcher = MAN_UNIT_PATTERN.matcher(clean);
        if (manMatcher.find()) {
            Double parsed = parseDouble(manMatcher.group(1));
            if (parsed != null) {
                return (int) Math.round(parsed * 10_000);
            }
        }

        Matcher numberMatcher = NUMBER_PATTERN.matcher(clean);
        if (!numberMatcher.find()) {
            return null;
        }

        Double parsed = parseDouble(numberMatcher.group(1));
        if (parsed == null) {
            return null;
        }
        String suffix = numberMatcher.group(2).toLowerCase();
        if ("k".equals(suffix)) {
            parsed *= 1_000;
        } else if ("m".equals(suffix)) {
            parsed *= 1_000_000;
        }

        return (int) Math.round(parsed);
    }

    private static Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
