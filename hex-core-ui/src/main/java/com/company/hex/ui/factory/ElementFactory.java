package com.company.hex.ui.factory;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.elements.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Фабрика для создания UI элементов.
 * Предоставляет централизованный механизм создания элементов с возможностью регистрации кастомных типов.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ElementFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ElementFactory.class);
    
    /**
     * Реестр создателей элементов.
     * Ключ - класс элемента, значение - функция создания.
     */
    private static final Map<Class<? extends BaseElement>, BiFunction<String, SelenideElement, ? extends BaseElement>> 
            ELEMENT_CREATORS = new HashMap<>();
    
    static {
        // Регистрация базовых типов элементов
        register(Input.class, Input::new);
        register(Button.class, Button::new);
        register(Checkbox.class, Checkbox::new);
        register(Select.class, Select::new);
        register(TextElement.class, TextElement::new);
        
        logger.debug("ElementFactory инициализирована с {} базовыми типами элементов", ELEMENT_CREATORS.size());
    }
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private ElementFactory() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }
    
    /**
     * Создать элемент указанного типа.
     * 
     * @param <T> тип элемента
     * @param elementType класс элемента
     * @param name имя элемента
     * @param selenideElement Selenide элемент
     * @return созданный элемент
     * @throws IllegalArgumentException если тип элемента не зарегистрирован
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseElement> T create(
            Class<T> elementType,
            String name,
            SelenideElement selenideElement) {
        
        if (elementType == null) {
            throw new IllegalArgumentException("Тип элемента не может быть null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя элемента не может быть null или пустым");
        }
        if (selenideElement == null) {
            throw new IllegalArgumentException("SelenideElement не может быть null");
        }
        
        BiFunction<String, SelenideElement, ? extends BaseElement> creator = ELEMENT_CREATORS.get(elementType);
        
        if (creator == null) {
            String errorMessage = String.format(
                    "Неизвестный тип элемента: %s. Зарегистрируйте его через ElementFactory.register()",
                    elementType.getName());
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        logger.debug("Создание элемента типа {} с именем '{}'", elementType.getSimpleName(), name);
        T element = (T) creator.apply(name, selenideElement);
        logger.trace("Элемент '{}' типа {} успешно создан", name, elementType.getSimpleName());
        
        return element;
    }
    
    /**
     * Зарегистрировать кастомный тип элемента.
     * Позволяет расширять фабрику пользовательскими типами элементов.
     * 
     * @param <T> тип элемента
     * @param elementType класс элемента
     * @param creator функция создания элемента
     */
    public static <T extends BaseElement> void register(
            Class<T> elementType,
            BiFunction<String, SelenideElement, T> creator) {
        
        if (elementType == null) {
            throw new IllegalArgumentException("Тип элемента не может быть null");
        }
        if (creator == null) {
            throw new IllegalArgumentException("Создатель элемента не может быть null");
        }
        
        if (ELEMENT_CREATORS.containsKey(elementType)) {
            logger.warn("Перезапись существующего создателя для типа: {}", elementType.getName());
        }
        
        ELEMENT_CREATORS.put(elementType, creator);
        logger.info("Зарегистрирован тип элемента: {}", elementType.getName());
    }
    
    /**
     * Проверить, зарегистрирован ли тип элемента.
     * 
     * @param elementType класс элемента
     * @return true если тип зарегистрирован
     */
    public static boolean isRegistered(Class<? extends BaseElement> elementType) {
        return ELEMENT_CREATORS.containsKey(elementType);
    }
    
    /**
     * Получить количество зарегистрированных типов элементов.
     * 
     * @return количество типов
     */
    public static int getRegisteredTypesCount() {
        return ELEMENT_CREATORS.size();
    }
    
    /**
     * Удалить регистрацию типа элемента.
     * Используйте с осторожностью - может нарушить работу существующих Page Objects.
     * 
     * @param elementType класс элемента
     * @return true если тип был удален
     */
    public static boolean unregister(Class<? extends BaseElement> elementType) {
        if (elementType == null) {
            return false;
        }
        
        boolean removed = ELEMENT_CREATORS.remove(elementType) != null;
        if (removed) {
            logger.warn("Удалена регистрация типа элемента: {}", elementType.getName());
        }
        return removed;
    }
    
    /**
     * Очистить все регистрации (кроме базовых типов).
     * Используется в основном для тестирования.
     */
    public static void clearCustomRegistrations() {
        logger.warn("Очистка всех кастомных регистраций элементов");
        ELEMENT_CREATORS.clear();
        
        // Восстанавливаем базовые типы
        register(Input.class, Input::new);
        register(Button.class, Button::new);
        register(Checkbox.class, Checkbox::new);
        register(Select.class, Select::new);
        register(TextElement.class, TextElement::new);
        
        logger.info("Базовые типы элементов восстановлены");
    }
}