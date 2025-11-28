package com.company.hex.project.components;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseComponent;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.TextElement;
import com.company.hex.ui.annotations.Element;
import io.qameta.allure.Step;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.textsInAnyOrder;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$$x;

/**
 * Компонент для работы с Ant Design Select.
 * Обрабатывает сложность с выпадающим списком, который рендерится вне DOM-дерева компонента.
 */
public class ModalWindow extends BaseComponent {

    // Локатор кнопки открытия списка (находится ВНУТРИ root компонента)
    @Element(name = "Select Trigger", xpath = ".//div[contains(@class, 'ant-select-selector')]")
    private Button trigger;

    // Локатор текущего выбранного значения (ВНУТРИ root)
    @Element(name = "Current Value", xpath = ".//span[contains(@class, 'ant-select-selection-item')]")
    private TextElement currentValue;

    // Локатор кнопки очистки (крестик), если есть
    @Element(name = "Clear Icon", xpath = ".//span[contains(@class, 'ant-select-clear')]")
    private Button clearIcon;

    // Глобальный локатор для выпадающего слоя (находится в body, ВНЕ root)
    // Ant Design добавляет класс 'ant-select-dropdown' и убирает 'hidden' при открытии
    private static final String DROPDOWN_LAYER_XPATH =
            "//div[contains(@class, 'ant-select-dropdown') and not(contains(@class, 'hidden'))]";

    private static final String OPTIONS_XPATH =
            ".//div[contains(@class, 'ant-select-item-option')]";

    /**
     * Получить текущее выбранное значение.
     */
    @Step("Получить текущее значение из '{this.componentName}'")
    public String getValue() {
        return currentValue.getText();
    }

    /**
     * Выбрать значение из списка.
     *
     * @param value текст опции, которую нужно выбрать
     */
    @Step("Выбрать значение '{value}' в '{this.componentName}'")
    public ModalWindow select(String value) {
        // 1. Если значение уже выбрано - ничего не делаем (оптимизация)
        if (currentValue.exists() && value.equals(currentValue.getText())) {
            logger.info("Значение '{}' уже выбрано в '{}'", value, getComponentName());
            return this;
        }

        // 2. Открываем список
        openDropdown();

        // 3. Ищем опцию в глобальном слое и кликаем
        // Используем $$x, так как ищем от корня страницы, а не от this.element
        SelenideElement option = $$x(DROPDOWN_LAYER_XPATH + OPTIONS_XPATH)
                .findBy(text(value));

        logger.info("Клик по опции '{}'", value);
        option.shouldBe(visible).click();

        // 4. Ждем, пока список закроется (опционально, для стабильности)
        $$x(DROPDOWN_LAYER_XPATH).shouldHave(size(0), Duration.ofMillis(500));

        return this;
    }

    /**
     * Проверить наличие опций в списке.
     *
     * @param expectedOptions ожидаемые опции
     */
    @Step("Проверить, что '{this.componentName}' содержит опции: {expectedOptions}")
    public ModalWindow shouldHaveOptions(List<String> expectedOptions) {
        openDropdown();

        // Проверяем коллекцию элементов в выпадающем слое
        $$x(DROPDOWN_LAYER_XPATH + OPTIONS_XPATH)
                .shouldHave(textsInAnyOrder(expectedOptions));

        // Закрываем список кликом по триггеру или ESC (если нужно)
        // trigger.click();
        return this;
    }

    /**
     * Очистить значение (если доступно).
     */
    @Step("Очистить значение в '{this.componentName}'")
    public ModalWindow clear() {
        trigger.hover(); // Крестик часто появляется только при ховере
        if (clearIcon.isDisplayed()) {
            clearIcon.click();
        } else {
            logger.warn("Иконка очистки не найдена в '{}'", getComponentName());
        }
        return this;
    }

    /**
     * Приватный метод для открытия списка.
     * Проверяет, открыт ли он уже, чтобы не закрыть кликом.
     */
    private void openDropdown() {
        // Проверяем, есть ли видимый слой дропдауна
        boolean isOpened = $$x(DROPDOWN_LAYER_XPATH).filter(visible).size() > 0;

        if (!isOpened) {
            logger.info("Открытие выпадающего списка '{}'", getComponentName());
            trigger.click();
            // Ждем появления слоя
            $$x(DROPDOWN_LAYER_XPATH).shouldHave(size(1), Duration.ofSeconds(4));
        }
    }
}
