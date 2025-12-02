package com.company.hex.db.annotations.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, что метод выполняет только операции чтения.
 *
 * <p>Это позволяет оптимизировать выполнение запросов:
 * использовать read-only транзакции, read replicas и т.д.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * @DbService
 * public interface UserRepository {
 *
 *     // Операция чтения — может использовать read replica
 *     @Select("SELECT * FROM users WHERE id = :id")
 *     @ReadOnly
 *     Optional<User> findById(@Param("id") Long id);
 *
 *     // Операция чтения списка
 *     @Select("SELECT * FROM users WHERE active = true")
 *     @ReadOnly
 *     List<User> findActiveUsers();
 *
 *     // Операция записи — НЕ должна быть @ReadOnly
 *     @Insert("INSERT INTO users (name, email) VALUES (:name, :email)")
 *     @ReturnGeneratedKeys
 *     Long create(@Param("name") String name, @Param("email") String email);
 * }
 * }</pre>
 *
 * <h2>Поведение</h2>
 * <ul>
 *     <li>Connection устанавливается в read-only режим</li>
 *     <li>Может использоваться read replica (если настроено)</li>
 *     <li>Попытка выполнить INSERT/UPDATE/DELETE вызовет исключение</li>
 *     <li>Транзакция всегда завершается commit (не rollback)</li>
 * </ul>
 *
 * <h2>Совместимость с базами данных</h2>
 * <ul>
 *     <li>PostgreSQL — полная поддержка read-only транзакций</li>
 *     <li>MySQL — поддержка через read replica routing</li>
 *     <li>H2 — игнорируется (нет read replicas)</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Transactional
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {

    /**
     * Использовать read replica для запроса.
     *
     * <p>Если {@code true} и настроен пул read replicas, запрос
     * будет направлен на read replica вместо primary.</p>
     *
     * <p>По умолчанию {@code true}.</p>
     *
     * @return использовать ли read replica
     */
    boolean useReplica() default true;
}
