package com.company.hex.ui.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.actions;

/**
 * Элемент выбора даты (DatePicker) для Ant Design.
 * Реализован на основе логики работы с десятилетиями и индексами месяцев.
 */
public class DatePicker extends BaseElement {

    // Локатор выпадающего списка, который виден в данный момент
    private static final String DROPDOWN_XPATH = "//div[contains(@class, 'ant-picker-dropdown') and not(contains(@class, 'hidden'))]";
    
    // Формат даты для ручного ввода (можно вынести в конфиг, если меняется)
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public DatePicker(String name, SelenideElement element) {
        super(name, element);
    }

    /**
     * Выбирает дату через UI календаря.
     * Логика: Открыть -> Выбрать год (через десятилетия) -> Выбрать месяц (по индексу) -> Выбрать день.
     *
     * @param date дата для выбора
     * @return this
     */
    @Step("Выбрать дату '{date}' в календаре '{this.name}'")
    public DatePicker setDate(LocalDate date) {
        logger.info("Выбор даты {} в календаре {}", date, name);

        openCalendar();

        // 1. Установка года (специфичная логика AntD с выбором десятилетия)
        selectYear(date.getYear());

        // 2. Установка месяца (по индексу 1-12)
        selectMonth(date.getMonthValue());

        // 3. Установка дня
        selectDay(date.getDayOfMonth());

        // Ждем закрытия дропдауна, чтобы убедиться, что дата выбрана
        $x(DROPDOWN_XPATH).should(disappear);
        
        return this;
    }

    /**
     * Вводит дату вручную через поле ввода (быстрый способ).
     * Использует Ctrl+A -> Backspace для очистки.
     *
     * @param date дата для ввода
     * @return this
     */
    @Step("Ввести дату '{date}' вручную в '{this.name}'")
    public DatePicker typeDate(LocalDate date) {
        String dateString = date.format(INPUT_FORMAT);
        logger.info("Ручной ввод даты {} в поле {}", dateString, name);

        // Находим input внутри компонента (обычно он вложен)
        SelenideElement input = element.$("input");
        
        input.click();
        actions().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.BACK_SPACE).perform();
        input.sendKeys(dateString);
        input.pressEnter();

        return this;
    }

    /**
     * Очищает значение в календаре, нажимая на иконку крестика (clear icon).
     * Иконка появляется только при наведении курсора.
     *
     * @return this
     */
    @Step("Очистить календарь '{this.name}'")
    public DatePicker clear() {
        logger.info("Очистка календаря {}", name);
        
        // Наводим курсор, чтобы появилась иконка очистки
        element.hover();
        
        SelenideElement clearBtn = element.$(".ant-picker-clear");
        if (clearBtn.exists() && clearBtn.isDisplayed()) {
            clearBtn.click();
        } else {
            logger.debug("Иконка очистки не найдена или календарь уже пуст");
        }
        
        return this;
    }

    /**
     * Проверяет, что указанная дата недоступна для выбора (disabled).
     *
     * @param date дата для проверки
     * @return true, если дата заблокирована
     */
    @Step("Проверить, что дата '{date}' недоступна в '{this.name}'")
    public boolean isDateDisabled(LocalDate date) {
        openCalendar();
        
        // Необходимо переключиться на нужный месяц/год, чтобы увидеть ячейку
        // Упрощенно: предполагаем, что мы уже близко, или используем логику setDate без клика по дню
        // Для полной надежности здесь нужно дублировать навигацию selectYear -> selectMonth
        selectYear(date.getYear());
        selectMonth(date.getMonthValue());

        String dayText = String.valueOf(date.getDayOfMonth());
        SelenideElement dayCell = $x(String.format("%s//td[contains(@class, 'ant-picker-cell') and .//text()='%s']", 
                DROPDOWN_XPATH, dayText));

        boolean isDisabled = dayCell.getAttribute("class").contains("disabled");
        
        // Закрываем календарь кликом вовне или Escape (опционально)
        element.pressEscape();
        
        return isDisabled;
    }

    private void openCalendar() {
        // Если дропдаун уже виден, не кликаем
        if (!$x(DROPDOWN_XPATH).isDisplayed()) {
            element.click();
            $x(DROPDOWN_XPATH).shouldBe(visible);
        }
    }

    private void selectYear(int year) {
        // Кнопка переключения года (в шапке)
        SelenideElement yearBtn = $x(DROPDOWN_XPATH + "//*[@class='ant-picker-year-btn']");
        
        // Логика из CalendarHelper: кликаем дважды, чтобы выйти в выбор десятилетия
        yearBtn.click();
        yearBtn.click();

        // Вычисляем диапазон десятилетия (например, для 2025 это "2020-2029")
        String decadeRange = resolveYearGroup(year);
        
        // Выбираем диапазон
        String rangeXpath = String.format("%s//*[@class='ant-picker-cell-inner' and text()='%s']", DROPDOWN_XPATH, decadeRange);
        $x(rangeXpath).click();

        // Выбираем конкретный год
        String yearXpath = String.format("%s//*[@class='ant-picker-cell-inner' and text()='%d']", DROPDOWN_XPATH, year);
        $x(yearXpath).click();
    }

    private void selectMonth(int monthIndex) {
        // Выбор месяца по индексу (1-12).
        // XPath ищет n-й элемент среди видимых ячеек месяца.
        String monthXpath = String.format("(%s//*[contains(@class, 'ant-picker-cell-in-view')])[%d]", DROPDOWN_XPATH, monthIndex);
        $x(monthXpath).click();
    }

    private void selectDay(int day) {
        // Выбор дня по тексту внутри видимых ячеек (in-view исключает дни соседних месяцев)
        String dayXpath = String.format("%s//td[contains(@class, 'ant-picker-cell-in-view')]//*[text()='%d']", DROPDOWN_XPATH, day);
        $x(dayXpath).click();
    }

    /**
     * Вычисляет диапазон десятилетия для года.
     * Пример: 2025 -> "2020-2029"
     */
    private String resolveYearGroup(int year) {
        int startYear = (year / 10) * 10;
        int endYear = startYear + 9;
        return startYear + "-" + endYear;
    }
}