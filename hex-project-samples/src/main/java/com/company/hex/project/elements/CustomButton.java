package com.company.hex.project.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.elements.Button;
import io.qameta.allure.Step;

/**
 * Пример кастомного элемента, который не зарегистрирован в ElementFactory по умолчанию.
 * Используется для проверки JIT регистрации.
 */
public class CustomButton extends Button {

    public CustomButton(String name, SelenideElement element) {
        super(name, element);
    }

    @Step("Custom click on '{this.name}'")
    public void customClick() {
        logger.info("Custom clicking on '{}'", name);
        element.click();
    }
}