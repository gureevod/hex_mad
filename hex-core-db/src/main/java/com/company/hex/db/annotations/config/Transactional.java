package com.company.hex.db.annotations.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, что метод должен выполняться в транзакции.
 *
 * <p>Управляет транзакционным поведением метода репозитория.
 * По умолчанию каждый метод выполняется в отдельной транзакции
 * с autocommit. Эта аннотация позволяет настроить более сложное
 * поведение.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * @DbService
 * public interface OrderRepository {
 *
 *     // Простая транзакция
 *     @Insert("INSERT INTO orders (user_id, total) VALUES (:userId, :total)")
 *     @Transactional
 *     @ReturnGeneratedKeys
 *     Long create(@Param("userId") Long userId, @Param("total") BigDecimal total);
 *
 *     // Транзакция с явным isolation level
 *     @Select("SELECT * FROM orders WHERE id = :id FOR UPDATE")
 *     @Transactional(isolation = Isolation.SERIALIZABLE)
 *     Optional<Order> findByIdForUpdate(@Param("id") Long id);
 *
 *     // Новая транзакция (вложенные вызовы)
 *     @Update("UPDATE orders SET status = :status WHERE id = :id")
 *     @Transactional(propagation = Propagation.REQUIRES_NEW)
 *     int updateStatus(@Param("id") Long id, @Param("status") String status);
 *
 *     // Read-only транзакция
 *     @Select("SELECT * FROM orders WHERE user_id = :userId")
 *     @Transactional(readOnly = true)
 *     List<Order> findByUserId(@Param("userId") Long userId);
 * }
 * }</pre>
 *
 * <h2>Программное управление транзакциями</h2>
 * <pre>{@code
 * TransactionManager tx = DbServiceFactory.getTransactionManager();
 * Long orderId = tx.executeInTransaction(() -> {
 *     Long id = orderRepo.create(userId, total);
 *     itemRepo.createItems(id, items);
 *     return id;
 * });
 * }</pre>
 *
 * <h2>Поведение при исключении</h2>
 * <ul>
 *     <li>RuntimeException — автоматический rollback</li>
 *     <li>Checked Exception — commit (если не указано иное)</li>
 *     <li>{@link #rollbackFor()} — явное указание исключений для rollback</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ReadOnly
 * @see com.company.hex.db.connection.TransactionManager
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

    /**
     * Стратегия распространения транзакции.
     *
     * <p>Определяет, как метод взаимодействует с существующей транзакцией.</p>
     *
     * @return стратегия распространения
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * Уровень изоляции транзакции.
     *
     * <p>По умолчанию используется уровень изоляции по умолчанию СУБД.</p>
     *
     * @return уровень изоляции
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * Транзакция только для чтения.
     *
     * <p>Оптимизация для операций чтения. Эквивалент {@link ReadOnly}.</p>
     *
     * @return {@code true} для read-only транзакции
     */
    boolean readOnly() default false;

    /**
     * Таймаут транзакции в секундах.
     *
     * <p>Значение -1 означает использование таймаута по умолчанию.</p>
     *
     * @return таймаут в секундах
     */
    int timeout() default -1;

    /**
     * Типы исключений, при которых транзакция откатывается.
     *
     * <p>По умолчанию rollback происходит для RuntimeException и Error.</p>
     *
     * @return классы исключений для rollback
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * Типы исключений, при которых транзакция НЕ откатывается.
     *
     * <p>Позволяет указать исключения, при которых commit должен
     * произойти несмотря на ошибку.</p>
     *
     * @return классы исключений для commit
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * Стратегии распространения транзакций.
     */
    enum Propagation {
        /**
         * Использовать существующую транзакцию или создать новую.
         */
        REQUIRED,

        /**
         * Всегда создавать новую транзакцию, приостановив существующую.
         */
        REQUIRES_NEW,

        /**
         * Использовать существующую транзакцию или выполнить без транзакции.
         */
        SUPPORTS,

        /**
         * Выполнить без транзакции, приостановив существующую.
         */
        NOT_SUPPORTED,

        /**
         * Использовать существующую транзакцию или выбросить исключение.
         */
        MANDATORY,

        /**
         * Выбросить исключение, если транзакция существует.
         */
        NEVER,

        /**
         * Создать вложенную транзакцию через savepoint.
         */
        NESTED
    }

    /**
     * Уровни изоляции транзакций.
     */
    enum Isolation {
        /**
         * Использовать уровень изоляции по умолчанию СУБД.
         */
        DEFAULT,

        /**
         * Минимальная изоляция. Допускает dirty reads.
         */
        READ_UNCOMMITTED,

        /**
         * Гарантирует чтение только зафиксированных данных.
         */
        READ_COMMITTED,

        /**
         * Повторяемое чтение. Гарантирует стабильность данных в транзакции.
         */
        REPEATABLE_READ,

        /**
         * Максимальная изоляция. Полная сериализация транзакций.
         */
        SERIALIZABLE
    }
}
