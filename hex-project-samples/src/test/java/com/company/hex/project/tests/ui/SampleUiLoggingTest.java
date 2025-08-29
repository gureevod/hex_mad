package com.company.hex.project.tests.ui;

import com.company.hex.core.logging.CorrelationIdManager;
import com.company.hex.testing.allure.AllureLifecycleListener;
import com.company.hex.ui.core.BaseComponent;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.logging.UiActionLogger;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Пример UI теста с демонстрацией логирования и Allure интеграции.
 * Показывает использование всех возможностей фреймворка для UI тестирования.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(AllureLifecycleListener.class)
@Epic("Sample Tests")
@Feature("UI Logging Demo")
@DisplayName("Демонстрация UI логирования и отчетности")
class SampleUiLoggingTest {
    
    private static final String TEST_URL = "https://the-internet.herokuapp.com";
    
    @BeforeEach
    void setUp() {
        // Настраиваем Selenide для демонстрации
        Configuration.browser = "chrome";
        Configuration.headless = false;
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
        
        // Устанавливаем контекст теста
        CorrelationIdManager.initializeTestContext(
            this.getClass().getSimpleName(), 
            "UI Logging Demo Test"
        );
        
        // Логируем информацию о браузере
        UiActionLogger.logBrowserInfo();
    }
    
    @Test
    @Story("Basic Element Interactions")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Демонстрация базовых взаимодействий с элементами")
    @Description("Тест демонстрирует автоматическое логирование базовых UI действий")
    void demonstrateBasicElementInteractions() {
        // Навигация на страницу
        String targetUrl = TEST_URL + "/login";
        UiActionLogger.logNavigation(targetUrl);
        open(targetUrl);
        
        // Создаем скриншот после загрузки страницы
        UiActionLogger.takeScreenshot("Page_Loaded");
        
        // Создаем элементы с логированием
        BaseElement usernameField = new BaseElement("#username", "Username Field");
        BaseElement passwordField = new BaseElement("#password", "Password Field");
        BaseElement loginButton = new BaseElement("button[type='submit']", "Login Button");
        
        // Проверяем видимость элементов
        usernameField.shouldBeVisible();
        passwordField.shouldBeVisible();
        loginButton.shouldBeVisible();
        
        // Вводим данные с автоматическим логированием
        usernameField.setValue("tomsmith");
        passwordField.setValue("SuperSecretPassword!");
        
        // Создаем скриншот перед кликом
        UiActionLogger.takeScreenshot("Before_Login_Click");
        
        // Кликаем по кнопке входа
        loginButton.click();
        
        // Проверяем успешный вход
        BaseElement successMessage = new BaseElement(".flash.success", "Success Message");
        successMessage.shouldBeVisible();
        successMessage.shouldHaveText("You logged into a secure area!");
        
        // Создаем финальный скриншот
        UiActionLogger.takeScreenshot("Login_Success");
    }
    
    @Test
    @Story("Component Interactions")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Демонстрация работы с компонентами")
    @Description("Тест показывает логирование действий с составными UI компонентами")
    void demonstrateComponentInteractions() {
        // Переходим на страницу с формой
        String targetUrl = TEST_URL + "/add_remove_elements";
        UiActionLogger.logNavigation(targetUrl);
        open(targetUrl);
        
        // Создаем компонент для демонстрации
        AddRemoveComponent addRemoveComponent = new AddRemoveComponent();
        
        // Проверяем загрузку компонента
        addRemoveComponent.shouldBeLoaded();
        
        // Добавляем несколько элементов
        addRemoveComponent.addElement();
        addRemoveComponent.addElement();
        addRemoveComponent.addElement();
        
        // Проверяем количество добавленных элементов
        int elementsCount = addRemoveComponent.getElementsCount();
        UiActionLogger.logAssertion("Elements Count", "equals", String.valueOf(elementsCount));
        
        // Удаляем один элемент
        addRemoveComponent.removeElement();
        
        // Создаем отчет о состоянии страницы
        UiActionLogger.createPageStateReport();
    }
    
    @Test
    @Story("Error Handling")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Демонстрация обработки ошибок UI")
    @Description("Тест показывает как фреймворк логирует и обрабатывает ошибки UI")
    void demonstrateErrorHandling() {
        // Переходим на страницу
        String targetUrl = TEST_URL + "/dynamic_loading/1";
        UiActionLogger.logNavigation(targetUrl);
        open(targetUrl);
        
        // Пытаемся найти элемент, который появится позже
        BaseElement startButton = new BaseElement("#start button", "Start Button");
        BaseElement hiddenElement = new BaseElement("#finish h4", "Hidden Element");
        
        // Кликаем по кнопке запуска
        startButton.click();
        
        // Ожидаем появления скрытого элемента с логированием
        UiActionLogger.logWait("Hidden Element", "becomes visible", 10000);
        hiddenElement.waitForVisible(10000);
        
        // Проверяем текст появившегося элемента
        hiddenElement.shouldHaveText("Hello World!");
        
        // Создаем финальный скриншот
        UiActionLogger.takeScreenshot("Dynamic_Loading_Complete");
    }
    
    @Test
    @Story("JavaScript Execution")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Демонстрация выполнения JavaScript")
    @Description("Тест показывает логирование выполнения JavaScript кода")
    void demonstrateJavaScriptExecution() {
        // Переходим на любую страницу
        open(TEST_URL);
        
        // Выполняем JavaScript с логированием
        String script = "return document.title;";
        String pageTitle = Selenide.executeJavaScript(script);
        UiActionLogger.logJavaScriptExecution(script, pageTitle);
        
        // Выполняем более сложный JavaScript
        String complexScript = "return {url: window.location.href, title: document.title, userAgent: navigator.userAgent};";
        Object result = Selenide.executeJavaScript(complexScript);
        UiActionLogger.logJavaScriptExecution(complexScript, result);
        
        // Прикрепляем результат к отчету
        Allure.addAttachment("Page Title", pageTitle);
        Allure.addAttachment("JavaScript Result", String.valueOf(result));
    }
    
    /**
     * Компонент для демонстрации работы с составными элементами.
     */
    private static class AddRemoveComponent extends BaseComponent {
        
        public AddRemoveComponent() {
            super(".example", "Add/Remove Elements Component");
        }
        
        @Step("🔘 Добавление элемента")
        public void addElement() {
            element("button[onclick='addElement()']", "Add Element Button").click();
            UiActionLogger.takeScreenshot("Element_Added");
        }
        
        @Step("🗑️ Удаление элемента")
        public void removeElement() {
            element("#elements button", "Delete Button").click();
            UiActionLogger.takeScreenshot("Element_Removed");
        }
        
        @Step("🔢 Получение количества элементов")
        public int getElementsCount() {
            return getElementsCount("#elements button");
        }
    }
}