package com.company.hex.db.service;

import com.company.hex.db.interceptor.ExecutionContext;

/**
 * Интерфейс для выполнения SQL запросов.
 *
 * <p>Отвечает за непосредственное выполнение SQL через JDBC
 * и маппинг результатов на Java объекты.</p>
 *
 * <h2>Ответственности</h2>
 * <ul>
 *     <li>Получение соединения из пула</li>
 *     <li>Преобразование именованных параметров в позиционные</li>
 *     <li>Выполнение PreparedStatement</li>
 *     <li>Маппинг ResultSet на объекты</li>
 *     <li>Освобождение ресурсов</li>
 * </ul>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * QueryExecutor executor = new DefaultQueryExecutor(connectionProvider, resultMapper);
 *
 * QueryDefinition query = QueryDefinition.builder()
 *     .sql("SELECT * FROM users WHERE id = :id")
 *     .parameter("id", 42L)
 *     .returnType(UserEntity.class)
 *     .methodName("findById")
 *     .queryType(QueryType.SELECT)
 *     .build();
 *
 * ExecutionContext context = new ExecutionContext();
 * Object result = executor.execute(query, context);
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryDefinition
 * @see ExecutionContext
 */
public interface QueryExecutor {

    /**
     * Выполняет запрос и возвращает результат.
     *
     * <p>Тип результата определяется на основе {@code QueryDefinition.getReturnType()}:</p>
     * <ul>
     *     <li>{@code Optional<T>} — один объект или пустой Optional</li>
     *     <li>{@code List<T>} — список объектов</li>
     *     <li>{@code T} — один объект (может быть null)</li>
     *     <li>{@code int} — количество затронутых строк</li>
     *     <li>{@code void} — без возврата</li>
     * </ul>
     *
     * @param query определение запроса
     * @param context контекст выполнения
     * @return результат выполнения (тип зависит от запроса)
     */
    Object execute(QueryDefinition query, ExecutionContext context);
}
