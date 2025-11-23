package com.company.hex.ui.elements;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Элемент Таблицы, адаптированный под AntDesign.
 * Включает в себя логику работы со строками, ячейками, инпутами и валидацией данных.
 */
public class Table extends BaseElement {

    // Локаторы AntDesign
    private static final String HEADER_LOCATOR = ".//thead//th";
    // Ищем строки и по классу, и по data-test-id, как в старом коде
    private static final String ROW_LOCATOR = ".//tbody//tr[contains(@class, 'ant-table-row') or contains(@data-test-id, '_row')]";
    private static final String CELL_LOCATOR = ".//td";
    private static final String PAGINATION_LOCATOR = "//li[contains(@class, 'ant-pagination-item')]";

    public Table(String name, SelenideElement element) {
        super(name, element);
    }

    // =================================================================================================================
    // Методы получения данных (Getters)
    // =================================================================================================================

    /**
     * Получить заголовки таблицы.
     */
    @Step("Получить заголовки таблицы '{this.name}'")
    public List<String> getHeaders() {
        return element.$$(By.xpath(HEADER_LOCATOR)).texts();
    }

    /**
     * Получить строку по индексу (начиная с 0).
     */
    public Row getRow(int index) {
        return new Row(index);
    }

    /**
     * Найти первую строку, содержащую указанный текст.
     */
    public Row getRow(String text) {
        logger.info("Поиск строки с текстом '{}' в таблице '{}'", text, name);
        ElementsCollection rows = element.$$(By.xpath(ROW_LOCATOR));
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getText().contains(text)) {
                return new Row(i);
            }
        }
        throw new NoSuchElementException("Строка с текстом '" + text + "' не найдена в таблице " + name);
    }

    /**
     * Получить количество отображаемых строк.
     */
    @Step("Получить количество строк в таблице '{this.name}'")
    public int getRowCount() {
        if (!element.exists() || !element.isDisplayed()) {
            return 0;
        }
        return element.$$(By.xpath(ROW_LOCATOR)).size();
    }

    /**
     * Получить все данные таблицы в виде списка Map.
     * Аналог старого parseTable().
     */
    @Step("Получить все данные таблицы '{this.name}' как список Map")
    public List<Map<String, String>> getRowsAsMaps() {
        List<String> headers = getHeaders();
        List<Map<String, String>> result = new ArrayList<>();
        ElementsCollection rows = element.$$(By.xpath(ROW_LOCATOR));

        for (SelenideElement row : rows) {
            ElementsCollection cells = row.$$(By.xpath(CELL_LOCATOR));
            Map<String, String> rowData = new HashMap<>();
            // Бежим до min(headers, cells), чтобы не упасть на colspan или скрытых колонках
            for (int i = 0; i < Math.min(headers.size(), cells.size()); i++) {
                rowData.put(headers.get(i), cells.get(i).getText());
            }
            result.add(rowData);
        }
        return result;
    }

    // =================================================================================================================
    // Методы проверок (Assertions)
    // =================================================================================================================

    /**
     * Проверяет наличие строки с указанными данными (Partial Match).
     * Аналог checkTableContainsRowWithData.
     */
    @Step("Таблица '{this.name}' должна содержать строку с данными: {data}")
    public Table shouldHaveRow(Map<String, String> data) {
        List<Map<String, String>> actualRows = getRowsAsMaps();
        boolean match = actualRows.stream().anyMatch(row -> isMapSubset(data, row));

        if (!match) {
            throw new AssertionError(String.format(
                    "В таблице '%s' не найдена строка с данными %s.\nФактические данные:\n%s",
                    name, data, actualRows));
        }
        return this;
    }

    /**
     * Проверяет отсутствие строки с указанными данными.
     * Аналог checkRowIsAbsent.
     */
    @Step("Таблица '{this.name}' НЕ должна содержать строку с данными: {data}")
    public Table shouldNotHaveRow(Map<String, String> data) {
        List<Map<String, String>> actualRows = getRowsAsMaps();
        boolean match = actualRows.stream().anyMatch(row -> isMapSubset(data, row));

        if (match) {
            throw new AssertionError(String.format(
                    "В таблице '%s' найдена строка, которой быть не должно: %s", name, data));
        }
        return this;
    }

    @Step("Таблица '{this.name}' должна быть пустой")
    public Table shouldBeEmpty() {
        int count = getRowCount();
        if (count > 0) {
            throw new AssertionError("Таблица '" + name + "' должна быть пустой, но найдено строк: " + count);
        }
        return this;
    }

    @Step("Таблица '{this.name}' должна содержать заголовки: {expectedHeaders}")
    public Table shouldHaveHeaders(String... expectedHeaders) {
        List<String> actualHeaders = getHeaders();
        List<String> expectedList = Arrays.asList(expectedHeaders);
        if (!new HashSet<>(actualHeaders).containsAll(expectedList)) {
            throw new AssertionError(String.format(
                    "В таблице '%s' отсутствуют ожидаемые заголовки.\nОжидалось: %s\nФактически: %s",
                    name, expectedList, actualHeaders));
        }
        return this;
    }

    // =================================================================================================================
    // Сложная логика (Refresh loop)
    // =================================================================================================================

    /**
     * Обновляет страницу, пока в таблице не появится строка с данными.
     * Аналог refreshPageUntilTableHasRowWithData.
     */
    @Step("Обновлять страницу, пока в таблице '{this.name}' не появится строка: {expectedData}")
    public Table refreshPageUntilDataAppears(Map<String, String> expectedData, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                shouldHaveRow(expectedData);
                logger.info("Данные найдены после {} обновлений", i);
                return this;
            } catch (AssertionError e) {
                if (i == maxRetries - 1) throw e;
                logger.info("Данные не найдены. Обновление страницы (попытка {}/{})", i + 1, maxRetries);
                Selenide.refresh();
                waitForLoad(); // Метод ожидания загрузки (см. ниже)
            }
        }
        return this;
    }

    // =================================================================================================================
    // Внутренний класс Row
    // =================================================================================================================

    public class Row {
        private final int index;
        private final SelenideElement rowElement;

        public Row(int index) {
            this.index = index;
            // Используем ленивый поиск строки внутри таблицы
            this.rowElement = element.$$(By.xpath(ROW_LOCATOR)).get(index);
        }

        /**
         * Получить ячейку по имени колонки.
         */
        public SelenideElement getCell(String columnName) {
            int colIndex = getColumnIndex(columnName);
            return rowElement.$$(By.xpath(CELL_LOCATOR)).get(colIndex);
        }

        /**
         * Получить текст ячейки.
         */
        @Step("Получить текст из колонки '{columnName}' в строке {this.index}")
        public String getValue(String columnName) {
            return getCell(columnName).getText();
        }

        /**
         * Клик по строке.
         */
        @Step("Клик по строке {this.index}")
        public void click() {
            rowElement.scrollIntoView(true).click();
        }

        /**
         * Двойной клик по строке.
         */
        @Step("Двойной клик по строке {this.index}")
        public void doubleClick() {
            rowElement.scrollIntoView(true).doubleClick();
        }

        /**
         * Заполнение инпутов внутри строки.
         * Аналог enterInputsInRow.
         */
        @Step("Заполнить инпуты в строке {this.index}: {values}")
        public void fillInputs(Map<String, String> values) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                SelenideElement cell = getCell(entry.getKey());
                SelenideElement input = cell.$("input"); // Ищем input внутри ячейки
                
                if (!input.exists()) {
                    // Fallback для AntDesign: иногда input лежит глубже или имеет специфичный класс
                    input = cell.$(By.xpath(".//input[contains(@class, 'ant-input')]"));
                }
                
                input.shouldBe(visible).clear();
                input.setValue(entry.getValue());
            }
        }

        /**
         * Клик по кнопке действия внутри строки (например "Редактировать", "Удалить").
         * Ищет по title или тексту.
         */
        @Step("Нажать кнопку '{actionName}' в строке {this.index}")
        public void clickAction(String actionName) {
            // Пытаемся найти по title (как в старом коде) или по тексту
            SelenideElement btn = rowElement.$(By.xpath(".//*[@title='" + actionName + "' or text()='" + actionName + "']"));
            btn.shouldBe(visible).click();
        }

        /**
         * Проверить значения в конкретной строке.
         * Аналог checkTableValues.
         */
        @Step("Проверить значения в строке {this.index}")
        public Row shouldHaveValues(Map<String, String> expectedValues) {
            for (Map.Entry<String, String> entry : expectedValues.entrySet()) {
                String actual = getValue(entry.getKey());
                if (!actual.equals(entry.getValue())) {
                    throw new AssertionError(String.format(
                            "В строке %d колонка '%s' ожидалась '%s', но была '%s'",
                            index, entry.getKey(), entry.getValue(), actual));
                }
            }
            return this;
        }
    }

    // =================================================================================================================
    // Вспомогательные методы
    // =================================================================================================================

    private int getColumnIndex(String columnName) {
        List<String> headers = getHeaders();
        int index = headers.indexOf(columnName);
        if (index == -1) {
            throw new IllegalArgumentException("Колонка '" + columnName + "' не найдена. Доступные: " + headers);
        }
        return index;
    }

    /**
     * Проверяет, что expected является подмножеством actual.
     * (Замена Maps.difference из Guava)
     */
    private boolean isMapSubset(Map<String, String> expected, Map<String, String> actual) {
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String actualValue = actual.get(entry.getKey());
            // Сравниваем значения, учитывая null
            if (actualValue == null || !actualValue.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Метод ожидания загрузки после рефреша.
     * В старом коде был waitForOverlayToDisappear.
     * Здесь нужно реализовать ожидание исчезновения спиннеров AntDesign.
     */
    private void waitForLoad() {
        // Ждем исчезновения спиннера AntDesign
        $$(".ant-spin-spinning").shouldHave(CollectionCondition.size(0), Duration.ofSeconds(10));
        // Ждем появления таблицы
        element.shouldBe(visible, Duration.ofSeconds(10));
    }
}