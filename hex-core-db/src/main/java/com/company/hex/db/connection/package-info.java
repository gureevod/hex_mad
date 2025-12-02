/**
 * Пакет с компонентами управления соединениями.
 *
 * <p>Содержит классы для управления подключениями к БД:</p>
 *
 * <ul>
 *     <li>{@code ConnectionProvider} — управление пулами соединений через HikariCP</li>
 *     <li>{@code TransactionManager} — управление транзакциями</li>
 *     <li>Поддержка множественных DataSource</li>
 *     <li>ThreadLocal изоляция для параллельных тестов</li>
 * </ul>
 *
 * <h2>Пример использования TransactionManager</h2>
 * <pre>{@code
 * TransactionManager tx = DbServiceFactory.getTransactionManager();
 * tx.executeInTransaction(() -> {
 *     orderRepo.create(1L, BigDecimal.TEN);
 *     itemRepo.addItem(orderId, productId, 2);
 *     return orderId;
 * });
 * }</pre>
 *
 * @since 1.0.0
 */
package com.company.hex.db.connection;
