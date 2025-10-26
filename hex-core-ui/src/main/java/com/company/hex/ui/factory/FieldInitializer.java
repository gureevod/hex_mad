package com.company.hex.ui.factory;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.annotations.Component;
import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Elements;
import com.company.hex.ui.collections.ElementList;
import com.company.hex.ui.core.BaseComponent;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.core.UiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$$x;

/**
 * Утилитный класс для инициализации полей с аннотациями @Element и @Elements.
 * Создает lazy proxy для элементов, которые инициализируются при первом обращении.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class FieldInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(FieldInitializer.class);
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private FieldInitializer() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }
    
    /**
     * Инициализировать все поля с аннотациями в объекте (Page или Component).
     * 
     * @param target объект для инициализации
     * @param pageName имя страницы для контекста
     * @param componentName имя компонента для контекста (может быть null)
     * @param componentRoot корневой локатор компонента (может быть null)
     */
    public static void initializeFields(
            Object target,
            String pageName,
            String componentName,
            String componentRoot) {
        
        if (target == null) {
            throw new IllegalArgumentException("Target объект не может быть null");
        }
        
        Class<?> clazz = target.getClass();
        logger.debug("Инициализация полей для класса: {}", clazz.getName());
        
        // Получаем все поля, включая унаследованные
        List<Field> allFields = getAllFields(clazz);
        
        int initializedCount = 0;
        for (Field field : allFields) {
            field.setAccessible(true);
            
            if (field.isAnnotationPresent(Element.class)) {
                initializeElement(target, field, pageName, componentName, componentRoot);
                initializedCount++;
            } else if (field.isAnnotationPresent(Elements.class)) {
                initializeElementList(target, field, pageName, componentName, componentRoot);
                initializedCount++;
            } else if (field.isAnnotationPresent(Component.class)) {
                initializeComponent(target, field, pageName);
                initializedCount++;
            }
        }
        
        logger.info("Инициализировано {} полей в классе {}", initializedCount, clazz.getSimpleName());
    }
    
    /**
     * Получить все поля класса, включая унаследованные.
     * 
     * @param clazz класс
     * @return список всех полей
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        
        while (clazz != null && clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                fields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        
        return fields;
    }
    
    /**
     * Инициализировать поле с аннотацией @Element.
     *
     * @param target объект
     * @param field поле
     * @param pageName имя страницы
     * @param componentName имя компонента
     * @param componentRoot корневой локатор компонента
     */
    @SuppressWarnings("unchecked")
    private static void initializeElement(
            Object target,
            Field field,
            String pageName,
            String componentName,
            String componentRoot) {
        
        Element annotation = field.getAnnotation(Element.class);
        String name = annotation.name();
        
        // Валидация
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' в классе '%s' имеет пустое имя в аннотации @Element",
                            field.getName(), target.getClass().getName()));
        }
        
        String xpath = annotation.xpath();
        String css = annotation.css();
        
        if ((xpath == null || xpath.trim().isEmpty()) && (css == null || css.trim().isEmpty())) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' в классе '%s' должно иметь xpath или css локатор",
                            field.getName(), target.getClass().getName()));
        }
        
        // Определяем тип локатора и сам локатор
        boolean isXpath = xpath != null && !xpath.trim().isEmpty();
        String locator = isXpath ? xpath : css;
        
        // Получаем тип элемента
        Class<? extends BaseElement> elementType = (Class<? extends BaseElement>) field.getType();
        
        if (!BaseElement.class.isAssignableFrom(elementType)) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' в классе '%s' должно быть типа BaseElement или его наследника",
                            field.getName(), target.getClass().getName()));
        }
        
        // Создаем элемент напрямую (Selenide уже обеспечивает lazy resolution)
        BaseElement element = createElementDirectly(
                elementType,
                name,
                locator,
                isXpath,
                componentRoot,
                pageName,
                componentName);
        
        // Устанавливаем элемент в поле
        setField(field, target, element);
        
        logger.trace("Поле '{}' инициализировано как {} с именем '{}'",
                field.getName(), elementType.getSimpleName(), name);
    }
    
    /**
     * Инициализировать поле с аннотацией @Elements.
     *
     * @param target объект
     * @param field поле
     * @param pageName имя страницы
     * @param componentName имя компонента
     * @param componentRoot корневой локатор компонента
     */
    @SuppressWarnings("unchecked")
    private static void initializeElementList(
            Object target,
            Field field,
            String pageName,
            String componentName,
            String componentRoot) {
        
        Elements annotation = field.getAnnotation(Elements.class);
        String name = annotation.name();
        
        // Валидация
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' в классе '%s' имеет пустое имя в аннотации @Elements",
                            field.getName(), target.getClass().getName()));
        }
        
        String xpath = annotation.xpath();
        String css = annotation.css();
        
        if ((xpath == null || xpath.trim().isEmpty()) && (css == null || css.trim().isEmpty())) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' в классе '%s' должно иметь xpath или css локатор",
                            field.getName(), target.getClass().getName()));
        }
        
        // Определяем тип локатора и сам локатор
        boolean isXpath = xpath != null && !xpath.trim().isEmpty();
        String locator = isXpath ? xpath : css;
        
        // Получаем тип элемента из generic параметра
        Class<? extends BaseElement> elementType = getElementTypeFromList(field);
        
        // Создаем контекст
        UiContext context = new UiContext(pageName, componentName);
        
        // Создаем resolver для коллекции
        Supplier<ElementsCollection> resolver = createCollectionResolver(locator, isXpath, componentRoot);
        
        // Создаем ElementList
        ElementList<?> elementList = new ElementList<>(name, resolver, elementType, context);
        
        // Устанавливаем в поле
        setField(field, target, elementList);
        
        logger.trace("Поле '{}' инициализировано как ElementList<{}> с именем '{}'",
                field.getName(), elementType.getSimpleName(), name);
    }
    
    /**
     * Создать resolver для коллекции элементов.
     *
     * @param locator локатор
     * @param isXpath true если xpath
     * @param componentRoot корневой локатор компонента
     * @return Supplier для ElementsCollection
     */
    private static Supplier<ElementsCollection> createCollectionResolver(
            String locator,
            boolean isXpath,
            String componentRoot) {
        
        return () -> {
            if (componentRoot != null && !componentRoot.trim().isEmpty()) {
                // Если есть root компонента, комбинируем локаторы
                String fullLocator;
                if (isXpath) {
                    // Для xpath комбинируем: root + относительный локатор
                    fullLocator = componentRoot + locator;
                    return $$x(fullLocator);
                } else {
                    // Для CSS используем пространство для вложенности
                    fullLocator = componentRoot + " " + locator;
                    return $$(fullLocator);
                }
            } else {
                // Иначе ищем от корня документа
                if (isXpath) {
                    return $$x(locator);
                } else {
                    return $$(locator);
                }
            }
        };
    }
    
    /**
     * Инициализировать поле с аннотацией @Component.
     *
     * @param target объект
     * @param field поле
     * @param pageName имя страницы
     */
    private static void initializeComponent(
            Object target,
            Field field,
            String pageName) {
        
        Component annotation = field.getAnnotation(Component.class);
        String componentName = annotation.name();
        String componentRoot = annotation.root();
        
        // Валидация
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' в классе '%s' имеет пустое имя в аннотации @Component",
                            field.getName(), target.getClass().getName()));
        }
        
        if (componentRoot == null || componentRoot.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' в классе '%s' должно иметь root локатор в аннотации @Component",
                            field.getName(), target.getClass().getName()));
        }
        
        // Проверяем что поле имеет тип BaseComponent
        Class<?> componentType = field.getType();
        if (!BaseComponent.class.isAssignableFrom(componentType)) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' в классе '%s' должно быть типа BaseComponent или его наследника",
                            field.getName(), target.getClass().getName()));
        }
        
        try {
            // Создаем экземпляр компонента
            BaseComponent component = (BaseComponent) componentType.getDeclaredConstructor().newInstance();
            
            // Инициализируем компонент с контекстом
            component.initialize(componentName, componentRoot, pageName);
            
            // Устанавливаем компонент в поле
            setField(field, target, component);
            
            logger.debug("Компонент '{}' типа {} успешно инициализирован в поле '{}'",
                    componentName, componentType.getSimpleName(), field.getName());
            
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Ошибка при создании компонента '%s' типа %s: %s",
                    componentName, componentType.getName(), e.getMessage());
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
    
    /**
     * Создать элемент напрямую без использования proxy.
     * Selenide уже обеспечивает lazy resolution элементов.
     *
     * @param elementType тип элемента
     * @param name имя элемента
     * @param locator локатор
     * @param isXpath true если xpath
     * @param componentRoot корневой локатор компонента
     * @param pageName имя страницы
     * @param componentName имя компонента
     * @return созданный элемент
     */
    private static <T extends BaseElement> T createElementDirectly(
            Class<T> elementType,
            String name,
            String locator,
            boolean isXpath,
            String componentRoot,
            String pageName,
            String componentName) {
        
        try {
            // Строим полный локатор
            String fullLocator = buildFullLocator(locator, isXpath, componentRoot);
            
            // Находим SelenideElement (lazy по умолчанию в Selenide)
            SelenideElement selenideElement;
            if (isXpath) {
                selenideElement = $(org.openqa.selenium.By.xpath(fullLocator));
            } else {
                selenideElement = $(org.openqa.selenium.By.cssSelector(fullLocator));
            }
            
            // Создаем элемент через фабрику
            T element = ElementFactory.create(elementType, name, selenideElement);
            
            // Устанавливаем контекст
            if (pageName != null) {
                element.setPageName(pageName);
            }
            if (componentName != null) {
                element.setComponentName(componentName);
            }
            
            logger.debug("Элемент '{}' типа {} успешно создан", name, elementType.getSimpleName());
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
     * Построить полный локатор с учетом componentRoot.
     *
     * @param locator базовый локатор
     * @param isXpath true если xpath
     * @param componentRoot корневой локатор компонента
     * @return полный локатор
     */
    private static String buildFullLocator(String locator, boolean isXpath, String componentRoot) {
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
     * Установить значение поля.
     * 
     * @param field поле
     * @param target объект
     * @param value значение
     */
    private static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            String errorMessage = String.format(
                    "Не удалось установить значение поля '%s' в классе '%s'",
                    field.getName(), target.getClass().getName());
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
    
    /**
     * Получить тип элемента из generic типа ElementList.
     * 
     * @param field поле
     * @return тип элемента
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends BaseElement> getElementTypeFromList(Field field) {
        Type genericType = field.getGenericType();
        
        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' должно иметь параметризованный тип ElementList<T>",
                            field.getName()));
        }
        
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        
        if (typeArguments.length == 0) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' должно иметь тип параметра в ElementList<T>",
                            field.getName()));
        }
        
        return (Class<? extends BaseElement>) typeArguments[0];
    }
}