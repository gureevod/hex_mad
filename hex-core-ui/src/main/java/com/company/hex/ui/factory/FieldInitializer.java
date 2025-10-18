package com.company.hex.ui.factory;

import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Elements;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.proxy.LazyElementHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
        
        // Создаем lazy proxy
        Object proxy = createLazyProxy(
                elementType,
                name,
                locator,
                isXpath,
                componentRoot,
                pageName,
                componentName);
        
        // Устанавливаем proxy в поле
        setField(field, target, proxy);
        
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
    private static void initializeElementList(
            Object target,
            Field field,
            String pageName,
            String componentName,
            String componentRoot) {
        
        Elements annotation = field.getAnnotation(Elements.class);
        
        // TODO: Реализация ElementList будет добавлена позже
        logger.warn("Инициализация @Elements для поля '{}' пока не реализована", field.getName());
    }
    
    /**
     * Создать lazy proxy для элемента.
     * 
     * @param elementType тип элемента
     * @param name имя элемента
     * @param locator локатор
     * @param isXpath true если xpath
     * @param componentRoot корневой локатор компонента
     * @param pageName имя страницы
     * @param componentName имя компонента
     * @return proxy объект
     */
    @SuppressWarnings("unchecked")
    private static <T extends BaseElement> T createLazyProxy(
            Class<T> elementType,
            String name,
            String locator,
            boolean isXpath,
            String componentRoot,
            String pageName,
            String componentName) {
        
        LazyElementHandler handler = new LazyElementHandler(
                elementType,
                name,
                locator,
                isXpath,
                componentRoot,
                pageName,
                componentName);
        
        return (T) Proxy.newProxyInstance(
                elementType.getClassLoader(),
                new Class<?>[]{elementType},
                handler);
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