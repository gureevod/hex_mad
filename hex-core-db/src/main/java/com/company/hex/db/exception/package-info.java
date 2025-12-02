/**
 * Пакет с иерархией исключений модуля hex-core-db.
 *
 * <p>Предоставляет типизированные исключения для всех ошибок работы с БД:</p>
 *
 * <ul>
 *     <li>{@code DbException} — базовое исключение</li>
 *     <li>{@code QueryValidationException} — ошибки валидации SQL</li>
 *     <li>{@code ConnectionException} — ошибки подключения</li>
 *     <li>{@code QueryExecutionException} — ошибки выполнения</li>
 *     <li>{@code MappingException} — ошибки маппинга результатов</li>
 *     <li>{@code TransactionException} — ошибки транзакций</li>
 * </ul>
 *
 * <h2>Иерархия constraint исключений</h2>
 * <pre>
 * QueryExecutionException
 *   ├── QueryTimeoutException
 *   └── ConstraintViolationException
 *       ├── UniqueConstraintException
 *       ├── ForeignKeyException
 *       └── NotNullException
 * </pre>
 *
 * @since 1.0.0
 */
package com.company.hex.db.exception;
