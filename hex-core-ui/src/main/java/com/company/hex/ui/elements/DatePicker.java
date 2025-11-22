package com.company.hex.ui.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * Элемент выбора даты для Ant Design.
 */
public class DatePicker extends BaseElement {

    // Формат даты в атрибуте title ячейки AntD (обычно YYYY-MM-DD)
    private static final DateTimeFormatter TITLE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DatePicker(String name, SelenideElement element) {
        super(name, element);
    }

    /**
     * Установить дату через UI (кликая по календарю).
     *
     * @param date желаемая дата
     * @return this
     */
    @Step("Установить дату '{date}' в '{this.name}'")
    public DatePicker setDate(LocalDate date) {
        logger.info("Установка даты '{}' в '{}' {}", date, name, getContext());

        // 1. Открываем календарь, если он не открыт
        if (!isPanelVisible()) {
            element.click();
        }

        // 2. Находим активную (видимую) панель календаря
        // В AntD панель находится в body, а не внутри элемента
        SelenideElement panel = $$(".ant-picker-dropdown")
                .findBy(visible)
                .shouldBe(visible);

        // 3. Настраиваем Год и Месяц
        adjustYearAndMonth(panel, date);

        // 4. Кликаем по дню
        // Используем атрибут title="2025-10-26", это самый надежный способ в AntD
        String dateTitle = date.format(TITLE_FORMAT);

        panel.$("td[title='" + dateTitle + "'] .ant-picker-cell-inner")
                .scrollTo()
                .click();

        // 5. Ждем, пока панель закроется (значит выбор прошел успешно)
        panel.should(disappear);

        return this;
    }

    /**
     * Проверка, открыта ли панель календаря.
     */
    private boolean isPanelVisible() {
        return $$(".ant-picker-dropdown").findBy(visible).isDisplayed();
    }

    /**
     * Логика переключения года и месяца.
     */
    private void adjustYearAndMonth(SelenideElement panel, LocalDate targetDate) {
        SelenideElement header = panel.$(".ant-picker-header");
        SelenideElement yearBtn = header.$(".ant-picker-year-btn");
        SelenideElement monthBtn = header.$(".ant-picker-month-btn");

        // --- Настройка Года ---
        int currentYear = Integer.parseInt(yearBtn.getText());
        int targetYear = targetDate.getYear();

        while (currentYear != targetYear) {
            if (currentYear < targetYear) {
                header.$(".ant-picker-header-super-next-btn").click();
                currentYear++;
            } else {
                header.$(".ant-picker-header-super-prev-btn").click();
                currentYear--;
            }
            // Небольшая защита от бесконечного цикла, если UI тормозит
            yearBtn.shouldHave(text(String.valueOf(currentYear)));
        }

        // --- Настройка Месяца ---
        // AntD пишет месяц как "Nov", "Jan" и т.д. (зависит от локали, здесь предполагаем English)
        // Для надежности можно сравнивать через индексы или переключиться на вид выбора месяца,
        // но простой перебор стрелками часто работает стабильнее.

        String currentMonthStr = monthBtn.getText();
        int currentMonth = parseMonth(currentMonthStr);
        int targetMonth = targetDate.getMonthValue();

        while (currentMonth != targetMonth) {
            if (currentMonth < targetMonth) {
                header.$(".ant-picker-header-next-btn").click();
                currentMonth++;
            } else {
                header.$(".ant-picker-header-prev-btn").click();
                currentMonth--;
            }
            // Ждем обновления текста месяца, чтобы не кликать слишком быстро
            monthBtn.shouldNotHave(text(currentMonthStr));
            currentMonthStr = monthBtn.getText();
        }
    }

    /**
     * Вспомогательный метод для парсинга месяца из текста (Дек -> 12).
     */
    private int parseMonth(String monthStr) {
        // Пробуем распарсить короткое название (Jan, Feb...)
        // Если у вас русская локаль, нужно поменять Locale.US на Locale.forLanguageTag("ru")
        for (java.time.Month month : java.time.Month.values()) {
            String shortName = month.getDisplayName(TextStyle.SHORT, new Locale("ru"));
            if (monthStr.startsWith(shortName)) {
                return month.getValue();
            }
        }
        // Если не вышло, пробуем полное название или возвращаем ошибку
        throw new IllegalStateException("Не удалось распознать месяц: " + monthStr);
    }

    /**
     * Быстрый ввод даты через input (если поле не read-only).
     */
    @Step("Ввести дату '{date}' вручную в '{this.name}'")
    public DatePicker typeDate(LocalDate date, String pattern) {
        String dateStr = date.format(DateTimeFormatter.ofPattern(pattern));
        logger.info("Ввод даты '{}' в '{}'", dateStr, name);

        // В AntD input часто лежит внутри span, ищем тег input
        SelenideElement input = element.getTagName().equals("input") ? element : element.$("input");

        input.click();
        // Используем Keys.chord(Keys.CONTROL, "a") + Keys.BACK_SPACE если clear() не работает
        input.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
        input.sendKeys(org.openqa.selenium.Keys.BACK_SPACE);
        input.sendKeys(dateStr);
        input.pressEnter();

        return this;
    }
}