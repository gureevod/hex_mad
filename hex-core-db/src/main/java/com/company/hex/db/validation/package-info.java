/**
 * Пакет с валидаторами SQL запросов.
 *
 * <p>Предоставляет статическую валидацию SQL до выполнения:</p>
 *
 * <ul>
 *     <li>{@code QueryValidator} — интерфейс валидатора</li>
 *     <li>{@code ParameterValidator} — проверка соответствия параметров</li>
 *     <li>{@code SecurityValidator} — обнаружение опасных операций</li>
 *     <li>{@code CompositeQueryValidator} — комбинирование валидаторов</li>
 * </ul>
 *
 * <h2>Типы валидаций</h2>
 * <ul>
 *     <li>Синтаксические: параметры (:param) совпадают с @Param аннотациями</li>
 *     <li>Безопасности: UPDATE/DELETE без WHERE требует @DangerousQuery</li>
 *     <li>Типов: предупреждение о nullable без Optional</li>
 * </ul>
 *
 * <h2>Примеры валидации</h2>
 * <pre>{@code
 * // ❌ QueryValidationException: Parameter mismatch
 * @Select("SELECT * FROM users WHERE id = :userId")
 * Optional<User> findById(@Param("id") Long id);
 *
 * // ⚠️ Warning: UPDATE without WHERE
 * @Update("UPDATE users SET active = false")
 * int deactivateAll();
 *
 * // ✅ Explicit dangerous operation
 * @Update("UPDATE users SET active = false")
 * @DangerousQuery(reason = "Deactivate all for cleanup")
 * int deactivateAll();
 * }</pre>
 *
 * @since 1.0.0
 */
package com.company.hex.db.validation;
