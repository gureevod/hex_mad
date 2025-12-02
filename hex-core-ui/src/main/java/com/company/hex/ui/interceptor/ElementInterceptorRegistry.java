package com.company.hex.ui.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Thread-safe реестр для управления ElementInterceptor'ами.
 */
public final class ElementInterceptorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ElementInterceptorRegistry.class);

    // Используем обычный ArrayList + ReadWriteLock вместо CopyOnWriteArrayList
    // для лучшего контроля над сортировкой
    private static final List<ElementInterceptor> interceptors = new ArrayList<>();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Кешированный отсортированный список
    private static volatile List<ElementInterceptor> cachedSortedList = Collections.emptyList();
    private static volatile boolean cacheValid = false;

    private ElementInterceptorRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Зарегистрировать interceptor.
     */
    public static void register(ElementInterceptor interceptor) {
        Objects.requireNonNull(interceptor, "Interceptor не может быть null");

        lock.writeLock().lock();
        try {
            // Проверяем на дубликаты
            if (interceptors.contains(interceptor)) {
                logger.warn("Interceptor {} уже зарегистрирован",
                        interceptor.getClass().getSimpleName());
                return;
            }

            interceptors.add(interceptor);
            cacheValid = false;

            logger.debug("Зарегистрирован interceptor: {} с приоритетом {}",
                    interceptor.getClass().getSimpleName(), interceptor.getOrder());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удалить interceptor.
     */
    public static void unregister(ElementInterceptor interceptor) {
        lock.writeLock().lock();
        try {
            if (interceptors.remove(interceptor)) {
                cacheValid = false;
                logger.debug("Удалён interceptor: {}", interceptor.getClass().getSimpleName());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удалить все interceptors определенного типа.
     */
    public static void unregisterByType(Class<? extends ElementInterceptor> interceptorClass) {
        lock.writeLock().lock();
        try {
            boolean removed = interceptors.removeIf(interceptorClass::isInstance);
            if (removed) {
                cacheValid = false;
                logger.debug("Удалены interceptors типа: {}", interceptorClass.getSimpleName());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Очистить все interceptor'ы.
     */
    public static void clear() {
        lock.writeLock().lock();
        try {
            interceptors.clear();
            cachedSortedList = Collections.emptyList();
            cacheValid = true;
            logger.debug("Все interceptors очищены");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Получить все зарегистрированные interceptor'ы (отсортированные).
     * Использует кеширование для производительности.
     */
    public static List<ElementInterceptor> getAll() {
        // Быстрая проверка без блокировки
        if (cacheValid) {
            return cachedSortedList;
        }

        lock.readLock().lock();
        try {
            if (cacheValid) {
                return cachedSortedList;
            }
        } finally {
            lock.readLock().unlock();
        }

        // Нужно обновить кеш
        lock.writeLock().lock();
        try {
            if (!cacheValid) {
                List<ElementInterceptor> sorted = new ArrayList<>(interceptors);
                sorted.sort(Comparator.comparingInt(ElementInterceptor::getOrder));
                cachedSortedList = Collections.unmodifiableList(sorted);
                cacheValid = true;

                logger.trace("Пересоздан кеш interceptors: {} элементов", sorted.size());
            }
            return cachedSortedList;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Проверить, зарегистрированы ли какие-либо interceptor'ы.
     */
    public static boolean hasInterceptors() {
        lock.readLock().lock();
        try {
            return !interceptors.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Получить количество зарегистрированных interceptor'ов.
     */
    public static int count() {
        lock.readLock().lock();
        try {
            return interceptors.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Выполнить действие через цепочку interceptors.
     * Это главный метод для использования в BaseElement.
     */
    public static <R> R execute(ActionContext context, Supplier<R> action) {
        List<ElementInterceptor> currentInterceptors = getAll();

        if (currentInterceptors.isEmpty()) {
            return action.get();
        }

        ActionChain<R> chain = ActionChain.create(currentInterceptors, context, action);
        return chain.proceed();
    }
}