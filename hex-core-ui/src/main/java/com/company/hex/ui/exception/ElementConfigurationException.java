package com.company.hex.ui.exception;

/**
 * Исключение при ошибке конфигурации элемента.
 * Используется для сообщения о проблемах в аннотациях, неправильных типах полей и т.д.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ElementConfigurationException extends RuntimeException {
    
    private final String fieldName;
    private final String className;
    private final String issue;
    
    /**
     * Создать исключение с детальной информацией о проблеме конфигурации.
     * 
     * @param fieldName имя поля с ошибкой
     * @param className имя класса
     * @param issue описание проблемы
     */
    public ElementConfigurationException(String fieldName, String className, String issue) {
        super(buildMessage(fieldName, className, issue));
        this.fieldName = fieldName;
        this.className = className;
        this.issue = issue;
    }
    
    /**
     * Создать исключение с детальной информацией и исходной причиной.
     * 
     * @param fieldName имя поля с ошибкой
     * @param className имя класса
     * @param issue описание проблемы
     * @param cause исходное исключение
     */
    public ElementConfigurationException(String fieldName, String className, String issue, Throwable cause) {
        super(buildMessage(fieldName, className, issue), cause);
        this.fieldName = fieldName;
        this.className = className;
        this.issue = issue;
    }
    
    /**
     * Построить сообщение об ошибке конфигурации.
     * 
     * @param fieldName имя поля
     * @param className имя класса
     * @param issue описание проблемы
     * @return форматированное сообщение
     */
    private static String buildMessage(String fieldName, String className, String issue) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              ОШИБКА КОНФИГУРАЦИИ ЭЛЕМЕНТА                      ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Поле:  %-54s ║\n", truncate(fieldName, 54)));
        sb.append(String.format("║ Класс: %-54s ║\n", truncate(className, 54)));
        sb.append("║                                                                ║\n");
        sb.append("║ Проблема:                                                      ║\n");
        
        // Разбиваем длинное описание проблемы на строки
        String[] issueLines = splitText(issue, 60);
        for (String line : issueLines) {
            sb.append(String.format("║   %-60s ║\n", line));
        }
        
        sb.append("║                                                                ║\n");
        sb.append("║ 💡 Рекомендации:                                               ║\n");
        sb.append("║   • Проверьте аннотации @Element, @Elements или @Component     ║\n");
        sb.append("║   • Убедитесь, что поле имеет правильный тип                   ║\n");
        sb.append("║   • Проверьте наличие обязательных параметров в аннотациях     ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝");
        
        return sb.toString();
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
     * Разбить текст на строки заданной длины.
     * 
     * @param text текст
     * @param maxLength максимальная длина строки
     * @return массив строк
     */
    private static String[] splitText(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return new String[] { "" };
        }
        
        if (text.length() <= maxLength) {
            return new String[] { text };
        }
        
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                // Если слово само длиннее maxLength, разбиваем его
                if (word.length() > maxLength) {
                    int start = 0;
                    while (start < word.length()) {
                        int end = Math.min(start + maxLength, word.length());
                        lines.add(word.substring(start, end));
                        start = end;
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    
    /**
     * Получить имя поля с ошибкой.
     * 
     * @return имя поля
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Получить имя класса.
     * 
     * @return имя класса
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Получить описание проблемы.
     * 
     * @return описание проблемы
     */
    public String getIssue() {
        return issue;
    }
}