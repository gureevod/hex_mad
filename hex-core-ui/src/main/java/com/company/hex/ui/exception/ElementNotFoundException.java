package com.company.hex.ui.exception;

/**
 * Исключение при ненайденном элементе с детальной диагностикой.
 * Предоставляет контекстную информацию и подсказки для решения проблемы.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ElementNotFoundException extends RuntimeException {
    
    private final String elementName;
    private final String locator;
    private final String pageName;
    private final String componentName;
    private final String suggestions;
    
    /**
     * Создать исключение с полной контекстной информацией.
     * 
     * @param elementName имя элемента
     * @param locator локатор элемента
     * @param pageName имя страницы
     * @param componentName имя компонента (может быть null)
     * @param cause исходное исключение
     */
    public ElementNotFoundException(String elementName, String locator, 
                                    String pageName, String componentName,
                                    Throwable cause) {
        super(buildMessage(elementName, locator, pageName, componentName), cause);
        this.elementName = elementName;
        this.locator = locator;
        this.pageName = pageName;
        this.componentName = componentName;
        this.suggestions = generateSuggestions(locator);
    }
    
    /**
     * Создать исключение без исходной причины.
     * 
     * @param elementName имя элемента
     * @param locator локатор элемента
     * @param pageName имя страницы
     * @param componentName имя компонента (может быть null)
     */
    public ElementNotFoundException(String elementName, String locator, 
                                    String pageName, String componentName) {
        this(elementName, locator, pageName, componentName, null);
    }
    
    /**
     * Построить детальное сообщение об ошибке.
     * 
     * @param elementName имя элемента
     * @param locator локатор
     * @param pageName имя страницы
     * @param componentName имя компонента
     * @return форматированное сообщение
     */
    private static String buildMessage(String elementName, String locator,
                                       String pageName, String componentName) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    ЭЛЕМЕНТ НЕ НАЙДЕН                           ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Элемент:   %-50s ║\n", truncate(elementName, 50)));
        sb.append(String.format("║ Страница:  %-50s ║\n", truncate(pageName, 50)));
        sb.append(String.format("║ Компонент: %-50s ║\n", truncate(componentName != null ? componentName : "Root", 50)));
        sb.append("║ Локатор:                                                       ║\n");
        
        // Разбиваем длинный локатор на строки
        String[] locatorLines = splitLocator(locator, 60);
        for (String line : locatorLines) {
            sb.append(String.format("║   %-60s ║\n", line));
        }
        
        sb.append("╚════════════════════════════════════════════════════════════════╝");
        return sb.toString();
    }
    
    /**
     * Генерировать подсказки для решения проблемы.
     * 
     * @param locator локатор элемента
     * @return строка с подсказками
     */
    private static String generateSuggestions(String locator) {
        StringBuilder suggestions = new StringBuilder("\n📋 Возможные причины:\n");
        
        if (locator.startsWith("//") || locator.startsWith(".//")) {
            suggestions.append("  • Проверьте правильность XPath выражения\n");
            suggestions.append("  • Убедитесь, что элемент не находится в iframe\n");
        }
        
        if (locator.contains("@id=")) {
            suggestions.append("  • ID может быть динамическим - используйте contains(@id, '...')\n");
        }
        
        if (locator.contains("@class=")) {
            suggestions.append("  • Классы могут меняться - используйте contains(@class, '...')\n");
        }
        
        if (locator.contains("[@") || locator.contains("[text()")) {
            suggestions.append("  • Проверьте точность условий в квадратных скобках\n");
        }
        
        suggestions.append("  • Элемент может загружаться асинхронно - увеличьте timeout\n");
        suggestions.append("  • Элемент может быть скрыт (display:none или visibility:hidden)\n");
        suggestions.append("  • Проверьте, не находится ли элемент в Shadow DOM\n");
        suggestions.append("  • Используйте инструменты разработчика браузера для проверки локатора\n");
        
        return suggestions.toString();
    }
    
    /**
     * Обрезать строку до максимальной длины.
     * 
     * @param str строка
     * @param maxLength максимальная длина
     * @return обрезанная строка
     */
    private static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Разбить длинный локатор на строки.
     * 
     * @param locator локатор
     * @param maxLength максимальная длина строки
     * @return массив строк
     */
    private static String[] splitLocator(String locator, int maxLength) {
        if (locator.length() <= maxLength) {
            return new String[] { locator };
        }
        
        java.util.List<String> lines = new java.util.ArrayList<>();
        int start = 0;
        while (start < locator.length()) {
            int end = Math.min(start + maxLength, locator.length());
            lines.add(locator.substring(start, end));
            start = end;
        }
        
        return lines.toArray(new String[0]);
    }
    
    /**
     * Получить подсказки для решения проблемы.
     * 
     * @return строка с подсказками
     */
    public String getSuggestions() {
        return suggestions;
    }
    
    /**
     * Получить имя элемента.
     * 
     * @return имя элемента
     */
    public String getElementName() {
        return elementName;
    }
    
    /**
     * Получить локатор элемента.
     * 
     * @return локатор
     */
    public String getLocator() {
        return locator;
    }
    
    /**
     * Получить имя страницы.
     * 
     * @return имя страницы
     */
    public String getPageName() {
        return pageName;
    }
    
    /**
     * Получить имя компонента.
     * 
     * @return имя компонента
     */
    public String getComponentName() {
        return componentName;
    }
    
    @Override
    public String getMessage() {
        return super.getMessage() + suggestions;
    }
}