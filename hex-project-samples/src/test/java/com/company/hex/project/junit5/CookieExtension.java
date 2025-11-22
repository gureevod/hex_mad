package com.company.hex.project.junit5;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.company.hex.project.annotations.AddCookie;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CookieExtension implements BeforeEachCallback {

    private static final Logger logger = LoggerFactory.getLogger(CookieExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        // Ищем аннотации @AddCookie на методе и на классе
        List<AddCookie> cookies = AnnotationSupport.findRepeatableAnnotations(
                context.getElement(),
                AddCookie.class
        );

        if (cookies.isEmpty()) {
            return;
        }

        // 1. Гарантируем, что браузер открыт на нужном домене
        ensureBrowserIsOpen(cookies.get(0));

        // 2. Добавляем все куки
        boolean cookieAdded = false;
        for (AddCookie cookieAnn : cookies) {
            addCookie(cookieAnn);
            cookieAdded = true;
        }

        // 3. Обновляем страницу, чтобы приложение увидело куки
        if (cookieAdded) {
            logger.debug("Обновление страницы для применения кук...");
            Selenide.refresh();
        }
    }

    private void ensureBrowserIsOpen(AddCookie annotation) {
        String targetUrl = annotation.url().isEmpty()
                ? Configuration.baseUrl
                : annotation.url();

        if (targetUrl == null || targetUrl.isEmpty()) {
            throw new IllegalStateException("Невозможно установить куку: не задан ни url в аннотации, ни Configuration.baseUrl");
        }

        // Если драйвер не запущен или мы на пустой странице (data:,)
        if (!WebDriverRunner.hasWebDriverStarted() || WebDriverRunner.url().equals("data:,")) {
            logger.info("Открытие URL для установки кук: {}", targetUrl);
            Selenide.open(targetUrl);
        } else {
            // Если мы уже где-то находимся, проверим, совпадает ли домен.
            // Для простоты, если домены разные, Selenium сам кинет ошибку при установке куки,
            // но лучше открыть явно, если URL задан жестко.
            if (!annotation.url().isEmpty() && !WebDriverRunner.url().contains(annotation.url())) {
                Selenide.open(targetUrl);
            }
        }
    }

    private void addCookie(AddCookie annotation) {
        try {
            Cookie cookie = new Cookie.Builder(annotation.name(), annotation.value())
                    .path(annotation.path())
                    .build();

            WebDriverRunner.getWebDriver().manage().addCookie(cookie);
            logger.info("Добавлена кука: {}={}", annotation.name(), annotation.value());
        } catch (Exception e) {
            logger.error("Ошибка при добавлении куки {}. Возможно, браузер открыт не на том домене?", annotation.name(), e);
            throw e;
        }
    }
}
