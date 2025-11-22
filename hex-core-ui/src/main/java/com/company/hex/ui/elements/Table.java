package com.company.hex.ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.visible;

/**
 * Умная таблица.
 * Позволяет работать с ячейками по именам колонок, а не по индексам.
 */
public class Table extends BaseElement {

    // Локаторы по умолчанию (можно переопределить через сеттеры или конструктор)
    @Getter @Setter
    private String headerLocator = ".//thead//th";
    @Getter @Setter
    private String rowLocator = ".//tbody//tr";
    @Getter @Setter
    private String cellLocator = ".//td";

    public Table(String name, SelenideElement element) {
        super(name, element);
    }

    /**
     * Получить список заголовков таблицы.
     */
    @Step("Получить заголовки таблицы '{this.name}'")
    public List<String> getHeaders() {
        return element.$$(headerLocator).texts();
    }

    /**
     * Получить строку по индексу (начиная с 0).
     */
    public Row getRow(int index) {
        return new Row(index);
    }

    /**
     * Найти первую строку, где в колонке columnName содержится value.
     * Пример: table.getRow("Email", "user@example.com")
     */
    @Step("Найти строку в '{this.name}', где '{columnName}' содержит '{value}'")
    public Row getRow(String columnName, String value) {
        int colIndex = getColumnIndex(columnName);

        // Ищем строку, у которой в нужной ячейке есть нужный текст
        // Используем stream для поиска индекса строки (это быстрее и надежнее сложных xpath)
        ElementsCollection rows = element.$$(rowLocator);

        for (int i = 0; i < rows.size(); i++) {
            SelenideElement cell = rows.get(i).$$(cellLocator).get(colIndex);
            if (cell.getText().contains(value)) {
                return new Row(i);
            }
        }

        throw new NoSuchElementException(
                String.format("В таблице '%s' не найдена строка, где колонка '%s' содержит '%s'",
                        name, columnName, value));
    }

    /**
     * Получить все данные таблицы в виде списка Map, где ключ - заголовок, значение - текст ячейки.
     * Удобно для assert-ов всей таблицы целиком.
     */
    @Step("Получить все данные из таблицы '{this.name}'")
    public List<Map<String, String>> getAllData() {
        List<String> headers = getHeaders();
        List<Map<String, String>> data = new ArrayList<>();

        ElementsCollection rows = element.$$(rowLocator);
        for (SelenideElement rowElement : rows) {
            ElementsCollection cells = rowElement.$$(cellLocator);
            Map<String, String> rowData = new HashMap<>();

            // Бежим по ячейкам, но не выходим за границы заголовков
            for (int i = 0; i < Math.min(headers.size(), cells.size()); i++) {
                rowData.put(headers.get(i), cells.get(i).getText());
            }
            data.add(rowData);
        }
        return data;
    }

    /**
     * Проверить, что таблица пуста (нет строк данных).
     * Полезно для проверки фильтров, которые ничего не нашли.
     */
    @Step("Таблица должна быть пустой")
    public void shouldBeEmpty() {
        element.$$(rowLocator).shouldHave(size(0));
    }

    /**
     * Проверить, что таблица содержит ожидаемое количество строк.
     */
    @Step("Таблица должна содержать {expectedSize} строк")
    public void shouldHaveSize(int expectedSize) {
        element.$$(rowLocator).shouldHave(size(expectedSize));
    }

    /**
     * Ждать, пока в таблице появится хотя бы одна строка.
     * Полезно после перезагрузки страницы.
     */
    @Step("Ждать загрузки данных в таблицу")
    public Table waitUntilDataLoaded() {
        element.$$(rowLocator).first().shouldBe(visible, Duration.ofSeconds(10));
        return this;
    }

    // --- Внутренняя логика ---

    private int getColumnIndex(String columnName) {
        List<String> headers = getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                String.format("Колонка '%s' не найдена в таблице '%s'. Доступные колонки: %s",
                        columnName, name, headers));
    }

    /**
     * Внутренний класс, представляющий строку таблицы.
     * Предоставляет доступ к ячейкам этой конкретной строки.
     */
    public class Row {
        private final int index;
        private final SelenideElement rowElement;

        public Row(int index) {
            this.index = index;
            this.rowElement = element.$$(rowLocator).get(index);
        }

        /**
         * Получить корневой элемент строки (tr) для проверок (например, shouldBe(visible)).
         */
        public SelenideElement getSelf() {
            return rowElement;
        }

        /**
         * Получить ячейку по имени колонки.
         * Возвращает SelenideElement, с которым можно делать click(), getText() и т.д.
         */
        @Step("Получить ячейку колонки '{columnName}' в строке {this.index}")
        public SelenideElement getCell(String columnName) {
            int colIndex = getColumnIndex(columnName);
            return rowElement.$$(cellLocator).get(colIndex);
        }

        /**
         * Получить ячейку по индексу.
         */
        public SelenideElement getCell(int colIndex) {
            return rowElement.$$(cellLocator).get(colIndex);
        }

        /**
         * Удобный метод для клика по кнопке/ссылке внутри конкретной колонки.
         * Например: row.clickInColumn("Actions", "button.edit")
         */
        @Step("Кликнуть по элементу '{selector}' в колонке '{columnName}'")
        public void clickInColumn(String columnName, String selector) {
            getCell(columnName).$(selector).click();
        }
    }
}