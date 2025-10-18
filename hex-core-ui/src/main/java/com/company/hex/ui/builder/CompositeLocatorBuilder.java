package com.company.hex.ui.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder для создания составных локаторов с поддержкой параметризации.
 * Позволяет строить сложные XPath локаторы из нескольких частей с плейсхолдерами.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * CompositeLocatorBuilder locator = new CompositeLocatorBuilder()
 *     .base("//div[@class='container']")
 *     .append("//section[@id='{section}']")
 *     .append("//input[@data-field='{field}']");
 * 
 * String resolved = locator.resolve("section", "profile", "field", "email");
 * // Результат: //div[@class='container']//section[@id='profile']//input[@data-field='email']
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class CompositeLocatorBuilder {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    private final List<String> locatorParts;
    private String baseLocator;
    
    /**
     * Создать новый builder для составного локатора.
     */
    public CompositeLocatorBuilder() {
        this.locatorParts = new ArrayList<>();
    }
    
    /**
     * Установить базовую часть локатора.
     * 
     * @param xpath базовый XPath локатор
     * @return this для fluent API
     */
    public CompositeLocatorBuilder base(String xpath) {
        if (xpath == null || xpath.trim().isEmpty()) {
            throw new IllegalArgumentException("Базовый локатор не может быть null или пустым");
        }
        this.baseLocator = xpath.trim();
        return this;
    }
    
    /**
     * Добавить часть локатора.
     * 
     * @param xpath XPath локатор для добавления
     * @return this для fluent API
     */
    public CompositeLocatorBuilder append(String xpath) {
        if (xpath == null || xpath.trim().isEmpty()) {
            throw new IllegalArgumentException("Локатор для добавления не может быть null или пустым");
        }
        locatorParts.add(xpath.trim());
        return this;
    }
    
    /**
     * Построить финальный локатор без параметров.
     * 
     * @return составной локатор
     * @throws IllegalStateException если базовый локатор не установлен
     */
    public String build() {
        if (baseLocator == null) {
            throw new IllegalStateException("Базовый локатор должен быть установлен через base()");
        }
        
        if (locatorParts.isEmpty()) {
            return baseLocator;
        }
        
        StringBuilder result = new StringBuilder(baseLocator);
        for (String part : locatorParts) {
            result.append(part);
        }
        
        return result.toString();
    }
    
    /**
     * Построить локатор с подстановкой параметров.
     * Параметры передаются парами: имя, значение, имя, значение, ...
     * 
     * @param params параметры для подстановки (пары ключ-значение)
     * @return локатор с подставленными параметрами
     * @throws IllegalArgumentException если количество параметров нечетное
     */
    public String resolve(String... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException(
                "Параметры должны передаваться парами: имя, значение, имя, значение, ...");
        }
        
        // Создаем map параметров
        Map<String, String> paramMap = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            paramMap.put(params[i], params[i + 1]);
        }
        
        return resolve(paramMap);
    }
    
    /**
     * Построить локатор с подстановкой параметров из Map.
     * 
     * @param params map параметров для подстановки
     * @return локатор с подставленными параметрами
     */
    public String resolve(Map<String, String> params) {
        String locator = build();
        
        if (params == null || params.isEmpty()) {
            return locator;
        }
        
        // Заменяем все плейсхолдеры
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(locator);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String value = params.get(placeholder);
            
            if (value == null) {
                throw new IllegalArgumentException(
                    String.format("Не найдено значение для параметра '{%s}' в локаторе: %s", 
                        placeholder, locator));
            }
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Проверить, содержит ли локатор параметры.
     * 
     * @return true если локатор содержит плейсхолдеры
     */
    public boolean hasParameters() {
        String locator = build();
        return PLACEHOLDER_PATTERN.matcher(locator).find();
    }
    
    /**
     * Получить список всех параметров в локаторе.
     * 
     * @return список имен параметров
     */
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        String locator = build();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(locator);
        
        while (matcher.find()) {
            String param = matcher.group(1);
            if (!parameters.contains(param)) {
                parameters.add(param);
            }
        }
        
        return parameters;
    }
    
    /**
     * Создать копию builder'а.
     * 
     * @return новый builder с теми же настройками
     */
    public CompositeLocatorBuilder copy() {
        CompositeLocatorBuilder copy = new CompositeLocatorBuilder();
        if (this.baseLocator != null) {
            copy.base(this.baseLocator);
        }
        for (String part : this.locatorParts) {
            copy.append(part);
        }
        return copy;
    }
    
    @Override
    public String toString() {
        return build();
    }
}