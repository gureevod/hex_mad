package com.company.hex.ui.factory;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.core.UiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class ElementFactory {

    private static final Logger logger = LoggerFactory.getLogger(ElementFactory.class);

    // Используем ConcurrentHashMap для потокобезопасности при параллельном запуске тестов
    private static final Map<Class<? extends BaseElement>, BiFunction<String, SelenideElement, ? extends BaseElement>>
            ELEMENT_CREATORS = new ConcurrentHashMap<>();

    private ElementFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Создает элемент. Если тип встречается впервые, регистрирует его автоматически.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseElement> T create(
            Class<T> elementType,
            String name,
            SelenideElement selenideElement) {

        // Если создателя нет, он будет создан через reflection и сохранен в мапу.
        BiFunction<String, SelenideElement, ? extends BaseElement> creator =
                ELEMENT_CREATORS.computeIfAbsent(elementType, ElementFactory::createConstructorFunction);

        try {
            return (T) creator.apply(name, selenideElement);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании элемента '" + name + "' типа " + elementType.getSimpleName(), e);
        }
    }

    public static <T extends BaseElement> T create(
            Class<T> elementType,
            String name,
            SelenideElement selenideElement,
            UiContext context) {
        T element = create(elementType, name, selenideElement);
        if (context != null) {
            element.setPageName(context.getPageName());
            element.setComponentName(context.getComponentName());
        }
        return element;
    }

    /**
     * Создает функцию-конструктор через Reflection.
     * Вызывается один раз для каждого типа элемента.
     */
    private static <T extends BaseElement> BiFunction<String, SelenideElement, T> createConstructorFunction(Class<T> clazz) {
        try {
            // Ищем конструктор (String, SelenideElement)
            Constructor<T> constructor = clazz.getConstructor(String.class, SelenideElement.class);

            logger.debug("JIT регистрация для типа: {}", clazz.getName());

            return (name, element) -> {
                try {
                    return constructor.newInstance(name, element);
                } catch (Exception e) {
                    throw new RuntimeException("Не удалось инстанцировать " + clazz.getSimpleName(), e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    String.format("Класс %s должен иметь публичный конструктор (String, SelenideElement)", clazz.getName()), e);
        }
    }
}