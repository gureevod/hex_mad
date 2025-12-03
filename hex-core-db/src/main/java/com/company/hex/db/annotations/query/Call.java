package com.company.hex.db.annotations.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для вызова хранимой процедуры или функции базы данных.
 *
 * <p>Используется для декларативного определения вызовов stored procedures
 * в методах репозитория. Поддерживает IN, OUT и INOUT параметры.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Простой вызов процедуры
 * @Call("{call update_user_status(:userId, :status)}")
 * void updateUserStatus(@Param("userId") Long userId, @Param("status") String status);
 *
 * // Вызов функции с возвратом значения
 * @Call("{? = call calculate_total(:orderId)}")
 * BigDecimal calculateOrderTotal(@Param("orderId") Long orderId);
 *
 * // Процедура с OUT параметром
 * @Call("{call get_user_count(:count)}")
 * void getUserCount(@OutParam(value = "count", sqlType = Types.INTEGER) Integer[] count);
 *
 * // Процедура с INOUT параметром
 * @Call("{call increment_counter(:counter)}")
 * void incrementCounter(@InOutParam("counter") AtomicInteger counter);
 *
 * // PostgreSQL функция
 * @Call("SELECT * FROM get_user_orders(:userId)")
 * List<Order> getUserOrders(@Param("userId") Long userId);
 * }</pre>
 *
 * <h2>Синтаксис вызова</h2>
 * <ul>
 *     <li>{@code {call procedure_name(params)}} — стандартный JDBC escape синтаксис</li>
 *     <li>{@code {? = call function_name(params)}} — функция с возвратом значения</li>
 *     <li>{@code SELECT * FROM function_name(params)} — PostgreSQL стиль</li>
 * </ul>
 *
 * <h2>Типы параметров</h2>
 * <ul>
 *     <li>{@code @Param} — IN параметр (входной)</li>
 *     <li>{@code @OutParam} — OUT параметр (выходной)</li>
 *     <li>{@code @InOutParam} — INOUT параметр (двунаправленный)</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see com.company.hex.db.annotations.param.Param
 * @see com.company.hex.db.annotations.param.OutParam
 * @see com.company.hex.db.annotations.param.InOutParam
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Call {

    /**
     * SQL вызов процедуры или функции.
     *
     * <p>Поддерживает именованные параметры в формате {@code :paramName},
     * которые будут заменены на соответствующие значения из аргументов метода.</p>
     *
     * @return SQL вызов процедуры
     */
    String value();
}
