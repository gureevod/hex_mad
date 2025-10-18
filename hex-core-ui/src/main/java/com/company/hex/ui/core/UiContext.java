package com.company.hex.ui.core;

/**
 * Контекст UI элемента для логирования и Allure steps.
 * Хранит информацию о странице и компоненте, к которым принадлежит элемент.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class UiContext {
    
    private final String pageName;
    private final String componentName;
    
    /**
     * Создать контекст с именем страницы и компонента.
     * 
     * @param pageName имя страницы
     * @param componentName имя компонента (может быть null)
     */
    public UiContext(String pageName, String componentName) {
        this.pageName = pageName;
        this.componentName = componentName;
    }
    
    /**
     * Создать контекст только с именем страницы.
     * 
     * @param pageName имя страницы
     */
    public UiContext(String pageName) {
        this(pageName, null);
    }
    
    /**
     * Получить имя страницы.
     * 
     * @return имя страницы или "Unknown Page"
     */
    public String getPageName() {
        return pageName != null ? pageName : "Unknown Page";
    }
    
    /**
     * Получить имя компонента.
     * 
     * @return имя компонента или "Root"
     */
    public String getComponentName() {
        return componentName != null ? componentName : "Root";
    }
    
    /**
     * Получить полный контекст для логирования.
     * 
     * @return строка с контекстом
     */
    public String getFullContext() {
        return String.format("в странице '%s' компоненте '%s'", getPageName(), getComponentName());
    }
    
    @Override
    public String toString() {
        return String.format("UiContext[page='%s', component='%s']", getPageName(), getComponentName());
    }
}