package com.company.hex.ui.builder;

import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.elements.Input;

/**
 * Builder для создания Input элементов с fluent API.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * // Простой локатор
 * Input email = InputBuilder.create("//input[@type='email']")
 *     .withName("Email Field")
 *     .build();
 * 
 * // Составной локатор
 * Input dynamicField = InputBuilder.create()
 *     .withName("Dynamic Field")
 *     .locator()
 *         .base("//div[@class='form']")
 *         .append("//input[@data-field='{field}']")
 *         .build();
 * 
 * dynamicField.resolve("field", "email").fill("test@example.com");
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class InputBuilder extends ElementBuilder<Input, InputBuilder> {
    
    /**
     * Создать новый InputBuilder.
     */
    protected InputBuilder() {
        super();
    }
    
    /**
     * Создать новый InputBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     */
    protected InputBuilder(String xpath) {
        super();
        this.simpleLocator = xpath;
    }
    
    /**
     * Создать новый InputBuilder.
     * 
     * @return новый builder
     */
    public static InputBuilder create() {
        return new InputBuilder();
    }
    
    /**
     * Создать новый InputBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый builder
     */
    public static InputBuilder create(String xpath) {
        return new InputBuilder(xpath);
    }
    
    @Override
    protected Class<? extends BaseElement> getDefaultElementClass() {
        return Input.class;
    }
}