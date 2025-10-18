package com.company.hex.ui.builder;

import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.elements.Button;

/**
 * Builder для создания Button элементов с fluent API.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * // Простой локатор
 * Button submit = ButtonBuilder.create("//button[@type='submit']")
 *     .withName("Submit Button")
 *     .build();
 * 
 * // Составной параметризованный локатор
 * Button actionButton = ButtonBuilder.create()
 *     .withName("Action Button")
 *     .locator()
 *         .base("//div[@class='actions']")
 *         .append("//button[@data-action='{action}']")
 *         .build();
 * 
 * actionButton.resolve("action", "save").click();
 * actionButton.resolve("action", "cancel").click();
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ButtonBuilder extends ElementBuilder<Button, ButtonBuilder> {
    
    /**
     * Создать новый ButtonBuilder.
     */
    protected ButtonBuilder() {
        super();
    }
    
    /**
     * Создать новый ButtonBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     */
    protected ButtonBuilder(String xpath) {
        super();
        this.simpleLocator = xpath;
    }
    
    /**
     * Создать новый ButtonBuilder.
     * 
     * @return новый builder
     */
    public static ButtonBuilder create() {
        return new ButtonBuilder();
    }
    
    /**
     * Создать новый ButtonBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый builder
     */
    public static ButtonBuilder create(String xpath) {
        return new ButtonBuilder(xpath);
    }
    
    @Override
    protected Class<? extends BaseElement> getDefaultElementClass() {
        return Button.class;
    }
}