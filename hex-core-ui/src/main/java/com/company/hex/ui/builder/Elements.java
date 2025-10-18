package com.company.hex.ui.builder;

/**
 * Утилитный класс со статическими методами для создания элементов через Builder API.
 * Предоставляет удобный fluent интерфейс для построения UI элементов.
 * 
 * <p>Рекомендуется использовать статический импорт:</p>
 * <pre>
 * {@code
 * import static com.company.hex.ui.builder.Elements.*;
 * 
 * public class MyPage extends BasePage {
 *     
 *     // Простые локаторы
 *     private final Input email = input("//input[@type='email']")
 *         .withName("Email Field");
 *     
 *     private final Button submit = button("//button[@type='submit']")
 *         .withName("Submit Button");
 *     
 *     // Составные локаторы
 *     private final Input dynamicField = input()
 *         .withName("Dynamic Field")
 *         .locator()
 *             .base("//div[@class='form']")
 *             .append("//input[@data-field='{field}']")
 *             .build();
 * }
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public final class Elements {
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private Elements() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }
    
    /**
     * Создать InputBuilder без локатора.
     * Локатор должен быть установлен через locator().
     * 
     * @return новый InputBuilder
     */
    public static InputBuilder input() {
        return InputBuilder.create();
    }
    
    /**
     * Создать InputBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый InputBuilder
     */
    public static InputBuilder input(String xpath) {
        return InputBuilder.create(xpath);
    }
    
    /**
     * Создать ButtonBuilder без локатора.
     * Локатор должен быть установлен через locator().
     * 
     * @return новый ButtonBuilder
     */
    public static ButtonBuilder button() {
        return ButtonBuilder.create();
    }
    
    /**
     * Создать ButtonBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый ButtonBuilder
     */
    public static ButtonBuilder button(String xpath) {
        return ButtonBuilder.create(xpath);
    }
    
    /**
     * Создать CheckboxBuilder без локатора.
     * Локатор должен быть установлен через locator().
     * 
     * @return новый CheckboxBuilder
     */
    public static CheckboxBuilder checkbox() {
        return CheckboxBuilder.create();
    }
    
    /**
     * Создать CheckboxBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый CheckboxBuilder
     */
    public static CheckboxBuilder checkbox(String xpath) {
        return CheckboxBuilder.create(xpath);
    }
    
    /**
     * Создать SelectBuilder без локатора.
     * Локатор должен быть установлен через locator().
     * 
     * @return новый SelectBuilder
     */
    public static SelectBuilder select() {
        return SelectBuilder.create();
    }
    
    /**
     * Создать SelectBuilder с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый SelectBuilder
     */
    public static SelectBuilder select(String xpath) {
        return SelectBuilder.create(xpath);
    }
    
    /**
     * Создать ElementBuilder для кастомного типа элемента.
     * Используется для создания специализированных элементов.
     * 
     * @return новый ElementBuilder
     */
    public static ElementBuilder<?, ?> element() {
        return new GenericElementBuilder();
    }
    
    /**
     * Создать ElementBuilder для кастомного типа элемента с простым локатором.
     * 
     * @param xpath XPath локатор
     * @return новый ElementBuilder
     */
    public static ElementBuilder<?, ?> element(String xpath) {
        return new GenericElementBuilder(xpath);
    }
    
    /**
     * Внутренний класс для создания generic элементов.
     */
    private static class GenericElementBuilder extends ElementBuilder<com.company.hex.ui.core.BaseElement, GenericElementBuilder> {
        
        protected GenericElementBuilder() {
            super();
        }
        
        protected GenericElementBuilder(String xpath) {
            super();
            this.simpleLocator = xpath;
        }
        
        @Override
        protected Class<? extends com.company.hex.ui.core.BaseElement> getDefaultElementClass() {
            if (customImplementation == null) {
                throw new IllegalStateException(
                    "Для generic элемента необходимо указать класс через withCustomImplementation() или as()");
            }
            return customImplementation;
        }
    }
}