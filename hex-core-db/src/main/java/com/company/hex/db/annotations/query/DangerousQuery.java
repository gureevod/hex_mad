package com.company.hex.db.annotations.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркерная аннотация для явного подтверждения потенциально опасных SQL операций.
 *
 * <p>Используется для подавления предупреждений валидатора при выполнении
 * UPDATE или DELETE запросов без WHERE clause, которые могут затронуть
 * все записи в таблице.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // UPDATE без WHERE с явным подтверждением
 * @Update("UPDATE users SET active = false")
 * @DangerousQuery(reason = "Деактивация всех пользователей для ежегодной очистки")
 * int deactivateAllUsers();
 *
 * // DELETE без WHERE с подтверждением
 * @Delete("DELETE FROM temp_sessions")
 * @DangerousQuery(reason = "Очистка временных сессий при старте тестов")
 * int clearTempSessions();
 *
 * // TRUNCATE операция
 * @Script(resource = "db/truncate-all.sql")
 * @DangerousQuery(reason = "Полная очистка БД перед тестовым прогоном")
 * void truncateAllTables();
 * }</pre>
 *
 * <h2>Когда использовать</h2>
 * <ul>
 *     <li>UPDATE запросы без WHERE clause</li>
 *     <li>DELETE запросы без WHERE clause</li>
 *     <li>TRUNCATE операции</li>
 *     <li>DROP операции в тестовых скриптах</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Update
 * @see Delete
 * @see Script
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DangerousQuery {

    /**
     * Причина использования потенциально опасной операции.
     *
     * <p>Обязательное поле для документирования намерения разработчика.
     * Причина будет включена в логи при выполнении запроса.</p>
     *
     * @return описание причины использования опасной операции
     */
    String reason();

    /**
     * Требовать подтверждение при выполнении в production-like окружениях.
     *
     * <p>Если {@code true}, выполнение запроса в окружениях, отличных от
     * локального и CI, потребует дополнительного подтверждения через
     * системное свойство {@code hex.db.dangerousQueryConfirmed=true}.</p>
     *
     * @return требуется ли подтверждение для production
     */
    boolean requireConfirmation() default false;
}
