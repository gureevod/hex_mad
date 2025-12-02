package com.company.hex.db.annotations.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает выходной параметр для хранимых процедур.
 *
 * <p>Используется совместно с аннотацией {@code @Call} для получения
 * значений из OUT или INOUT параметров хранимых процедур.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // OUT параметр для получения результата
 * @Call("{call calculate_total(:orderId, :result)}")
 * void calculateTotal(@Param("orderId") Long orderId,
 *                     @OutParam(value = "result", sqlType = Types.DECIMAL) BigDecimal[] result);
 *
 * // Несколько OUT параметров
 * @Call("{call get_user_stats(:userId, :orderCount, :totalAmount)}")
 * void getUserStats(@Param("userId") Long userId,
 *                   @OutParam(value = "orderCount", sqlType = Types.INTEGER) Integer[] orderCount,
 *                   @OutParam(value = "totalAmount", sqlType = Types.DECIMAL) BigDecimal[] totalAmount);
 *
 * // INOUT параметр (для @InOutParam см. соответствующую аннотацию)
 * }</pre>
 *
 * <h2>Использование результата</h2>
 * <p>OUT параметры передаются как одноэлементные массивы для возможности
 * модификации значения. После выполнения процедуры результат доступен
 * в первом элементе массива:</p>
 * <pre>{@code
 * BigDecimal[] result = new BigDecimal[1];
 * orderRepo.calculateTotal(orderId, result);
 * BigDecimal total = result[0];
 * }</pre>
 *
 * <h2>Поддерживаемые SQL типы</h2>
 * <ul>
 *     <li>{@link java.sql.Types#VARCHAR} — для String</li>
 *     <li>{@link java.sql.Types#INTEGER} — для Integer</li>
 *     <li>{@link java.sql.Types#BIGINT} — для Long</li>
 *     <li>{@link java.sql.Types#DECIMAL} — для BigDecimal</li>
 *     <li>{@link java.sql.Types#TIMESTAMP} — для LocalDateTime</li>
 *     <li>и другие типы из {@link java.sql.Types}</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Param
 * @see InOutParam
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface OutParam {

    /**
     * Имя параметра в вызове хранимой процедуры.
     *
     * <p>Должно соответствовать имени OUT параметра в {@code @Call}.</p>
     *
     * @return имя параметра
     */
    String value();

    /**
     * SQL тип выходного параметра.
     *
     * <p>Обязательное поле. Указывает тип из {@link java.sql.Types}
     * для регистрации OUT параметра в CallableStatement.</p>
     *
     * @return SQL тип из {@link java.sql.Types}
     */
    int sqlType();
}
