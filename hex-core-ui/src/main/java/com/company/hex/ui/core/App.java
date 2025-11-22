package com.company.hex.ui.core;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Глобальный фасад для управления состоянием приложения в браузере.
 * Позволяет управлять куками, LocalStorage и SessionStorage из любой точки теста.
 */
public final class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private App() {}

    /**
     * Управление куками.
     */
    public static class CookieManager {

        /**
         * Добавить куку. Автоматически открывает браузер, если он закрыт.
         */
        public static void add(String name, String value) {
            ensureBrowserContext();

            try {
                WebDriverRunner.getWebDriver().manage().addCookie(new Cookie(name, value));
                logger.info("Кука установлена: {}={}", name, value);
            } catch (Exception e) {
                logger.error("Не удалось установить куку {}. Проверьте домен.", name);
                throw e;
            }
        }

        /**
         * Добавить сложную куку (с путем, доменом и т.д.)
         */
        public static void add(Cookie cookie) {
            ensureBrowserContext();
            WebDriverRunner.getWebDriver().manage().addCookie(cookie);
            logger.info("Кука установлена: {}", cookie);
        }

        public static void delete(String name) {
            if (WebDriverRunner.hasWebDriverStarted()) {
                WebDriverRunner.getWebDriver().manage().deleteCookieNamed(name);
                logger.info("Кука удалена: {}", name);
            }
        }

        public static void clearAll() {
            if (WebDriverRunner.hasWebDriverStarted()) {
                Selenide.clearBrowserCookies();
                logger.info("Все куки очищены");
            }
        }
    }

    /**
     * Управление LocalStorage.
     */
    public static class LocalStorage {

        public static void setItem(String key, String value) {
            ensureBrowserContext();
            Selenide.executeJavaScript("localStorage.setItem(arguments[0], arguments[1]);", key, value);
            logger.info("LocalStorage set: {}={}", key, value);
        }

        public static String getItem(String key) {
            ensureBrowserContext();
            return Selenide.executeJavaScript("return localStorage.getItem(arguments[0]);", key);
        }

        public static void clear() {
            if (WebDriverRunner.hasWebDriverStarted()) {
                Selenide.executeJavaScript("localStorage.clear();");
                logger.info("LocalStorage очищен");
            }
        }
    }

    /**
     * Вспомогательный метод: гарантирует, что мы находимся в контексте приложения.
     * Selenium не дает работать с куками/storage на странице "data:,".
     */
    private static void ensureBrowserContext() {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            logger.info("Браузер не запущен. Открываем baseUrl: {}", Configuration.baseUrl);
            Selenide.open(Configuration.baseUrl);
            return;
        }

        String currentUrl = WebDriverRunner.url();
        if (currentUrl.equals("data:,") || currentUrl.equals("about:blank")) {
            logger.info("Браузер в пустом состоянии. Открываем baseUrl: {}", Configuration.baseUrl);
            Selenide.open(Configuration.baseUrl);
        }
    }

    /**
     * Перезагрузить страницу (часто нужно для применения кук/storage).
     */
    public static void refresh() {
        Selenide.refresh();
    }
}