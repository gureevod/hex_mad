package com.company.hex.ui.builder;

import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.elements.Select;

/**
 * Builder для создания Select элементов с fluent API.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * // Простой локатор
 * Select country = SelectBuilder.create("//select[@id='country']")
 *     .withName("Country Selector")
 *     .build();
 * 
 * // Составной параметризованный локатор
 * Select dynamicSelect = SelectBuilder.create()
 *     .withName("Dynamic Select")
 *     .locator()
 *         .base("//div[@class='form-section']")
 *         .append("//select[@data-field='{field}']")
 *         .build();
 * 
 * dynamicSelect.resolve("field", "country").selectByText("Russia");
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class SelectBuilder extends ElementBuilder<Select, SelectBuilder> {
    
    /**
     * Создать новый SelectBuilder.
     */
    protected SelectBuilder() {
        super();
    }
    
    /**
     * Создать новый SelectBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     */
    protected SelectBuilder(String xpath) {
        super();
        this.simpleLocator = xpath;
    }
    
    /**
     * Создать новый SelectBuilder.
     * 
     * @return новый builder
     */
    public static SelectBuilder create() {
        return new SelectBuilder();
    }
    
    /**
     * Создать новый SelectBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый builder
     */
    public static SelectBuilder create(String xpath) {
        return new SelectBuilder(xpath);
    }
    
    @Override
    protected Class<? extends BaseElement> getDefaultElementClass() {
        return Select.class;
    }
}