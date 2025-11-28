package com.company.hex.ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.CollectionCondition.texts;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Элемент выпадающего списка (Dropdown/Select).
 * Адаптирован для Ant Design (React), где опции рендерятся в отдельном слое (Portal).
 *
 * @author Hex Framework
 */
public class Dropdown extends BaseElement {

    // Локаторы из старого проекта (Ant Design specific)
    private static final String SELECTOR_VALUE_XPATH = ".//*[@class='ant-select-selection-item' or @class='ant-select-selection-placeholder']";
    // Локатор глобального слоя с опциями (ищем видимый, так как AntD создает их в body)
    private static final String DROPDOWN_LAYER_XPATH = "//div[contains(@class, 'ant-select-dropdown') and not(contains(@class, 'hidden'))]";
    private static final String OPTION_CONTENT_XPATH = ".//*[contains(@class, 'ant-select-item-option-content') or contains(@class, 'item-option-content')]";

    public Dropdown(String name, SelenideElement element) {
        super(name, element);
    }

    /**
     * Получить текущее выбранное значение (текст в поле).
     */
    @Override
    @Step("Получить выбранное значение в '{this.name}'")
    public String getText() {
        logger.debug("Получение текста из '{}' {}", name, getContext());
        // В AntD текст лежит не в корне, а в span'е selection-item
        SelenideElement valueElement = element.$x(SELECTOR_VALUE_XPATH);
        // Если элемент пустой (placeholder), берем его, иначе ищем item
        String text = valueElement.exists() ? valueElement.getText() : element.getText();
        logger.debug("Текст из '{}': '{}'", name, text);
        return text;
    }

    /**
     * Выбрать значение по тексту.
     *
     * @param value текст опции
     * @return this
     */
    @Step("Выбрать значение '{value}' в '{this.name}'")
    public Dropdown select(String value) {
        logger.info("Выбор значения '{}' в '{}' {}", value, name, getContext());

        // 1. Проверяем, может уже выбрано?
        if (value.equals(getText())) {
            logger.debug("Значение '{}' уже выбрано в '{}'", value, name);
            return this;
        }

        // 2. Кликаем по дропдауну, чтобы открыть список
        expand();

        // 3. Ищем опцию в появившемся слое и кликаем
        SelenideElement option = getPopupOptions().find(text(value));

        if (!option.exists()) {
            // Пытаемся закрыть, чтобы не мешать другим тестам, если опция не найдена
            collapse();
            throw new AssertionError(String.format("Опция '%s' не найдена в выпадающем списке '%s'", value, name));
        }

        option.click();

        // 4. Ждем, пока слой исчезнет (анимация закрытия)
        getPopupLayer().should(disappear);

        return this;
    }

    /**
     * Проверить наличие списка опций.
     * Метод открывает список, проверяет и закрывает его.
     *
     * @param expectedOptions ожидаемые опции
     * @return this
     */
    @Step("'{this.name}' должен содержать опции {expectedOptions}")
    public Dropdown shouldHaveOptions(List<String> expectedOptions) {
        logger.info("Проверка опций в '{}': {} {}", name, expectedOptions, getContext());

        expand();

        try {
            getPopupOptions().shouldHave(texts(expectedOptions));
        } finally {
            collapse();
        }
        return this;
    }

    /**
     * Открыть выпадающий список (если закрыт).
     */
    @Step("Открыть список '{this.name}'")
    public Dropdown expand() {
        if (!isExpanded()) {
            logger.debug("Открытие списка '{}'", name);
            element.click();
            // Ждем появления слоя
            getPopupLayer().shouldBe(visible, Duration.ofSeconds(5));
        }
        return this;
    }

    /**
     * Закрыть выпадающий список (если открыт).
     */
    @Step("Закрыть список '{this.name}'")
    public Dropdown collapse() {
        if (isExpanded()) {
            logger.debug("Закрытие списка '{}'", name);
            // В AntD клик по селектору закрывает его, либо можно нажать ESC
            element.click();
            // Либо element.pressEscape();
        }
        return this;
    }

    /**
     * Получить все доступные опции в виде списка строк.
     */
    @Step("Получить список всех опций из '{this.name}'")
    public List<String> getOptions() {
        expand();
        List<String> options;
        try {
            options = getPopupOptions().texts();
        } finally {
            collapse();
        }
        return options;
    }

    // --- Private Helpers ---

    /**
     * Находит слой выпадающего списка (Portal), привязанный к body.
     * Ищем тот, который сейчас видим и не скрыт.
     */
    private SelenideElement getPopupLayer() {
        return $x(DROPDOWN_LAYER_XPATH);
    }

    /**
     * Находит коллекцию опций внутри активного слоя.
     */
    private ElementsCollection getPopupOptions() {
        return getPopupLayer().$$x(OPTION_CONTENT_XPATH)
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(5));
    }

    /**
     * Проверяет, открыт ли список.
     * В AntD наличие класса ant-select-open на корневом элементе или наличие видимого слоя говорит об открытии.
     */
    private boolean isExpanded() {
        // Вариант 1: Проверка класса на самом элементе (часто ant-select-open)
        if (element.has(cssClass("ant-select-open"))) {
            return true;
        }
        // Вариант 2: Проверка видимости слоя (как в старом коде)
        return $x(DROPDOWN_LAYER_XPATH).isDisplayed();
    }
}