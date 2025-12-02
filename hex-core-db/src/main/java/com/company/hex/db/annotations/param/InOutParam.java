package com.company.hex.db.annotations.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает входной-выходной параметр для хранимых процедур.
 *
 * <p>Используется совместно с аннотацией {@code @Call} для параметров,
 * которые передают значение в хранимую процедуру и получают модифицированное
 * значение обратно.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // INOUT параметр для инкремента счётчика
 * @Call("{call increment_counter(:counter)}")
 * void incrementCounter(@InOutParam(value = "counter", sqlType = Types.INTEGER) Integer[] counter);
 *
 * // INOUT параметр для модификации строки
 * @Call("{call format_name(:name)}")
 * void formatName(@InOutParam(value = "name", sqlType = Types.VARCHAR) String[] name);
 *
 * // Комбинация IN, OUT и INOUT
 * @Call("{call process_order(:orderId, :status, :errorMessage)}")
 * void processOrder(@Param("orderId") Long orderId,
 *                   @InOutParam(value = "status", sqlType = Types.VARCHAR) String[] status,
 *                   @OutParam(value = "errorMessage", sqlType = Types.VARCHAR) String[] errorMessage);
 * }</pre>
 *
 * <h2>Использование</h2>
 * <p>INOUT параметры передаются как одноэлементные массивы с начальным
 * значением. После выполнения процедуры модифицированное значение
 * доступно в первом элементе массива:</p>
 * <pre>{@code
 * Integer[] counter = new Integer[]{0};
 * repo.incrementCounter(counter);
 * System.out.println(counter[0]); // 1
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Param
 * @see OutParam
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface InOutParam {

    /**
     * Имя параметра в вызове хранимой процедуры.
     *
     * <p>Должно соответствовать имени INOUT параметра в {@code @Call}.</p>
     *
     * @return имя параметра
     */
    String value();

    /**
     * SQL тип параметра.
     *
     * <p>Обязательное поле. Указывает тип из {@link java.sql.Types}
     * для регистрации INOUT параметра в CallableStatement.</p>
     *
     * @return SQL тип из {@link java.sql.Types}
     */
    int sqlType();
}
