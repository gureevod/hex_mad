package com.company.hex.ui.core;

import com.company.hex.ui.factory.FieldInitializer;
import com.company.hex.core.logging.HexLoggerFactory;
import org.slf4j.Logger;

/**
 * Базовый абстрактный класс для всех UI компонентов.
 * Компонент - это переиспользуемая группа элементов с собственным root локатором.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * public class HeaderComponent extends BaseComponent {
 *     
 *     @Element(name = "Home Button", xpath = ".//a[@title='home']")
 *     Button homeButton;
 *     
 *     @Element(name = "Profile Menu", xpath = ".//div[@class='profile-menu']")
 *     Button profileMenu;
 *     
 *     @Step("Navigate to home from '{this.componentName}'")
 *     public HomePage goHome() {
 *         homeButton.click();
 *         return new HomePage();
 *     }
 * }
 * 
 * // Использование в Page:
 * @Component(name = "Main Header", root = "//header[@id='main-header']")
 * HeaderComponent header;
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseComponent {
    
    protected final Logger logger;
    protected String componentName;
    protected String componentRoot;
    protected String pageName;
    
    /**
     * Конструктор базового компонента.
     * Компонент инициализируется через FieldInitializer при создании Page Object.
     */
    protected BaseComponent() {
        this.logger = HexLoggerFactory.getUiLogger(getClass());
        logger.trace("Создание компонента: {}", getClass().getSimpleName());
    }
    
    /**
     * Инициализировать компонент с контекстом.
     * Вызывается автоматически при инициализации полей Page Object.
     * 
     * @param componentName имя компонента
     * @param componentRoot корневой локатор компонента
     * @param pageName имя страницы
     */
    public void initialize(String componentName, String componentRoot, String pageName) {
        this.componentName = componentName;
        this.componentRoot = componentRoot;
        this.pageName = pageName;
        
        logger.debug("Инициализация компонента '{}' на странице '{}' с root: {}",
                componentName, pageName, componentRoot);
        
        // Инициализируем все поля компонента
        FieldInitializer.initializeFields(this, pageName, componentName, componentRoot);
        
        logger.info("Компонент '{}' успешно инициализирован", componentName);
    }
    
    /**
     * Получить имя компонента.
     * 
     * @return имя компонента
     */
    public String getComponentName() {
        return componentName != null ? componentName : getClass().getSimpleName();
    }
    
    /**
     * Получить корневой локатор компонента.
     * 
     * @return корневой локатор
     */
    public String getComponentRoot() {
        return componentRoot;
    }
    
    /**
     * Получить имя страницы, на которой находится компонент.
     * 
     * @return имя страницы
     */
    public String getPageName() {
        return pageName != null ? pageName : "Unknown Page";
    }
    
    /**
     * Проверить, инициализирован ли компонент.
     * 
     * @return true если компонент инициализирован
     */
    public boolean isInitialized() {
        return componentName != null && componentRoot != null;
    }
    
    @Override
    public String toString() {
        return String.format("%s[name='%s', root='%s', page='%s']",
                getClass().getSimpleName(), getComponentName(), componentRoot, getPageName());
    }
}