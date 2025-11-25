package com.company.hex.ui.interceptor;

import com.company.hex.ui.core.BaseElement;

/**
 * Interceptor для перехвата действий с UI элементами.
 * Позволяет добавить логику до/после действия или при ошибке.
 * 
 * <p>Примеры использования:</p>
 * <ul>
 *   <li>Автоматические скриншоты при ошибках</li>
 *   <li>Сбор метрик производительности</li>
 *   <li>Дополнительное логирование</li>
 *   <li>Retry логика для нестабильных элементов</li>
 * </ul>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public interface ElementInterceptor {
    
    /**
     * Вызывается до выполнения действия.
     * 
     * @param element элемент, над которым выполняется действие
     * @param actionName имя действия (click, fill, shouldBe и т.д.)
     * @param args аргументы действия (может быть пустым массивом)
     */
    default void beforeAction(BaseElement element, String actionName, Object[] args) {
        // По умолчанию ничего не делаем
    }
    
    /**
     * Вызывается после успешного выполнения действия.
     * 
     * @param element элемент, над которым выполнялось действие
     * @param actionName имя действия
     * @param result результат действия (может быть null для void методов)
     */
    default void afterAction(BaseElement element, String actionName, Object result) {
        // По умолчанию ничего не делаем
    }
    
    /**
     * Вызывается при ошибке во время выполнения действия.
     * 
     * @param element элемент, над которым выполнялось действие
     * @param actionName имя действия
     * @param exception исключение, которое произошло
     */
    default void onError(BaseElement element, String actionName, Exception exception) {
        // По умолчанию ничего не делаем
    }
    
    /**
     * Приоритет interceptor'а. Меньше значение = раньше выполняется.
     * По умолчанию 100.
     * 
     * @return приоритет
     */
    default int getOrder() {
        return 100;
    }
}