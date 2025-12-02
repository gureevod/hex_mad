package com.company.hex.db.annotations.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для определения DELETE запроса к базе данных.
 *
 * <p>Используется для декларативного определения SQL DELETE запросов
 * в методах репозитория. Поддерживает именованные параметры в формате {@code :paramName}.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Простой DELETE
 * @Delete("DELETE FROM users WHERE id = :id")
 * int deleteById(@Param("id") Long id);
 *
 * // DELETE с несколькими условиями
 * @Delete("DELETE FROM users WHERE active = false AND last_login < :date")
 * int deleteInactiveUsers(@Param("date") LocalDate date);
 *
 * // DELETE с IN clause
 * @Delete("DELETE FROM users WHERE id IN (:ids)")
 * int deleteByIds(@ParamList("ids") List<Long> ids);
 *
 * // DELETE без WHERE (требует @DangerousQuery)
 * @Delete("DELETE FROM temp_users")
 * @DangerousQuery(reason = "Очистка временной таблицы")
 * int deleteAllTempUsers();
 * }</pre>
 *
 * <h2>Возвращаемые типы</h2>
 * <ul>
 *     <li>{@code void} — результат не возвращается</li>
 *     <li>{@code int} — количество удалённых строк</li>
 *     <li>{@code boolean} — {@code true} если удалена хотя бы одна строка</li>
 * </ul>
 *
 * <h2>Безопасность</h2>
 * <p>DELETE запросы без WHERE clause считаются потенциально опасными.
 * Валидатор выдаст предупреждение, если WHERE clause отсутствует.
 * Для явного подтверждения намерения используйте аннотацию {@code @DangerousQuery}.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Param
 * @see ParamList
 * @see DangerousQuery
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Delete {

    /**
     * SQL DELETE запрос для выполнения.
     *
     * <p>Поддерживает именованные параметры в формате {@code :paramName},
     * которые будут заменены на соответствующие значения из аргументов метода,
     * аннотированных {@link Param}.</p>
     *
     * @return SQL DELETE запрос
     */
    String value();
}
