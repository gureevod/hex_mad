package com.company.hex.ui.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;

/**
 * Класс для работы с текстовыми элементами (div, span, p, label и т.д.).
 * Предоставляет методы для получения и проверки текста.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Element(name = "Welcome Message", xpath = "//h1[@class='welcome']")
 * TextElement welcomeMessage;
 * 
 * welcomeMessage.shouldHave(text("Welcome, User!"))
 *               .shouldBe(visible);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class TextElement extends BaseElement {
    
    /**
     * Конструктор для создания TextElement.
     * 
     * @param name имя элемента для логирования
     * @param element Selenide элемент
     */
    public TextElement(String name, SelenideElement element) {
        super(name, element);
    }
    
    /**
     * Получить текст элемента.
     * Переопределяет базовый метод для возврата типа String.
     * 
     * @return текст элемента
     */
    @Override
    @Step("Получить текст из '{this.name}'")
    public String getText() {
        logger.debug("Получение текста из '{}' {}", name, getContext());
        String text = element.getText();
        logger.debug("Текст из '{}': '{}'", name, text);
        return text;
    }
    
    /**
     * Получить внутренний текст элемента (innerText).
     * 
     * @return внутренний текст
     */
    @Step("Получить innerText из '{this.name}'")
    public String getInnerText() {
        logger.debug("Получение innerText из '{}' {}", name, getContext());
        String innerText = element.innerText();
        logger.debug("InnerText из '{}': '{}'", name, innerText);
        return innerText;
    }
    
    /**
     * Получить собственный текст элемента (без текста дочерних элементов).
     * 
     * @return собственный текст элемента
     */
    @Step("Получить собственный текст из '{this.name}'")
    public String getOwnText() {
        logger.debug("Получение собственного текста из '{}' {}", name, getContext());
        String ownText = element.getOwnText();
        logger.debug("Собственный текст из '{}': '{}'", name, ownText);
        return ownText;
    }
    
    /**
     * Проверить, что элемент содержит указанный текст.
     * 
     * @param expectedText ожидаемый текст
     * @return this для fluent API
     */
    @Step("'{this.name}' должен содержать текст '{expectedText}'")
    public TextElement shouldHaveText(String expectedText) {
        logger.info("Проверка '{}' должен содержать текст '{}' {}", name, expectedText, getContext());
        element.shouldHave(text(expectedText));
        return this;
    }
    
    /**
     * Проверить, что элемент содержит точный текст (без учета пробелов).
     * 
     * @param expectedText ожидаемый точный текст
     * @return this для fluent API
     */
    @Step("'{this.name}' должен иметь точный текст '{expectedText}'")
    public TextElement shouldHaveExactText(String expectedText) {
        logger.info("Проверка '{}' должен иметь точный текст '{}' {}", name, expectedText, getContext());
        element.shouldHave(exactText(expectedText));
        return this;
    }
    
    /**
     * Проверить, что элемент содержит текст с учетом регистра.
     * 
     * @param expectedText ожидаемый текст
     * @return this для fluent API
     */
    @Step("'{this.name}' должен содержать текст '{expectedText}' (с учетом регистра)")
    public TextElement shouldHaveTextCaseSensitive(String expectedText) {
        logger.info("Проверка '{}' должен содержать текст '{}' (с учетом регистра) {}", 
                name, expectedText, getContext());
        element.shouldHave(exactTextCaseSensitive(expectedText));
        return this;
    }
    
    /**
     * Проверить, что элемент НЕ содержит указанный текст.
     * 
     * @param unexpectedText нежелательный текст
     * @return this для fluent API
     */
    @Step("'{this.name}' НЕ должен содержать текст '{unexpectedText}'")
    public TextElement shouldNotHaveText(String unexpectedText) {
        logger.info("Проверка '{}' НЕ должен содержать текст '{}' {}", name, unexpectedText, getContext());
        element.shouldNotHave(text(unexpectedText));
        return this;
    }
    
    /**
     * Проверить, что элемент пустой (не содержит текста).
     * 
     * @return this для fluent API
     */
    @Step("'{this.name}' должен быть пустым")
    public TextElement shouldBeEmpty() {
        logger.info("Проверка '{}' должен быть пустым {}", name, getContext());
        element.shouldHave(empty);
        return this;
    }
    
    /**
     * Проверить, что элемент не пустой (содержит текст).
     * 
     * @return this для fluent API
     */
    @Step("'{this.name}' НЕ должен быть пустым")
    public TextElement shouldNotBeEmpty() {
        logger.info("Проверка '{}' НЕ должен быть пустым {}", name, getContext());
        element.shouldNotBe(empty);
        return this;
    }
    
    /**
     * Проверить, что текст элемента соответствует регулярному выражению.
     * 
     * @param regex регулярное выражение
     * @return this для fluent API
     */
    @Step("'{this.name}' должен соответствовать regex '{regex}'")
    public TextElement shouldMatchText(String regex) {
        logger.info("Проверка '{}' должен соответствовать regex '{}' {}", name, regex, getContext());
        element.shouldHave(matchText(regex));
        return this;
    }
    
    /**
     * Проверить длину текста элемента.
     * 
     * @param expectedLength ожидаемая длина
     * @return this для fluent API
     */
    @Step("'{this.name}' должен иметь длину текста {expectedLength}")
    public TextElement shouldHaveTextLength(int expectedLength) {
        logger.info("Проверка '{}' должен иметь длину текста {} {}", name, expectedLength, getContext());
        String actualText = getText();
        int actualLength = actualText.length();
        if (actualLength != expectedLength) {
            throw new AssertionError(String.format(
                    "Элемент '%s' должен иметь длину текста %d, но имеет %d. Текст: '%s'",
                    name, expectedLength, actualLength, actualText));
        }
        return this;
    }
    
    /**
     * Проверить, что текст начинается с указанной строки.
     * 
     * @param prefix ожидаемый префикс
     * @return this для fluent API
     */
    @Step("'{this.name}' должен начинаться с '{prefix}'")
    public TextElement shouldStartWith(String prefix) {
        logger.info("Проверка '{}' должен начинаться с '{}' {}", name, prefix, getContext());
        String actualText = getText();
        if (!actualText.startsWith(prefix)) {
            throw new AssertionError(String.format(
                    "Элемент '%s' должен начинаться с '%s', но текст: '%s'",
                    name, prefix, actualText));
        }
        return this;
    }
    
    /**
     * Проверить, что текст заканчивается указанной строкой.
     * 
     * @param suffix ожидаемый суффикс
     * @return this для fluent API
     */
    @Step("'{this.name}' должен заканчиваться на '{suffix}'")
    public TextElement shouldEndWith(String suffix) {
        logger.info("Проверка '{}' должен заканчиваться на '{}' {}", name, suffix, getContext());
        String actualText = getText();
        if (!actualText.endsWith(suffix)) {
            throw new AssertionError(String.format(
                    "Элемент '%s' должен заканчиваться на '%s', но текст: '%s'",
                    name, suffix, actualText));
        }
        return this;
    }
}