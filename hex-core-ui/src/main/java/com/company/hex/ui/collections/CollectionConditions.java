package com.company.hex.ui.collections;

/**
 * Дополнительные условия для проверки коллекций элементов.
 * Предоставляет удобные методы для создания кастомных условий проверки коллекций.
 * 
 * <p>Примечание: Большинство стандартных условий уже доступны в Selenide через
 * {@code com.codeborne.selenide.CollectionCondition}. Этот класс предоставляет
 * дополнительные условия, специфичные для Hex Framework.</p>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class CollectionConditions {
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private CollectionConditions() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }
    
    // Примечание: Дополнительные кастомные условия могут быть добавлены здесь по мере необходимости.
    // Для базовых проверок используйте стандартные Selenide условия:
    // - CollectionCondition.size(n)
    // - CollectionCondition.sizeGreaterThan(n)
    // - CollectionCondition.sizeLessThan(n)
    // - CollectionCondition.empty
    // - CollectionCondition.texts(...)
    // - и другие из com.codeborne.selenide.CollectionCondition
}