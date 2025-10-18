package com.company.hex.project.tests.ui;

import com.company.hex.project.pages.DashboardPage;
import com.company.hex.project.pages.HomePage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

/**
 * Тесты для демонстрации работы компонентов с гибким scoping.
 * Показывает как один компонент может быть переиспользован на разных страницах
 * с разными root локаторами.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Epic("UI Framework")
@Feature("Компоненты с гибким scoping")
@DisplayName("Тесты компонентов с гибким scoping")
public class ComponentScopingTest {
    
    @Test
    @Story("Story 3.2: Компоненты с гибким scoping")
    @DisplayName("Использование HeaderComponent на Dashboard странице")
    @Description("""
            Тест демонстрирует:
            1. Создание Page Object с компонентом
            2. Использование компонента с указанным root локатором
            3. Контекстное логирование (page + component + element)
            4. Автоматические Allure steps для всех действий
            """)
    public void testHeaderComponentOnDashboard() {
        // Arrange
        DashboardPage dashboard = new DashboardPage();
        
        // Act & Assert
        dashboard.open();
        dashboard.verifyPageLoaded();
        
        dashboard.header.verifyLogoDisplayed()
                .openProfileMenu();
        
        // Проверяем что компонент правильно инициализирован
        dashboard.welcomeMessage.shouldBe(visible);
    }
    
    @Test
    @Story("Story 3.2: Компоненты с гибким scoping")
    @DisplayName("Переиспользование компонента с разными root локаторами")
    @Description("""
            Тест демонстрирует:
            1. Один класс компонента используется дважды на одной странице
            2. Каждый экземпляр имеет свой root локатор и имя
            3. Контекстное логирование различает компоненты
            """)
    public void testMultipleComponentInstances() {
        // Arrange
        DashboardPage dashboard = new DashboardPage();
        
        // Act & Assert
        dashboard.open();
        
        // Используем header компонент
        dashboard.header.verifyLogoDisplayed();
        
        // Используем sidebar компонент (тот же класс, но другой root)
        // TODO: Когда будет реальное приложение, добавить проверки sidebar
        
        dashboard.verifyWelcomeMessage("Welcome");
    }
    
    @Test
    @Story("Story 3.2: Компоненты с гибким scoping")
    @DisplayName("Навигация через компонент")
    @Description("""
            Тест демонстрирует:
            1. Бизнес-методы в компоненте
            2. Переход между страницами через компонент
            3. Группировка Allure steps
            """)
    public void testNavigationThroughComponent() {
        // Arrange
        DashboardPage dashboard = new DashboardPage();
        
        // Act
        dashboard.open();
        HomePage homePage = dashboard.navigateHome();
        
        // Assert
        // TODO: Когда HomePage будет полностью реализован, добавить проверки
    }
    
    @Test
    @Story("Story 3.2: Компоненты с гибким scoping")
    @DisplayName("Контекстное логирование в компонентах")
    @Description("""
            Тест демонстрирует:
            1. Все действия логируются с полным контекстом
            2. Формат: "Action on 'Element' в странице 'Page' компоненте 'Component'"
            3. Allure steps включают имя компонента
            """)
    public void testContextualLoggingInComponents() {
        // Arrange
        DashboardPage dashboard = new DashboardPage();
        
        // Act & Assert
        dashboard.open();
        
        dashboard.header.openSearch()
                .openProfileMenu();
        
        // Все действия будут залогированы с контекстом:
        // "Открытие поиска в компоненте 'Main Header' на странице 'Dashboard'"
        // "Открытие меню профиля в компоненте 'Main Header' на странице 'Dashboard'"
    }
    
    @Test
    @Story("Story 3.2: Компоненты с гибким scoping")
    @DisplayName("Fluent API с компонентами")
    @Description("""
            Тест демонстрирует:
            1. Цепочки вызовов с компонентами
            2. Возврат this для fluent API
            3. Читаемый код тестов
            """)
    public void testFluentApiWithComponents() {
        // Arrange & Act & Assert
        DashboardPage dashboard = new DashboardPage();
        dashboard.open();
        dashboard.verifyPageLoaded();
        
        dashboard.header.verifyLogoDisplayed()
                .openSearch()
                .openProfileMenu();
    }
}