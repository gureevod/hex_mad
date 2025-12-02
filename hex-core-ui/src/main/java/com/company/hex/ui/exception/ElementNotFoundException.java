package com.company.hex.ui.exception;

import java.io.Serial;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Исключение при ненайденном элементе с детальной диагностикой.
 */
public class ElementNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int BOX_WIDTH = 68;
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final String elementName;
    private final String locator;
    private final String pageName;
    private final String componentName;
    private final Duration timeout;
    private final String currentUrl;
    private final String pageTitle;
    private final LocalDateTime timestamp;

    // Private constructor - используй Builder
    private ElementNotFoundException(Builder b, String message) {
        super(message, b.cause);
        this.elementName = b.elementName != null ? b.elementName : "Unknown";
        this.locator = b.locator != null ? b.locator : "Unknown";
        this.pageName = b.pageName != null ? b.pageName : "Unknown Page";
        this.componentName = b.componentName;
        this.timeout = b.timeout;
        this.currentUrl = b.currentUrl;
        this.pageTitle = b.pageTitle;
        this.timestamp = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String elementName;
        private String locator;
        private String pageName;
        private String componentName;
        private Duration timeout;
        private Throwable cause;
        private String currentUrl;
        private String pageTitle;

        public Builder elementName(String val) { this.elementName = val; return this; }
        public Builder locator(String val) { this.locator = val; return this; }
        public Builder pageName(String val) { this.pageName = val; return this; }
        public Builder componentName(String val) { this.componentName = val; return this; }
        public Builder timeout(Duration val) { this.timeout = val; return this; }
        public Builder cause(Throwable val) { this.cause = val; return this; }
        public Builder currentUrl(String val) { this.currentUrl = val; return this; }
        public Builder pageTitle(String val) { this.pageTitle = val; return this; }

        public ElementNotFoundException build() {
            String message = buildMessage(this);
            return new ElementNotFoundException(this, message);
        }

        private static String buildMessage(Builder b) {
            StringBuilder sb = new StringBuilder();

            sb.append("\n");
            sb.append("╔").append("═".repeat(BOX_WIDTH - 2)).append("╗\n");
            sb.append(centered("🔍 ЭЛЕМЕНТ НЕ НАЙДЕН"));
            sb.append("╠").append("═".repeat(BOX_WIDTH - 2)).append("╣\n");

            sb.append(labeled("Элемент", b.elementName));
            sb.append(labeled("Страница", b.pageName));

            if (b.componentName != null && !b.componentName.isEmpty()) {
                sb.append(labeled("Компонент", b.componentName));
            }

            if (b.timeout != null) {
                sb.append(labeled("Timeout", formatDuration(b.timeout)));
            }

            sb.append(labeled("Время", LocalDateTime.now().format(TIME_FMT)));

            if (b.currentUrl != null) {
                sb.append("╠").append("═".repeat(BOX_WIDTH - 2)).append("╣\n");
                sb.append(labeled("URL", b.currentUrl));
            }

            if (b.pageTitle != null) {
                sb.append(labeled("Title", b.pageTitle));
            }

            // Локатор
            sb.append("╠").append("═".repeat(BOX_WIDTH - 2)).append("╣\n");
            sb.append(left("Локатор:"));

            if (b.locator != null) {
                for (String line : wrapText(b.locator, BOX_WIDTH - 8)) {
                    sb.append(left("  " + line));
                }
            }

            sb.append("╚").append("═".repeat(BOX_WIDTH - 2)).append("╝\n");

            // Подсказки
            sb.append("\n📋 Возможные причины:\n");
            for (String tip : generateTips(b.locator)) {
                sb.append("  • ").append(tip).append("\n");
            }

            return sb.toString();
        }

        private static String centered(String text) {
            int padding = (BOX_WIDTH - 2 - text.length()) / 2;
            String padded = " ".repeat(Math.max(0, padding)) + text;
            return String.format("║%-" + (BOX_WIDTH - 2) + "s║\n", padded);
        }

        private static String left(String text) {
            String t = text.length() > BOX_WIDTH - 4
                    ? text.substring(0, BOX_WIDTH - 7) + "..."
                    : text;
            return String.format("║ %-" + (BOX_WIDTH - 4) + "s ║\n", t);
        }

        private static String labeled(String label, String value) {
            String val = value != null ? value : "N/A";
            String prefix = label + ": ";
            int maxLen = BOX_WIDTH - 4 - prefix.length();
            String displayVal = val.length() > maxLen
                    ? val.substring(0, maxLen - 3) + "..."
                    : val;
            return String.format("║ %s%-" + maxLen + "s ║\n", prefix, displayVal);
        }

        private static List<String> wrapText(String text, int maxWidth) {
            if (text == null) return List.of("N/A");

            List<String> lines = new ArrayList<>();
            int start = 0;
            while (start < text.length()) {
                int end = Math.min(start + maxWidth, text.length());
                lines.add(text.substring(start, end));
                start = end;
            }
            return lines;
        }

        private static String formatDuration(Duration d) {
            if (d == null) return "N/A";
            long ms = d.toMillis();
            return ms < 1000 ? ms + "ms" : String.format("%.1fs", ms / 1000.0);
        }

        private static List<String> generateTips(String locator) {
            List<String> tips = new ArrayList<>();

            if (locator != null) {
                if (locator.contains("@id=") && !locator.contains("contains")) {
                    tips.add("ID может быть динамическим - используйте contains(@id, '...')");
                }
                if (locator.contains("@class=") && !locator.contains("contains")) {
                    tips.add("Классы могут меняться - используйте contains(@class, '...')");
                }
            }

            tips.add("Элемент может загружаться асинхронно - увеличьте timeout");
            tips.add("Элемент может быть скрыт (display:none)");
            tips.add("Проверьте, не находится ли элемент в iframe");
            tips.add("Используйте DevTools для проверки локатора");

            return tips;
        }
    }

    // Getters
    public String getElementName() { return elementName; }
    public String getLocator() { return locator; }
    public String getPageName() { return pageName; }
    public String getComponentName() { return componentName; }
    public Duration getTimeout() { return timeout; }
    public String getCurrentUrl() { return currentUrl; }
    public String getPageTitle() { return pageTitle; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Краткое сообщение для логов.
     */
    public String getShortMessage() {
        return String.format("Element '%s' not found on page '%s' [%s]",
                elementName, pageName, locator);
    }
}