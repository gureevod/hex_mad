package com.company.hex.ui.builder;

import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.elements.Checkbox;

/**
 * Builder для создания Checkbox элементов с fluent API.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * // Простой локатор
 * Checkbox terms = CheckboxBuilder.create("//input[@type='checkbox'][@id='terms']")
 *     .withName("Accept Terms")
 *     .build();
 * 
 * // Составной параметризованный локатор
 * Checkbox dynamicCheckbox = CheckboxBuilder.create()
 *     .withName("Dynamic Checkbox")
 *     .locator()
 *         .base("//div[@class='settings']")
 *         .append("//input[@type='checkbox'][@data-setting='{setting}']")
 *         .build();
 * 
 * dynamicCheckbox.resolve("setting", "notifications").check();
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class CheckboxBuilder extends ElementBuilder<Checkbox, CheckboxBuilder> {
    
    /**
     * Создать новый CheckboxBuilder.
     */
    protected CheckboxBuilder() {
        super();
    }
    
    /**
     * Создать новый CheckboxBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     */
    protected CheckboxBuilder(String xpath) {
        super();
        this.simpleLocator = xpath;
    }
    
    /**
     * Создать новый CheckboxBuilder.
     * 
     * @return новый builder
     */
    public static CheckboxBuilder create() {
        return new CheckboxBuilder();
    }
    
    /**
     * Создать новый CheckboxBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый builder
     */
    public static CheckboxBuilder create(String xpath) {
        return new CheckboxBuilder(xpath);
    }
    
    @Override
    protected Class<? extends BaseElement> getDefaultElementClass() {
        return Checkbox.class;
    }
}