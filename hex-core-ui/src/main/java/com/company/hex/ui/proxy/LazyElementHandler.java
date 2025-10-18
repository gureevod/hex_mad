package com.company.hex.ui.proxy;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.factory.ElementFactory;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Обработчик для lazy инициализации UI элементов через Java Proxy.
 * Элемент создается только при первом обращении к нему, что позволяет:
 * - Избежать ошибок при создании Page Objects с динамическими элементами
 * - Минимизировать накладные расходы при инициализации
 * - Обеспечить совместимость с подходом Selenide
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class LazyElementHandler implements InvocationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(LazyElementHandler.class);
    
    private final Class<? extends BaseElement> elementType;
    private final String name;
    private final String locator;
    private final boolean isXpath;
    private final String componentRoot;
    private final String pageName;
    private final String componentName;
    
    // Кэшированный элемент (создается при первом обращении)
    private volatile BaseElement cachedElement;
    
    /**
     * Конструктор для создания обработчика lazy элемента.
     * 
     * @param elementType тип элемента
     * @param name имя элемента
     * @param locator локатор элемента (xpath или css)
     * @param isXpath true если локатор xpath, false если css
     * @param componentRoot корневой локатор компонента (может быть null)
     * @param pageName имя страницы для контекста
     * @param componentName имя компонента для контекста
     */
    public LazyElementHandler(
            Class<? extends BaseElement> elementType,
            String name,
            String locator,
            boolean isXpath,
            String componentRoot,
            String pageName,
            String componentName) {
        
        this.elementType = elementType;
        this.name = name;
        this.locator = locator;
        this.isXpath = isXpath;
        this.componentRoot = componentRoot;
        this.pageName = pageName;
        this.componentName = componentName;
        
        logger.trace("Создан LazyElementHandler для '{}' типа {}", name, elementType.getSimpleName());
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Инициализируем элемент при первом обращении
        if (cachedElement == null) {
            synchronized (this) {
                if (cachedElement == null) {
                    logger.debug("Lazy инициализация элемента '{}' типа {}", name, elementType.getSimpleName());
                    cachedElement = createRealElement();
                }
            }
        }
        
        // Делегируем вызов реальному элементу
        try {
            return method.invoke(cachedElement, args);
        } catch (Exception e) {
            logger.error("Ошибка при вызове метода '{}' на элементе '{}': {}", 
                    method.getName(), name, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Создать реальный экземпляр элемента.
     * 
     * @return созданный элемент
     */
    private BaseElement createRealElement() {
        try {
            // Находим SelenideElement
            SelenideElement selenideElement = findSelenideElement();
            
            // Создаем элемент через фабрику
            BaseElement element = ElementFactory.create(elementType, name, selenideElement);
            
            // Устанавливаем контекст
            if (pageName != null) {
                element.setPageName(pageName);
            }
            if (componentName != null) {
                element.setComponentName(componentName);
            }
            
            logger.debug("Элемент '{}' типа {} успешно инициализирован", name, elementType.getSimpleName());
            return element;
            
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Ошибка при создании элемента '%s' типа %s с локатором '%s'",
                    name, elementType.getSimpleName(), locator);
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
    
    /**
     * Найти SelenideElement по локатору.
     * 
     * @return найденный SelenideElement
     */
    private SelenideElement findSelenideElement() {
        String fullLocator = buildFullLocator();
        
        logger.trace("Поиск элемента '{}' по локатору: {}", name, fullLocator);
        
        if (isXpath) {
            return Selenide.$(By.xpath(fullLocator));
        } else {
            return Selenide.$(By.cssSelector(fullLocator));
        }
    }
    
    /**
     * Построить полный локатор с учетом componentRoot.
     * 
     * @return полный локатор
     */
    private String buildFullLocator() {
        if (componentRoot == null || componentRoot.trim().isEmpty()) {
            return locator;
        }
        
        // Если элемент внутри компонента, комбинируем локаторы
        if (isXpath) {
            // Для xpath: componentRoot + относительный локатор
            if (locator.startsWith(".//")) {
                // Относительный локатор - добавляем к root
                return componentRoot + "/" + locator.substring(3);
            } else if (locator.startsWith("//")) {
                // Абсолютный локатор - используем как есть
                return locator;
            } else {
                // Локатор без префикса - делаем относительным
                return componentRoot + "//" + locator;
            }
        } else {
            // Для CSS: используем вложенность через пробел
            return componentRoot + " " + locator;
        }
    }
    
    /**
     * Получить имя элемента.
     * 
     * @return имя элемента
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получить тип элемента.
     * 
     * @return класс элемента
     */
    public Class<? extends BaseElement> getElementType() {
        return elementType;
    }
    
    /**
     * Проверить, инициализирован ли элемент.
     * 
     * @return true если элемент уже создан
     */
    public boolean isInitialized() {
        return cachedElement != null;
    }
}