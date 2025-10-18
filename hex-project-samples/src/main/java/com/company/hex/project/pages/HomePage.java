package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Component;
import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.TextElement;
import com.company.hex.project.components.HeaderComponent;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;

/**
 * Page Object для домашней страницы.
 * Демонстрирует простой Page Object с компонентом и базовыми элементами.
 *
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * HomePage home = new HomePage().open();
 * home.verifyWelcomeMessage("Welcome, User!")
 *     .navigateToDashboard();
 * }
 * </pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Page(url = "/home", title = "Home Page")
public class HomePage extends BasePage {
    
    @Component(name = "Header", root = "//nav[@id='header']")
    public HeaderComponent header;
    
    @Element(name = "Welcome Message", xpath = "//h1[@class='welcome']")
    TextElement welcomeMessage;
    
    @Element(name = "Dashboard Link", xpath = "//a[@href='/dashboard']")
    Button dashboardLink;
    
    @Element(name = "Profile Link", xpath = "//a[@href='/profile']")
    Button profileLink;
    
    @Element(name = "Logout Button", xpath = "//button[@id='logout']")
    Button logoutButton;
    
    /**
     * Перейти на страницу Dashboard.
     *
     * @return DashboardPage
     */
    @Step("Перейти на Dashboard")
    public DashboardPage navigateToDashboard() {
        logger.info("Переход на Dashboard");
        dashboardLink.click();
        return new DashboardPage();
    }
    
    /**
     * Перейти на страницу профиля.
     *
     * @return UserProfilePage
     */
    @Step("Перейти в профиль")
    public UserProfilePage navigateToProfile() {
        logger.info("Переход в профиль");
        profileLink.click();
        return new UserProfilePage();
    }
    
    /**
     * Выйти из системы.
     *
     * @return LoginPage
     */
    @Step("Выйти из системы")
    public LoginPage logout() {
        logger.info("Выход из системы");
        logoutButton.click();
        return new LoginPage();
    }
    
    /**
     * Проверить приветственное сообщение.
     *
     * @param expectedMessage ожидаемое сообщение
     * @return this для fluent API
     */
    @Step("Проверить приветственное сообщение: {expectedMessage}")
    public HomePage verifyWelcomeMessage(String expectedMessage) {
        logger.info("Проверка приветственного сообщения: {}", expectedMessage);
        welcomeMessage.shouldBe(visible);
        welcomeMessage.shouldHaveText(expectedMessage);
        logger.info("Приветственное сообщение корректно");
        return this;
    }
    
    /**
     * Проверить, что все элементы страницы отображаются.
     *
     * @return this для fluent API
     */
    @Step("Проверить отображение элементов домашней страницы")
    public HomePage verifyPageLoaded() {
        logger.info("Проверка загрузки домашней страницы");
        
        header.verifyLogoDisplayed();
        welcomeMessage.shouldBe(visible);
        dashboardLink.shouldBe(visible);
        profileLink.shouldBe(visible);
        logoutButton.shouldBe(visible);
        
        logger.info("Все элементы домашней страницы отображаются корректно");
        return this;
    }
}