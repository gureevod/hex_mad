package com.company.hex.project.junit5;

import com.codeborne.selenide.Selenide;

import com.company.hex.project.annotations.AddCookie;
import com.company.hex.ui.core.App;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * JUnit 5 Extension для добавления кук перед тестом.
 * Читает аннотации @AddCookie и делегирует установку классу App.
 */
public class CookieExtension implements BeforeEachCallback {

    private static final Logger logger = LoggerFactory.getLogger(CookieExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        // 1. Ищем аннотации @AddCookie (и одиночные, и повторяемые)
        List<AddCookie> cookies = AnnotationSupport.findRepeatableAnnotations(
                context.getElement(),
                AddCookie.class
        );

        if (cookies.isEmpty()) {
            return;
        }

        boolean cookieAdded = false;

        for (AddCookie annotation : cookies) {
            // 2. Если в аннотации указан специфичный URL, открываем его ПЕРЕД установкой.
            // Если URL пустой, то App.CookieManager.add() сам откроет baseUrl внутри себя.
            if (!annotation.url().isEmpty()) {
                logger.debug("Открытие специфичного URL из аннотации: {}", annotation.url());
                Selenide.open(annotation.url());
            }

            // 3. Создаем объект Cookie, чтобы передать путь (path) и другие параметры
            Cookie seleniumCookie = new Cookie.Builder(annotation.name(), annotation.value())
                    .path(annotation.path())
                    .build();

            // 4. Делегируем добавление нашему фасаду App
            // App сам проверит, запущен ли браузер, и откроет baseUrl, если мы еще не открывали ничего выше
            App.CookieManager.add(seleniumCookie);

            cookieAdded = true;
        }

        // 5. Обновляем страницу, чтобы приложение подхватило изменения
        if (cookieAdded) {
            logger.debug("Обновление страницы для применения кук...");
            App.refresh();
        }
    }
}