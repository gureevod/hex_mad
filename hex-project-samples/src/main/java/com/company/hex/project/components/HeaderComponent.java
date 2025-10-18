package com.company.hex.project.components;

import com.company.hex.ui.core.BaseComponent;
import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Elements;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.TextElement;
import com.company.hex.project.pages.HomePage;
import io.qameta.allure.Step;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Компонент навигационного заголовка.
 * Демонстрирует переиспользуемый компонент с гибким root локатором.
 * 
 * <p>Пример использования на разных страницах:</p>
 * <pre>
 * {@code
 * // На главной странице
 * @Component(name = "Main Header", root = "//header[@id='main-header']")
 * HeaderComponent mainHeader;
 * 
 * // На мобильной версии
 * @Component(name = "Mobile Header", root = "//header[@id='mobile-header']")
 * HeaderComponent mobileHeader;
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class HeaderComponent extends BaseComponent {
    
    @Element(name = "Home Button", xpath = ".//a[@title='home']")
    Button homeButton;
    
    @Element(name = "Logo", xpath = ".//img[@class='logo']")
    TextElement logo;
    
    @Element(name = "Profile Menu", xpath = ".//div[@class='profile-menu']")
    Button profileMenu;
    
    @Element(name = "Search Button", xpath = ".//button[@id='search']")
    Button searchButton;
    
    @Elements(name = "Navigation Links", xpath = ".//nav//a")
    Button[] navLinks;
    
    /**
     * Перейти на главную страницу через кнопку Home.
     * 
     * @return новый экземпляр HomePage
     */
    @Step("Перейти на главную из '{this.componentName}'")
    public HomePage goHome() {
        logger.info("Переход на главную из компонента '{}' на странице '{}'",
                getComponentName(), getPageName());
        homeButton.click();
        return new HomePage();
    }
    
    /**
     * Открыть меню профиля.
     * 
     * @return this для fluent API
     */
    @Step("Открыть меню профиля в '{this.componentName}'")
    public HeaderComponent openProfileMenu() {
        logger.info("Открытие меню профиля в компоненте '{}' на странице '{}'",
                getComponentName(), getPageName());
        profileMenu.click();
        return this;
    }
    
    /**
     * Открыть поиск.
     * 
     * @return this для fluent API
     */
    @Step("Открыть поиск в '{this.componentName}'")
    public HeaderComponent openSearch() {
        logger.info("Открытие поиска в компоненте '{}' на странице '{}'",
                getComponentName(), getPageName());
        searchButton.click();
        return this;
    }
    
    /**
     * Проверить, что логотип отображается.
     * 
     * @return this для fluent API
     */
    @Step("Проверить отображение логотипа в '{this.componentName}'")
    public HeaderComponent verifyLogoDisplayed() {
        logger.info("Проверка отображения логотипа в компоненте '{}' на странице '{}'",
                getComponentName(), getPageName());
        logo.shouldBe(com.codeborne.selenide.Condition.visible);
        return this;
    }
    
    /**
     * Получить текст всех навигационных ссылок.
     * 
     * @return список текстов ссылок
     */
    @Step("Получить все навигационные ссылки из '{this.componentName}'")
    public List<String> getNavigationLinks() {
        logger.debug("Получение навигационных ссылок из компонента '{}' на странице '{}'",
                getComponentName(), getPageName());
        
        // TODO: Когда ElementList будет реализован, использовать его
        // return navLinks.map(Button::getText).collect(Collectors.toList());
        
        logger.warn("ElementList пока не реализован, возвращаем пустой список");
        return List.of();
    }
    
    /**
     * Кликнуть по навигационной ссылке с указанным текстом.
     * 
     * @param linkText текст ссылки
     * @return this для fluent API
     */
    @Step("Кликнуть по навигационной ссылке '{linkText}' в '{this.componentName}'")
    public HeaderComponent clickNavigationLink(String linkText) {
        logger.info("Клик по навигационной ссылке '{}' в компоненте '{}' на странице '{}'",
                linkText, getComponentName(), getPageName());
        
        // TODO: Когда ElementList будет реализован, использовать его
        // navLinks.findFirst(link -> link.getText().equals(linkText))
        //         .orElseThrow(() -> new AssertionError("Ссылка не найдена: " + linkText))
        //         .click();
        
        logger.warn("ElementList пока не реализован, метод не выполнен");
        return this;
    }
}