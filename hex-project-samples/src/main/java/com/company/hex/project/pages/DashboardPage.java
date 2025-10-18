package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Component;
import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.TextElement;
import com.company.hex.project.components.HeaderComponent;
import io.qameta.allure.Step;

/**
 * Page Object для страницы Dashboard.
 * Демонстрирует использование переиспользуемых компонентов с гибким scoping.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * DashboardPage dashboard = new DashboardPage().open();
 * 
 * // Использование компонента
 * dashboard.header.verifyLogoDisplayed();
 * dashboard.header.openProfileMenu();
 * 
 * // Прямое использование элементов
 * dashboard.welcomeMessage.shouldHave(text("Welcome, User!"));
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Page(url = "/dashboard", title = "Dashboard")
public class DashboardPage extends BasePage {
    
    // Компонент с указанием root локатора и имени
    @Component(name = "Main Header", root = "//header[@id='main-header']")
    public HeaderComponent header;
    
    // Можно использовать тот же компонент с другим root локатором
    @Component(name = "Sidebar Navigation", root = "//aside[@id='sidebar']")
    public HeaderComponent sidebar;
    
    @Element(name = "Welcome Message", xpath = "//h1[@class='welcome']")
    public TextElement welcomeMessage;
    
    @Element(name = "User Avatar", xpath = "//img[@class='user-avatar']")
    public TextElement userAvatar;
    
    @Element(name = "Notifications Button", xpath = "//button[@id='notifications']")
    public Button notificationsButton;
    
    @Element(name = "Settings Button", xpath = "//button[@id='settings']")
    public Button settingsButton;
    
    /**
     * Перейти на главную страницу через header.
     * 
     * @return новый экземпляр HomePage
     */
    @Step("Перейти на главную со страницы Dashboard")
    public HomePage navigateHome() {
        logger.info("Переход на главную со страницы Dashboard через header");
        return header.goHome();
    }
    
    /**
     * Открыть профиль пользователя.
     * 
     * @return this для fluent API
     */
    @Step("Открыть профиль пользователя")
    public DashboardPage openUserProfile() {
        logger.info("Открытие профиля пользователя на странице Dashboard");
        header.openProfileMenu();
        return this;
    }
    
    /**
     * Открыть уведомления.
     * 
     * @return this для fluent API
     */
    @Step("Открыть уведомления")
    public DashboardPage openNotifications() {
        logger.info("Открытие уведомлений на странице Dashboard");
        notificationsButton.click();
        return this;
    }
    
    /**
     * Открыть настройки.
     * 
     * @return this для fluent API
     */
    @Step("Открыть настройки")
    public DashboardPage openSettings() {
        logger.info("Открытие настроек на странице Dashboard");
        settingsButton.click();
        return this;
    }
    
    /**
     * Проверить приветственное сообщение.
     * 
     * @param expectedText ожидаемый текст
     * @return this для fluent API
     */
    @Step("Проверить приветственное сообщение: {expectedText}")
    public DashboardPage verifyWelcomeMessage(String expectedText) {
        logger.info("Проверка приветственного сообщения на странице Dashboard: {}", expectedText);
        welcomeMessage.shouldHave(com.codeborne.selenide.Condition.text(expectedText));
        return this;
    }
    
    /**
     * Проверить, что все основные элементы отображаются.
     * 
     * @return this для fluent API
     */
    @Step("Проверить отображение основных элементов Dashboard")
    public DashboardPage verifyPageLoaded() {
        logger.info("Проверка загрузки страницы Dashboard");
        
        header.verifyLogoDisplayed();
        welcomeMessage.shouldBe(com.codeborne.selenide.Condition.visible);
        userAvatar.shouldBe(com.codeborne.selenide.Condition.visible);
        notificationsButton.shouldBe(com.codeborne.selenide.Condition.visible);
        settingsButton.shouldBe(com.codeborne.selenide.Condition.visible);
        
        logger.info("Все основные элементы Dashboard отображаются");
        return this;
    }
}