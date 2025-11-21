package com.company.hex.project.junit5;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.company.hex.ui.core.WebDriverFactory;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public class UiExtension implements BeforeAllCallback, AfterEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        // 1. Инициализируем Selenide через нашу фабрику
        WebDriverFactory.registerStrategy("selenoid-video", config -> {
            ChromeOptions options = new ChromeOptions();
            options.setCapability("enableVideo", true);
            options.setCapability("enableVNC", true);

            try {
                return new RemoteWebDriver(new URL("http://selenoid:4444/wd/hub"), options);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        WebDriverFactory.initSelenide();

        // 2. Подключаем Allure (чтобы не дублировать это в каждом тесте)
        // Проверяем, не добавлен ли листенер уже (чтобы избежать дублей при параллельном запуске)
        if (!SelenideLogger.hasListener("AllureSelenide")) {
            SelenideLogger.addListener("AllureSelenide",
                    new AllureSelenide()
                            .screenshots(true)
                            .savePageSource(true));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // 3. Закрываем браузер после каждого теста для изоляции
        // (или можно оставить открытым, если стратегия другая)
        Selenide.closeWebDriver();
    }
}
