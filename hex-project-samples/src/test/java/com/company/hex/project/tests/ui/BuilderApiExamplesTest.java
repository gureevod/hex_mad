package com.company.hex.project.tests.ui;

import com.company.hex.project.pages.AdvancedFormPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Примеры использования Builder API для создания элементов с динамическими локаторами.
 * Демонстрирует различные сценарии использования Builder API из Story 3.4.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Epic("UI Framework")
@Feature("Builder API")
@DisplayName("Примеры использования Builder API")
public class BuilderApiExamplesTest {
    
    @Test
    @Story("Простые локаторы через Builder API")
    @DisplayName("Пример 1: Простые элементы с коротким синтаксисом")
    @Description("""
        Демонстрирует создание элементов с простыми локаторами через Builder API.
        Использует короткий синтаксис: input(xpath).withName(name).build()
        """)
    public void testSimpleBuilderSyntax() {
        AdvancedFormPage page = new AdvancedFormPage();
        
        // Заполнение простых полей
        page.fillSimpleEmail("test@example.com");
        page.clickSimpleSubmit();
    }
    
    @Test
    @Story("Составные локаторы")
    @DisplayName("Пример 2: Элементы с составными локаторами")
    @Description("""
        Демонстрирует создание элементов с составными локаторами.
        Локатор строится из нескольких частей: base() + append() + append()
        """)
    public void testCompositeLocators() {
        AdvancedFormPage page = new AdvancedFormPage();
        
        // Работа с составными локаторами
        page.fillCompositeField("composite@example.com");
        page.clickCompositeButton();
    }
    
    @Test
    @Story("Параметризованные локаторы")
    @DisplayName("Пример 3: Динамические элементы с одним параметром")
    @Description("""
        Демонстрирует использование параметризованных локаторов.
        Один элемент используется для доступа к разным полям через resolve().
        """)
    public void testDynamicSingleParameter() {
        AdvancedFormPage page = new AdvancedFormPage();
        
        // Заполнение разных полей через один динамический элемент
        page.fillDynamicField("firstName", "Иван");
        page.fillDynamicField("lastName", "Петров");
        page.fillDynamicField("email", "ivan@example.com");
        
        // Выполнение разных действий через одну динамическую кнопку
        page.performDynamicAction("save");
        page.performDynamicAction("validate");
    }
    
    @Test
    @Story("Многоуровневые параметризованные локаторы")
    @DisplayName("Пример 4: Элементы с несколькими параметрами")
    @Description("""
        Демонстрирует использование многоуровневых параметризованных локаторов.
        Элемент имеет несколько параметров для доступа к вложенным структурам.
        """)
    public void testMultiLevelParameters() {
        AdvancedFormPage page = new AdvancedFormPage();
        
        // Заполнение полей в разных секциях
        page.fillMultiLevelField("profile", "email", "user@example.com");
        page.fillMultiLevelField("profile", "phone", "+7 999 123-45-67");
        page.fillMultiLevelField("address", "city", "Москва");
        page.fillMultiLevelField("address", "street", "Тверская");
    }
    
    @Test
    @Story("Сложные многоуровневые локаторы")
    @DisplayName("Пример 5: Работа с таблицами через параметризованные локаторы")
    @Description("""
        Демонстрирует использование сложных параметризованных локаторов для таблиц.
        Один элемент используется для доступа к разным ячейкам таблицы.
        """)
    public void testTableCellButtons() {
        AdvancedFormPage page = new AdvancedFormPage();
        
        // Клики по разным кнопкам в таблице
        page.clickTableCellButton("user-123", "actions", "edit");
        page.clickTableCellButton("user-123", "actions", "delete");
        page.clickTableCellButton("user-456", "status", "activate");
        page.clickTableCellButton("user-789", "actions", "view");
    }
    
    @Test
    @Story("Очень сложные вложенные структуры")
    @DisplayName("Пример 6: Глубоко вложенные параметризованные локаторы")
    @Description("""
        Демонстрирует использование очень сложных параметризованных локаторов.
        Элемент имеет 4 уровня параметризации для доступа к глубоко вложенным элементам.
        """)
    public void testComplexNestedLocators() {
        AdvancedFormPage page = new AdvancedFormPage();
        
        // Заполнение полей в сложной вложенной структуре
        page.fillComplexNestedField(
            "dashboard",      // секция
            "analytics",      // виджет
            "settings",       // форма
            "interval",       // поле
            "daily"          // значение
        );
        
        page.fillComplexNestedField(
            "dashboard",
            "reports",
            "filters",
            "dateFrom",
            "2024-01-01"
        );
    }
    
    @Test
    @Story("Комплексные сценарии")
    @DisplayName("Пример 7: Заполнение формы пользователя")
    @Description("""
        Демонстрирует комплексный сценарий заполнения формы.
        Использует различные типы локаторов в одном сценарии.
        """)
    public void testCompleteUserFormScenario() {
        AdvancedFormPage page = new AdvancedFormPage();
        
        // Заполнение основной информации
        page.fillUserForm("Иван", "Петров", "ivan.petrov@example.com");
        
        // Заполнение секции профиля
        page.fillProfileSection("ivan@example.com", "+7 999 123-45-67");
        
        // Заполнение секции адреса
        page.fillAddressSection("Москва", "Тверская улица, 1");
        
        // Сохранение формы
        page.performDynamicAction("save");
    }
    
    @Test
    @Story("Комплексные сценарии")
    @DisplayName("Пример 8: Массовые операции с таблицей")
    @Description("""
        Демонстрирует выполнение массовых операций с элементами таблицы.
        Показывает эффективность параметризованных локаторов для повторяющихся действий.
        """)
    public void testBulkTableOperations() {
        AdvancedFormPage page = new AdvancedFormPage();
        
        // Выполнение массовых операций
        page.performTableActions();
        
        // Дополнительные операции
        page.clickTableCellButton("user-111", "actions", "approve");
        page.clickTableCellButton("user-222", "actions", "reject");
        page.clickTableCellButton("user-333", "status", "deactivate");
    }
}