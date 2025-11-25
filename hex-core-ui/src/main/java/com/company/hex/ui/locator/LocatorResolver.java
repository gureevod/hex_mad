package com.company.hex.ui.locator;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилита для работы с параметризованными локаторами.
 * Поддерживает несколько форматов плейсхолдеров:
 * - Именованные параметры: {paramName}
 * - Позиционные параметры: {0}, {1}, {2}
 * - Printf-style: %s, %d
 * 
 * <p>Примеры:</p>
 * <pre>
 * {@code
 * // Именованные параметры
 * String locator = "//div[@id='{userId}'][@status='{status}']";
 * Map<String, String> params = Map.of("userId", "123", "status", "active");
 * String resolved = LocatorResolver.resolve(locator, params);
 * // Результат: //div[@id='123'][@status='active']
 * 
 * // Позиционные параметры
 * String locator2 = "//tr[@data-id='{0}']//td[{1}]";
 * String resolved2 = LocatorResolver.resolve(locator2, "user-123", "3");
 * // Результат: //tr[@data-id='user-123']//td[3]
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public final class LocatorResolver {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    private static final Pattern PRINTF_PATTERN = Pattern.compile("%[sdf]");
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private LocatorResolver() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }
    
    /**
     * Подставить именованные параметры в локатор.
     * Формат плейсхолдеров: {paramName}
     * 
     * @param locator шаблон локатора с плейсхолдерами
     * @param params map параметров для подстановки
     * @return локатор с подставленными значениями
     * @throws IllegalArgumentException если параметр не найден или локатор null
     */
    public static String resolve(String locator, Map<String, String> params) {
        if (locator == null) {
            throw new IllegalArgumentException("Локатор не может быть null");
        }
        
        if (params == null || params.isEmpty()) {
            return locator;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(locator);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            
            // Проверяем, не является ли это позиционным параметром (число)
            if (paramName.matches("\\d+")) {
                // Позиционный параметр, пропускаем в этом методе
                continue;
            }
            
            String value = params.get(paramName);
            if (value == null) {
                throw new IllegalArgumentException(
                    String.format("Отсутствует значение для параметра '{%s}' в локаторе: %s", 
                        paramName, locator));
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Подставить позиционные параметры в локатор.
     * Поддерживает два формата:
     * - {0}, {1}, {2} - индексированные плейсхолдеры
     * - %s, %d, %f - printf-style плейсхолдеры
     * 
     * @param locator шаблон локатора
     * @param params параметры по порядку
     * @return локатор с подставленными значениями
     * @throws IllegalArgumentException если локатор null
     */
    public static String resolve(String locator, String... params) {
        if (locator == null) {
            throw new IllegalArgumentException("Локатор не может быть null");
        }
        
        if (params == null || params.length == 0) {
            return locator;
        }
        
        // Сначала пробуем printf-style (%s, %d, %f)
        if (PRINTF_PATTERN.matcher(locator).find()) {
            try {
                return String.format(locator, (Object[]) params);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    String.format("Ошибка при подстановке параметров в локатор '%s': %s", 
                        locator, e.getMessage()), e);
            }
        }
        
        // Затем пробуем {0}, {1}, {2} style
        String result = locator;
        for (int i = 0; i < params.length; i++) {
            String placeholder = "{" + i + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, params[i]);
            }
        }
        
        return result;
    }
    
    /**
     * Проверить, содержит ли локатор плейсхолдеры.
     * 
     * @param locator локатор для проверки
     * @return true если локатор содержит плейсхолдеры любого формата
     */
    public static boolean hasPlaceholders(String locator) {
        if (locator == null) {
            return false;
        }
        return PLACEHOLDER_PATTERN.matcher(locator).find() 
            || PRINTF_PATTERN.matcher(locator).find();
    }
    
    /**
     * Получить список всех именованных параметров в локаторе.
     * Не включает позиционные параметры ({0}, {1}, ...).
     * 
     * @param locator локатор для анализа
     * @return список имен параметров (без дубликатов)
     */
    public static java.util.List<String> getParameterNames(String locator) {
        java.util.List<String> parameters = new java.util.ArrayList<>();
        
        if (locator == null) {
            return parameters;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(locator);
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            // Пропускаем позиционные параметры (числа)
            if (!paramName.matches("\\d+") && !parameters.contains(paramName)) {
                parameters.add(paramName);
            }
        }
        
        return parameters;
    }
    
    /**
     * Подсчитать количество позиционных параметров в локаторе.
     * 
     * @param locator локатор для анализа
     * @return количество позиционных параметров
     */
    public static int getPositionalParameterCount(String locator) {
        if (locator == null) {
            return 0;
        }
        
        // Проверяем printf-style
        Matcher printfMatcher = PRINTF_PATTERN.matcher(locator);
        int printfCount = 0;
        while (printfMatcher.find()) {
            printfCount++;
        }
        if (printfCount > 0) {
            return printfCount;
        }
        
        // Проверяем {0}, {1}, {2} style
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(locator);
        int maxIndex = -1;
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            if (paramName.matches("\\d+")) {
                int index = Integer.parseInt(paramName);
                maxIndex = Math.max(maxIndex, index);
            }
        }
        
        return maxIndex + 1; // Индексы начинаются с 0
    }
    
    /**
     * Валидировать локатор с параметрами.
     * Проверяет, что все необходимые параметры предоставлены.
     * 
     * @param locator шаблон локатора
     * @param params параметры для подстановки
     * @throws IllegalArgumentException если не хватает параметров
     */
    public static void validate(String locator, Map<String, String> params) {
        if (locator == null) {
            throw new IllegalArgumentException("Локатор не может быть null");
        }
        
        if (!hasPlaceholders(locator)) {
            return; // Нет плейсхолдеров - валидация не требуется
        }
        
        java.util.List<String> requiredParams = getParameterNames(locator);
        
        if (params == null || params.isEmpty()) {
            if (!requiredParams.isEmpty()) {
                throw new IllegalArgumentException(
                    String.format("Локатор '%s' требует параметры: %s", 
                        locator, requiredParams));
            }
            return;
        }
        
        for (String paramName : requiredParams) {
            if (!params.containsKey(paramName)) {
                throw new IllegalArgumentException(
                    String.format("Отсутствует обязательный параметр '%s' для локатора: %s", 
                        paramName, locator));
            }
        }
    }
}