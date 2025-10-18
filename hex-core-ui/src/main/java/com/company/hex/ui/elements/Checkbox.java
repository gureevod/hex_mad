package com.company.hex.ui.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.checked;

/**
 * Класс для работы с чекбоксами (checkbox).
 * Предоставляет методы для установки, снятия и проверки состояния чекбокса.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Element(name = "Accept Terms", xpath = "//input[@type='checkbox']")
 * Checkbox acceptTerms;
 * 
 * acceptTerms.check()
 *            .shouldBe(checked)
 *            .shouldBe(visible, enabled);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class Checkbox extends BaseElement {
    
    /**
     * Конструктор для создания Checkbox элемента.
     * 
     * @param name имя элемента для логирования
     * @param element Selenide элемент
     */
    public Checkbox(String name, SelenideElement element) {
        super(name, element);
    }
    
    /**
     * Установить чекбокс (отметить).
     * Если чекбокс уже отмечен, ничего не делает.
     * 
     * @return this для fluent API
     */
    @Step("Установить чекбокс '{this.name}'")
    public Checkbox check() {
        logger.info("Установка чекбокса '{}' {}", name, getContext());
        if (!isChecked()) {
            element.click();
            logger.debug("Чекбокс '{}' успешно установлен", name);
        } else {
            logger.debug("Чекбокс '{}' уже установлен", name);
        }
        return this;
    }
    
    /**
     * Снять чекбокс (убрать отметку).
     * Если чекбокс уже снят, ничего не делает.
     * 
     * @return this для fluent API
     */
    @Step("Снять чекбокс '{this.name}'")
    public Checkbox uncheck() {
        logger.info("Снятие чекбокса '{}' {}", name, getContext());
        if (isChecked()) {
            element.click();
            logger.debug("Чекбокс '{}' успешно снят", name);
        } else {
            logger.debug("Чекбокс '{}' уже снят", name);
        }
        return this;
    }
    
    /**
     * Переключить состояние чекбокса.
     * Если отмечен - снимает, если снят - отмечает.
     * 
     * @return this для fluent API
     */
    @Step("Переключить чекбокс '{this.name}'")
    public Checkbox toggle() {
        logger.info("Переключение чекбокса '{}' {}", name, getContext());
        element.click();
        logger.debug("Чекбокс '{}' успешно переключен", name);
        return this;
    }
    
    /**
     * Проверить, отмечен ли чекбокс.
     * 
     * @return true если чекбокс отмечен
     */
    public boolean isChecked() {
        logger.debug("Проверка состояния чекбокса '{}' {}", name, getContext());
        boolean checked = element.isSelected();
        logger.debug("Чекбокс '{}' {}", name, checked ? "отмечен" : "не отмечен");
        return checked;
    }
    
    /**
     * Установить чекбокс в указанное состояние.
     * 
     * @param shouldBeChecked true для установки, false для снятия
     * @return this для fluent API
     */
    @Step("Установить чекбокс '{this.name}' в состояние: {shouldBeChecked}")
    public Checkbox setChecked(boolean shouldBeChecked) {
        logger.info("Установка чекбокса '{}' в состояние: {} {}", name, shouldBeChecked, getContext());
        if (shouldBeChecked) {
            check();
        } else {
            uncheck();
        }
        return this;
    }
    
    /**
     * Проверить, что чекбокс отмечен.
     * 
     * @return this для fluent API
     */
    @Step("Чекбокс '{this.name}' должен быть отмечен")
    public Checkbox shouldBeChecked() {
        logger.info("Проверка чекбокса '{}' должен быть отмечен {}", name, getContext());
        element.shouldBe(checked);
        return this;
    }
    
    /**
     * Проверить, что чекбокс не отмечен.
     * 
     * @return this для fluent API
     */
    @Step("Чекбокс '{this.name}' должен быть не отмечен")
    public Checkbox shouldBeUnchecked() {
        logger.info("Проверка чекбокса '{}' должен быть не отмечен {}", name, getContext());
        element.shouldNotBe(checked);
        return this;
    }
}