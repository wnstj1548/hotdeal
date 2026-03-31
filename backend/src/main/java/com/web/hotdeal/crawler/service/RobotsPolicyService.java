package com.web.hotdeal.crawler.service;

import com.web.hotdeal.commons.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RobotsPolicyService {
    private static final RobotsRules ALLOW_ALL_RULES = new RobotsRules(Map.of());
    private static final Pattern DIRECTIVE_PATTERN = Pattern.compile("^([A-Za-z-]+)\\s*:\\s*(.*)$");

    private final CrawlerProperties crawlerProperties;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final Map<String, CachedRules> cache = new ConcurrentHashMap<>();

    public RobotsDecision evaluate(String targetUrl, String crawlerUserAgent) {
        if (!crawlerProperties.isRespectRobotsTxt()) {
            return RobotsDecision.allow(0L);
        }

        URI uri;
        try {
            uri = URI.create(targetUrl);
        } catch (Exception e) {
            return failClosed("Invalid crawl target url: " + targetUrl);
        }

        if (uri.getHost() == null || uri.getScheme() == null) {
            return failClosed("Invalid crawl target host: " + targetUrl);
        }

        RobotsRules rules = resolveRules(uri);
        String pathAndQuery = toPathAndQuery(uri);
        return rules.evaluate(pathAndQuery, normalizeUserAgent(crawlerUserAgent));
    }

    private RobotsRules resolveRules(URI uri) {
        String key = cacheKey(uri);
        long now = System.currentTimeMillis();

        CachedRules cached = cache.get(key);
        if (cached != null && now < cached.expiresAtEpochMs()) {
            return cached.rules();
        }

        RobotsRules fetched = fetchRules(uri);
        long ttlMs = Math.max(5_000L, crawlerProperties.getRobotsCacheTtlMs());
        cache.put(key, new CachedRules(fetched, now + ttlMs));
        return fetched;
    }

    private RobotsRules fetchRules(URI targetUri) {
        URI robotsUri;
        try {
            robotsUri = new URI(
                    targetUri.getScheme(),
                    targetUri.getAuthority(),
                    "/robots.txt",
                    null,
                    null
            );
        } catch (Exception e) {
            return failClosedRules("Failed to build robots.txt url for " + targetUri);
        }

        HttpRequest request = HttpRequest.newBuilder(robotsUri)
                .timeout(Duration.ofMillis(Math.max(1_000, crawlerProperties.getRobotsTimeoutMs())))
                .header("User-Agent", crawlerProperties.getUserAgent())
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 404) {
                return ALLOW_ALL_RULES;
            }
            if (status < 200 || status >= 300) {
                return failClosedRules("Failed to fetch robots.txt (" + status + ") from " + robotsUri);
            }
            return parseRobots(response.body());
        } catch (Exception e) {
            return failClosedRules("Failed to fetch robots.txt from " + robotsUri + ": " + e.getMessage());
        }
    }

    private RobotsRules parseRobots(String content) {
        if (content == null || content.isBlank()) {
            return ALLOW_ALL_RULES;
        }

        Map<String, MutableGroup> groups = new HashMap<>();
        List<String> currentAgents = new ArrayList<>();
        boolean inRuleSection = false;

        for (String rawLine : content.split("\\R")) {
            String line = stripComments(rawLine);
            if (line.isBlank()) {
                if (inRuleSection) {
                    currentAgents.clear();
                    inRuleSection = false;
                }
                continue;
            }

            java.util.regex.Matcher matcher = DIRECTIVE_PATTERN.matcher(line);
            if (!matcher.find()) {
                continue;
            }

            String directive = matcher.group(1).trim().toLowerCase(Locale.ROOT);
            String value = matcher.group(2).trim();
            switch (directive) {
                case "user-agent" -> {
                    if (inRuleSection) {
                        currentAgents.clear();
                        inRuleSection = false;
                    }
                    if (!value.isBlank()) {
                        currentAgents.add(value.toLowerCase(Locale.ROOT));
                    }
                }
                case "allow", "disallow", "crawl-delay" -> {
                    if (currentAgents.isEmpty()) {
                        currentAgents.add("*");
                    }
                    inRuleSection = true;
                    for (String agent : currentAgents) {
                        MutableGroup group = groups.computeIfAbsent(agent, ignored -> new MutableGroup());
                        if ("crawl-delay".equals(directive)) {
                            group.crawlDelayMs = parseCrawlDelayMs(value);
                            continue;
                        }
                        Rule rule = Rule.of("allow".equals(directive), value);
                        if (rule != null) {
                            group.rules.add(rule);
                        }
                    }
                }
                default -> {
                    // Ignore unsupported directives.
                }
            }
        }

        Map<String, GroupRules> result = new HashMap<>();
        for (Map.Entry<String, MutableGroup> entry : groups.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toGroupRules());
        }
        return new RobotsRules(result);
    }

    private long parseCrawlDelayMs(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            double seconds = Double.parseDouble(value.trim());
            if (seconds <= 0) {
                return 0L;
            }
            return (long) Math.ceil(seconds * 1_000D);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private String stripComments(String line) {
        if (line == null) {
            return "";
        }
        int idx = line.indexOf('#');
        String noComment = idx >= 0 ? line.substring(0, idx) : line;
        return noComment.trim();
    }

    private RobotsDecision failClosed(String reason) {
        if (crawlerProperties.isRobotsFailClosed()) {
            return RobotsDecision.block(reason);
        }
        log.warn("[Robots] fail-open: {}", reason);
        return RobotsDecision.allow(0L);
    }

    private RobotsRules failClosedRules(String reason) {
        if (crawlerProperties.isRobotsFailClosed()) {
            log.warn("[Robots] fail-closed: {}", reason);
            return new RobotsRules(Map.of("*", GroupRules.blockAll()));
        }
        log.warn("[Robots] fail-open: {}", reason);
        return ALLOW_ALL_RULES;
    }

    private String cacheKey(URI uri) {
        int port = uri.getPort();
        String authority = port < 0 ? uri.getHost() : uri.getHost() + ":" + port;
        return uri.getScheme().toLowerCase(Locale.ROOT) + "://" + authority.toLowerCase(Locale.ROOT);
    }

    private String normalizeUserAgent(String userAgent) {
        if (userAgent == null) {
            return "";
        }
        return userAgent.toLowerCase(Locale.ROOT);
    }

    private String toPathAndQuery(URI uri) {
        String path = (uri.getRawPath() == null || uri.getRawPath().isBlank()) ? "/" : uri.getRawPath();
        if (uri.getRawQuery() == null || uri.getRawQuery().isBlank()) {
            return path;
        }
        return path + "?" + uri.getRawQuery();
    }

    public record RobotsDecision(boolean allowed, long crawlDelayMs, String reason) {
        public static RobotsDecision allow(long crawlDelayMs) {
            return new RobotsDecision(true, Math.max(0L, crawlDelayMs), null);
        }

        public static RobotsDecision block(String reason) {
            return new RobotsDecision(false, 0L, reason == null ? "Blocked by robots policy" : reason);
        }
    }

    private record CachedRules(RobotsRules rules, long expiresAtEpochMs) {
    }

    private static final class RobotsRules {
        private final Map<String, GroupRules> groups;

        private RobotsRules(Map<String, GroupRules> groups) {
            this.groups = groups;
        }

        private RobotsDecision evaluate(String pathAndQuery, String userAgent) {
            GroupRules rules = selectGroup(userAgent);
            if (rules == null) {
                return RobotsDecision.allow(0L);
            }
            boolean allowed = rules.isAllowed(pathAndQuery);
            return allowed
                    ? RobotsDecision.allow(rules.crawlDelayMs)
                    : RobotsDecision.block("Blocked by robots.txt path rule for " + pathAndQuery);
        }

        private GroupRules selectGroup(String userAgent) {
            GroupRules best = null;
            int bestTokenLength = -1;
            GroupRules wildcard = groups.get("*");

            for (Map.Entry<String, GroupRules> entry : groups.entrySet()) {
                String token = entry.getKey();
                if ("*".equals(token) || token.isBlank()) {
                    continue;
                }
                if (!userAgent.contains(token)) {
                    continue;
                }
                if (token.length() > bestTokenLength) {
                    bestTokenLength = token.length();
                    best = entry.getValue();
                }
            }

            return best != null ? best : wildcard;
        }
    }

    private static final class MutableGroup {
        private final List<Rule> rules = new ArrayList<>();
        private long crawlDelayMs = 0L;

        private GroupRules toGroupRules() {
            return new GroupRules(List.copyOf(rules), Math.max(0L, crawlDelayMs));
        }
    }

    private static final class GroupRules {
        private final List<Rule> rules;
        private final long crawlDelayMs;

        private GroupRules(List<Rule> rules, long crawlDelayMs) {
            this.rules = rules;
            this.crawlDelayMs = crawlDelayMs;
        }

        private static GroupRules blockAll() {
            return new GroupRules(List.of(new Rule(false, "/", 1, Pattern.compile("^/.*"))), 0L);
        }

        private boolean isAllowed(String pathAndQuery) {
            Rule best = null;
            for (Rule rule : rules) {
                if (!rule.matches(pathAndQuery)) {
                    continue;
                }
                if (best == null || rule.priority() > best.priority()
                        || (rule.priority() == best.priority() && rule.allow())) {
                    best = rule;
                }
            }
            return best == null || best.allow();
        }
    }

    private record Rule(boolean allow, String rawPath, int priority, Pattern regex) {
        private static Rule of(boolean allow, String rawPath) {
            if (rawPath == null) {
                return null;
            }
            String path = rawPath.trim();
            if (path.isEmpty()) {
                return null;
            }
            int priority = path.replace("*", "").replace("$", "").length();
            Pattern regex = toRegex(path);
            return new Rule(allow, path, priority, regex);
        }

        private static Pattern toRegex(String rawPath) {
            StringBuilder builder = new StringBuilder("^");
            for (int i = 0; i < rawPath.length(); i++) {
                char ch = rawPath.charAt(i);
                if (ch == '*') {
                    builder.append(".*");
                    continue;
                }
                if (ch == '$' && i == rawPath.length() - 1) {
                    builder.append('$');
                    continue;
                }
                builder.append(Pattern.quote(String.valueOf(ch)));
            }
            if (!rawPath.endsWith("$")) {
                builder.append(".*");
            }
            return Pattern.compile(builder.toString());
        }

        private boolean matches(String pathAndQuery) {
            return regex.matcher(pathAndQuery).matches();
        }
    }
}
