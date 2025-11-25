package com.company.hex.ui.interceptor;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Реестр для управления ElementInterceptor'ами.
 * Thread-safe для использования в параллельных тестах.
 * 
 * <p>Использование:</p>
 * <pre>
 * {@code
 * // В @BeforeAll или setUp
 * ElementInterceptorRegistry.register(new ScreenshotOnErrorInterceptor());
 * ElementInterceptorRegistry.register(new MetricsInterceptor());
 * 
 * // В @AfterAll или tearDown
 * ElementInterceptorRegistry.clear();
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public final class ElementInterceptorRegistry {
    
    private static final List<ElementInterceptor> interceptors = new CopyOnWriteArrayList<>();
    
    private ElementInterceptorRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Зарегистрировать interceptor.
     * Interceptors сортируются по приоритету (order).
     * 
     * @param interceptor interceptor для регистрации
     * @throws IllegalArgumentException если interceptor null
     */
    public static void register(ElementInterceptor interceptor) {
        if (interceptor == null) {
            throw new IllegalArgumentException("Interceptor не может быть null");
        }
        interceptors.add(interceptor);
        sortByOrder();
    }
    
    /**
     * Удалить interceptor.
     * 
     * @param interceptor interceptor для удаления
     */
    public static void unregister(ElementInterceptor interceptor) {
        interceptors.remove(interceptor);
    }
    
    /**
     * Удалить все interceptors определенного типа.
     * 
     * @param interceptorClass класс interceptor'а
     */
    public static void unregisterByType(Class<? extends ElementInterceptor> interceptorClass) {
        interceptors.removeIf(interceptor -> interceptorClass.isInstance(interceptor));
    }
    
    /**
     * Очистить все interceptor'ы.
     * Рекомендуется вызывать в @AfterAll или tearDown.
     */
    public static void clear() {
        interceptors.clear();
    }
    
    /**
     * Получить все зарегистрированные interceptor'ы.
     * Возвращает неизменяемую копию списка.
     * 
     * @return список interceptor'ов
     */
    public static List<ElementInterceptor> getAll() {
        return List.copyOf(interceptors);
    }
    
    /**
     * Проверить, зарегистрированы ли какие-либо interceptor'ы.
     * 
     * @return true если есть зарегистрированные interceptor'ы
     */
    public static boolean hasInterceptors() {
        return !interceptors.isEmpty();
    }
    
    /**
     * Получить количество зарегистрированных interceptor'ов.
     * 
     * @return количество interceptor'ов
     */
    public static int count() {
        return interceptors.size();
    }
    
    /**
     * Отсортировать interceptor'ы по приоритету (order).
     * Меньший order = выше приоритет (выполняется раньше).
     */
    private static void sortByOrder() {
        interceptors.sort(Comparator.comparingInt(ElementInterceptor::getOrder));
    }
}