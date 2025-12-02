package com.company.hex.db.annotations.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает таймаут выполнения для метода или всего сервиса.
 *
 * <p>Переопределяет значение таймаута, указанное в {@link DbService#queryTimeout()}.
 * Если запрос не завершится в указанное время, будет выброшено
 * {@code QueryTimeoutException}.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * @DbService(queryTimeout = 30)  // значение по умолчанию для сервиса
 * public interface ReportRepository {
 *
 *     // Использует таймаут сервиса (30 сек)
 *     @Select("SELECT * FROM users WHERE id = :id")
 *     Optional<User> findById(@Param("id") Long id);
 *
 *     // Увеличенный таймаут для тяжёлого отчёта
 *     @Select("SELECT * FROM generate_report(:startDate, :endDate)")
 *     @Timeout(value = 300, unit = TimeUnit.SECONDS)
 *     List<ReportRow> generateReport(@Param("startDate") LocalDate startDate,
 *                                    @Param("endDate") LocalDate endDate);
 *
 *     // Короткий таймаут для быстрых операций
 *     @Select("SELECT count(*) FROM users")
 *     @Timeout(5)  // 5 секунд
 *     long countUsers();
 * }
 * }</pre>
 *
 * <h2>Поведение при таймауте</h2>
 * <ul>
 *     <li>Выбрасывается {@code QueryTimeoutException}</li>
 *     <li>Текущая транзакция помечается для rollback</li>
 *     <li>Соединение возвращается в пул</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DbService#queryTimeout()
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timeout {

    /**
     * Значение таймаута.
     *
     * <p>Интерпретируется в единицах, указанных в {@link #unit()}.</p>
     *
     * @return значение таймаута
     */
    int value();

    /**
     * Единица измерения таймаута.
     *
     * <p>По умолчанию секунды.</p>
     *
     * @return единица измерения
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * Единицы измерения времени для таймаута.
     */
    enum TimeUnit {
        /**
         * Миллисекунды.
         */
        MILLISECONDS,

        /**
         * Секунды.
         */
        SECONDS,

        /**
         * Минуты.
         */
        MINUTES
    }
}
