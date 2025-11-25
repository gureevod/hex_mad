package com.company.hex.ui.interceptor;

import com.company.hex.ui.core.BaseElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interceptor для сбора метрик выполнения действий с UI элементами.
 * Собирает статистику по количеству вызовов, ошибкам и времени выполнения.
 * 
 * <p>Использование:</p>
 * <pre>
 * {@code
 * MetricsInterceptor metrics = new MetricsInterceptor();
 * ElementInterceptorRegistry.register(metrics);
 * 
 * // Выполнить тесты...
 * 
 * // Получить статистику
 * Map<String, Long> actionCounts = metrics.getActionCounts();
 * Map<String, Long> errorCounts = metrics.getErrorCounts();
 * logger.info("Total clicks: {}", actionCounts.get("click"));
 * logger.info("Total errors: {}", errorCounts.values().stream().mapToLong(Long::longValue).sum());
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class MetricsInterceptor implements ElementInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsInterceptor.class);
    
    private final Map<String, AtomicLong> actionCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final ThreadLocal<Long> actionStartTime = new ThreadLocal<>();
    
    @Override
    public void beforeAction(BaseElement element, String actionName, Object[] args) {
        actionStartTime.set(System.currentTimeMillis());
        actionCounts.computeIfAbsent(actionName, k -> new AtomicLong()).incrementAndGet();
    }
    
    @Override
    public void afterAction(BaseElement element, String actionName, Object result) {
        long duration = System.currentTimeMillis() - actionStartTime.get();
        actionStartTime.remove();
        
        logger.trace("Действие '{}' на элементе '{}' выполнено за {}ms", 
            actionName, element.getName(), duration);
    }
    
    @Override
    public void onError(BaseElement element, String actionName, Exception exception) {
        errorCounts.computeIfAbsent(actionName, k -> new AtomicLong()).incrementAndGet();
        
        logger.debug("Действие '{}' на элементе '{}' завершилось с ошибкой: {}", 
            actionName, element.getName(), exception.getMessage());
    }
    
    /**
     * Получить количество вызовов каждого действия.
     * 
     * @return map с количеством вызовов по действиям
     */
    public Map<String, Long> getActionCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        actionCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
    
    /**
     * Получить количество ошибок для каждого действия.
     * 
     * @return map с количеством ошибок по действиям
     */
    public Map<String, Long> getErrorCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        errorCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
    
    /**
     * Получить общее количество действий.
     * 
     * @return общее количество действий
     */
    public long getTotalActions() {
        return actionCounts.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
    }
    
    /**
     * Получить общее количество ошибок.
     * 
     * @return общее количество ошибок
     */
    public long getTotalErrors() {
        return errorCounts.values().stream()
            .mapToLong(AtomicLong::get)
            .sum();
    }
    
    /**
     * Сбросить все метрики.
     */
    public void reset() {
        actionCounts.clear();
        errorCounts.clear();
        actionStartTime.remove();
    }
    
    @Override
    public int getOrder() {
        return 100; // Стандартный приоритет
    }
}