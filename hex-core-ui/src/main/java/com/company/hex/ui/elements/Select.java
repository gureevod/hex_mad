package com.company.hex.ui.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;

import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.selected;

/**
 * Класс для работы с выпадающими списками (select).
 * Предоставляет методы для выбора опций по тексту, значению или индексу.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Element(name = "Country", xpath = "//select[@id='country']")
 * Select country;
 * 
 * country.selectByText("United States")
 *        .shouldHaveSelectedText("United States");
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class Select extends BaseElement {
    
    /**
     * Конструктор для создания Select элемента.
     * 
     * @param name имя элемента для логирования
     * @param element Selenide элемент
     */
    public Select(String name, SelenideElement element) {
        super(name, element);
    }
    
    /**
     * Выбрать опцию по видимому тексту.
     * 
     * @param text видимый текст опции
     * @return this для fluent API
     */
    @Step("Выбрать в '{this.name}' опцию с текстом '{text}'")
    public Select selectByText(String text) {
        logger.info("Выбор в '{}' опции с текстом '{}' {}", name, text, getContext());
        element.selectOption(text);
        logger.debug("Опция '{}' успешно выбрана в '{}'", text, name);
        return this;
    }
    
    /**
     * Выбрать опцию по значению атрибута value.
     * 
     * @param value значение атрибута value
     * @return this для fluent API
     */
    @Step("Выбрать в '{this.name}' опцию со значением '{value}'")
    public Select selectByValue(String value) {
        logger.info("Выбор в '{}' опции со значением '{}' {}", name, value, getContext());
        element.selectOptionByValue(value);
        logger.debug("Опция со значением '{}' успешно выбрана в '{}'", value, name);
        return this;
    }
    
    /**
     * Выбрать опцию по индексу (начиная с 0).
     * 
     * @param index индекс опции
     * @return this для fluent API
     */
    @Step("Выбрать в '{this.name}' опцию с индексом {index}")
    public Select selectByIndex(int index) {
        logger.info("Выбор в '{}' опции с индексом {} {}", name, index, getContext());
        element.selectOption(index);
        logger.debug("Опция с индексом {} успешно выбрана в '{}'", index, name);
        return this;
    }
    
    /**
     * Получить текст выбранной опции.
     * 
     * @return текст выбранной опции
     */
    @Step("Получить выбранный текст из '{this.name}'")
    public String getSelectedText() {
        logger.debug("Получение выбранного текста из '{}' {}", name, getContext());
        String selectedText = element.getSelectedOption().getText();
        logger.debug("Выбранный текст в '{}': '{}'", name, selectedText);
        return selectedText;
    }
    
    /**
     * Получить значение (value) выбранной опции.
     * 
     * @return значение выбранной опции
     */
    @Step("Получить выбранное значение из '{this.name}'")
    public String getSelectedValue() {
        logger.debug("Получение выбранного значения из '{}' {}", name, getContext());
        String selectedValue = element.getSelectedOption().getValue();
        logger.debug("Выбранное значение в '{}': '{}'", name, selectedValue);
        return selectedValue;
    }
    
    /**
     * Получить все доступные опции (тексты).
     *
     * @return список текстов всех опций
     */
    @Step("Получить все опции из '{this.name}'")
    public List<String> getAllOptions() {
        logger.debug("Получение всех опций из '{}' {}", name, getContext());
        List<String> options = element.$$("option").texts();
        logger.debug("Количество опций в '{}': {}", name, options.size());
        return options;
    }
    
    /**
     * Получить все значения (value) опций.
     *
     * @return список значений всех опций
     */
    @Step("Получить все значения опций из '{this.name}'")
    public List<String> getAllOptionValues() {
        logger.debug("Получение всех значений опций из '{}' {}", name, getContext());
        List<String> values = element.$$("option")
                .asFixedIterable()
                .stream()
                .map(SelenideElement::getValue)
                .collect(Collectors.toList());
        logger.debug("Количество значений опций в '{}': {}", name, values.size());
        return values;
    }
    
    /**
     * Проверить, что выбрана опция с указанным текстом.
     * 
     * @param expectedText ожидаемый текст
     * @return this для fluent API
     */
    @Step("'{this.name}' должен иметь выбранный текст '{expectedText}'")
    public Select shouldHaveSelectedText(String expectedText) {
        logger.info("Проверка '{}' должен иметь выбранный текст '{}' {}", name, expectedText, getContext());
        element.getSelectedOption().shouldHave(com.codeborne.selenide.Condition.text(expectedText));
        return this;
    }
    
    /**
     * Проверить, что выбрана опция с указанным значением.
     * 
     * @param expectedValue ожидаемое значение
     * @return this для fluent API
     */
    @Step("'{this.name}' должен иметь выбранное значение '{expectedValue}'")
    public Select shouldHaveSelectedValue(String expectedValue) {
        logger.info("Проверка '{}' должен иметь выбранное значение '{}' {}", name, expectedValue, getContext());
        element.getSelectedOption().shouldHave(com.codeborne.selenide.Condition.value(expectedValue));
        return this;
    }
    
    /**
     * Проверить, что список содержит указанную опцию.
     * 
     * @param optionText текст опции
     * @return this для fluent API
     */
    @Step("'{this.name}' должен содержать опцию '{optionText}'")
    public Select shouldHaveOption(String optionText) {
        logger.info("Проверка '{}' должен содержать опцию '{}' {}", name, optionText, getContext());
        List<String> options = getAllOptions();
        if (!options.contains(optionText)) {
            throw new AssertionError(String.format(
                    "Select '%s' не содержит опцию '%s'. Доступные опции: %s",
                    name, optionText, options));
        }
        return this;
    }
    
    /**
     * Проверить количество опций в списке.
     *
     * @param expectedCount ожидаемое количество
     * @return this для fluent API
     */
    @Step("'{this.name}' должен иметь {expectedCount} опций")
    public Select shouldHaveOptionsCount(int expectedCount) {
        logger.info("Проверка '{}' должен иметь {} опций {}", name, expectedCount, getContext());
        int actualCount = element.$$("option").size();
        if (actualCount != expectedCount) {
            throw new AssertionError(String.format(
                    "Select '%s' должен иметь %d опций, но имеет %d",
                    name, expectedCount, actualCount));
        }
        return this;
    }
}