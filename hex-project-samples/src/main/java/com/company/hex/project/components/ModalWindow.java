package com.company.hex.project.components;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.core.BaseComponent;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.Input;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * Компонент для работы с модальным окном поиска (Ant Design Drawer).
 * Включает в себя кнопку открытия (три точки) и логику работы внутри модалки.
 */
public class ModalWindow extends BaseComponent {

    // --- Локаторы внутри компонента (Trigger) ---

    @Element(name = "Кнопка открытия модалки (...)", xpath = ".//button//span[contains(@class, 'anticon-ellipsis')]")
    private Button triggerButton;

    // --- Глобальные локаторы (Modal Content) ---
    // Модалка рендерится в корень body, поэтому ищем глобально, а не от this.element

    // Сложный XPath из легаси кода для надежности
    private static final String INPUT_XPATH = "(//*[@class[contains(.,'ant-drawer-open')]]//*[@class[contains(.,'ant-drawer-content')]]//*[contains(@data-test-id, '_modal_searchInput')])|(//*[@data-test-id = 'ModalFind_search_input'])|(//*[@class = 'ant-drawer-content']// input[contains(@data-test-id, 'searchInput')])";

    // Обертки над глобальными элементами.
    // Мы создаем их "на лету" или через new, так как они вне контекста BaseComponent
    private final Input searchInput = new Input("Поле поиска в модалке", $x(INPUT_XPATH));
    private final Button searchButton = new Button("Кнопка 'Поиск'", $x("//button[text()='Поиск. Модальное окно']")); // Текст из легаси
    private final Button applyButton = new Button("Кнопка 'Применить'", $x("//button[span[text()='Применить']]"));
    private final Button closeButton = new Button("Кнопка закрытия (X)", $x("//button[@aria-label='Close' and contains(@class, 'ant-drawer-close')]"));

    /**
     * Открывает модальное окно.
     * Содержит логику повторной попытки (Retry), если анимация не отработала.
     */
    @Step("Открыть модальное окно '{this.componentName}'")
    public ModalWindow open() {
        // Ждем исчезновения оверлеев (если были)
        $(".ant-spin-nested-loading").shouldNotBe(visible);

        triggerButton.shouldBe(visible, enabled).click();

        try {
            // Ждем появления инпута. Если не появился за 2 секунды - кидаем исключение, которое ловим ниже
            searchInput.getElement().shouldBe(clickable, Duration.ofSeconds(2));
        } catch (AssertionError e) {
            logger.warn("Модальное окно не открылось с первого раза. Пробуем кликнуть еще раз.");
            triggerButton.click();
            searchInput.getElement().shouldBe(clickable, Duration.ofSeconds(5));
        }

        return this;
    }

    /**
     * Выполняет поиск значения в модальном окне.
     */
    @Step("Поиск значения '{value}' в модальном окне")
    public ModalWindow search(String value) {
        searchInput.shouldBe(visible);
        searchInput.clear();
        searchInput.fill(value);

        // Логика проверки результатов из легаси:
        // Если результат не появился сразу, жмем кнопку "Поиск"
        String resultXpath = String.format("//*[@class='ant-drawer-body']//*[@class='ant-card-body']//*[text()[contains(.,'%s')]]", value);

        if (!$x(resultXpath).exists()) {
            if (searchButton.exists() && searchButton.isDisplayed()) {
                searchButton.click();
            }
            // Ждем появления результатов
            $x(resultXpath).shouldBe(visible, Duration.ofSeconds(10));
        }

        return this;
    }

    /**
     * Кликает по найденному значению.
     */
    @Step("Выбрать значение '{value}' из результатов")
    public ModalWindow selectResult(String value) {
        String resultXpath = String.format("//*[@class='ant-drawer-body']//*[@class='ant-card-body']//*[text()[contains(.,'%s')]]", value);

        ElementsCollection results = $$x(resultXpath);
        results.shouldHave(sizeGreaterThan(0));

        // В легаси коде брался последний элемент (raws.get(raws.size() - 1))
        SelenideElement targetElement = results.last();

        new Button("Результат поиска: " + value, targetElement).click();

        // Ждем исчезновения инпута (признак закрытия модалки при одиночном выборе)
        // Если это мультиселект, это ожидание может быть лишним, но для single select оно нужно
        return this;
    }

    /**
     * Полный цикл: Открыть -> Найти -> Выбрать (Single Select).
     */
    @Step("Выбрать '{value}' через модальное окно")
    public void select(String value) {
        open();
        search(value);
        selectResult(value);
        ensureModalClosed();
    }

    /**
     * Множественный выбор: Открыть -> (Найти -> Выбрать) * N -> Применить.
     */
    @Step("Выбрать несколько значений: {values}")
    public void multiSelect(String... values) {
        open();

        for (String value : values) {
            search(value);
            selectResult(value);
            // Очищаем поиск для следующей итерации, если нужно
            searchInput.clear();
        }

        applyButton.click();
        ensureModalClosed();
    }

    /**
     * Закрыть модальное окно вручную (через крестик).
     */
    @Step("Закрыть модальное окно")
    public void close() {
        if (closeButton.isDisplayed()) {
            closeButton.click();
            ensureModalClosed();
        }
    }

    /**
     * Вспомогательный метод проверки закрытия окна.
     */
    private void ensureModalClosed() {
        searchInput.getElement().should(disappear, Duration.ofSeconds(5));
        $(".ant-drawer-mask").should(disappear); // Ждем исчезновения затемнения
    }
}