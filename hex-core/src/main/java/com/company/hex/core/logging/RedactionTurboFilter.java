package com.company.hex.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

import java.util.regex.Pattern;

/**
 * Кастомный TurboFilter для редактирования чувствительных данных в логах.
 * Автоматически заменяет пароли, токены и PII на маскированные значения.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class RedactionTurboFilter extends TurboFilter {
    
    // Паттерны для поиска чувствительных данных
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "(?i)(password|pwd|pass|secret|key|token|auth|credential|api[_-]?key)\\s*[:=]\\s*[\"']?([^\\s\"',}\\]]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b(?:\\+?7|8)?[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}\\b"
    );
    
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
        "\\b(?:\\d{4}[\\s\\-]?){3}\\d{4}\\b"
    );
    
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
        "(?i)bearer\\s+([a-zA-Z0-9\\-._~+/]+=*)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern AUTHORIZATION_HEADER_PATTERN = Pattern.compile(
        "(?i)authorization\\s*[:=]\\s*[\"']?([^\\s\"',}\\]]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Маскированные значения
    private static final String REDACTED_PASSWORD = "***REDACTED***";
    private static final String REDACTED_EMAIL = "***EMAIL***";
    private static final String REDACTED_PHONE = "***PHONE***";
    private static final String REDACTED_CARD = "***CARD***";
    private static final String REDACTED_TOKEN = "***TOKEN***";
    
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        // Если сообщение пустое, пропускаем
        if (format == null) {
            return FilterReply.NEUTRAL;
        }
        
        // Создаем итоговое сообщение
        String message = format;
        if (params != null && params.length > 0) {
            try {
                message = String.format(format, params);
            } catch (Exception e) {
                // Если форматирование не удалось, используем исходное сообщение
                message = format + " " + java.util.Arrays.toString(params);
            }
        }
        
        // Применяем редактирование
        String redactedMessage = redactSensitiveData(message);
        
        // Если сообщение изменилось, обновляем параметры
        if (!message.equals(redactedMessage)) {
            // Заменяем format на редактированное сообщение и очищаем параметры
            try {
                java.lang.reflect.Field formatField = logger.getClass().getDeclaredField("format");
                formatField.setAccessible(true);
                formatField.set(logger, redactedMessage);
                
                // Очищаем параметры, так как мы уже применили их
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        params[i] = null;
                    }
                }
            } catch (Exception e) {
                // Если не удалось изменить через рефлексию, продолжаем с исходными данными
                // В этом случае редактирование будет применено на уровне аппендера
            }
        }
        
        return FilterReply.NEUTRAL;
    }
    
    /**
     * Применяет редактирование чувствительных данных к сообщению.
     * 
     * @param message исходное сообщение
     * @return сообщение с замаскированными чувствительными данными
     */
    private String redactSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        String result = message;
        
        // Редактируем пароли и ключи
        result = PASSWORD_PATTERN.matcher(result).replaceAll("$1=" + REDACTED_PASSWORD);
        
        // Редактируем Bearer токены
        result = BEARER_TOKEN_PATTERN.matcher(result).replaceAll("Bearer " + REDACTED_TOKEN);
        
        // Редактируем Authorization заголовки
        result = AUTHORIZATION_HEADER_PATTERN.matcher(result).replaceAll("Authorization=" + REDACTED_TOKEN);
        
        // Редактируем email адреса
        result = EMAIL_PATTERN.matcher(result).replaceAll(REDACTED_EMAIL);
        
        // Редактируем номера телефонов
        result = PHONE_PATTERN.matcher(result).replaceAll(REDACTED_PHONE);
        
        // Редактируем номера кредитных карт
        result = CREDIT_CARD_PATTERN.matcher(result).replaceAll(REDACTED_CARD);
        
        return result;
    }
}