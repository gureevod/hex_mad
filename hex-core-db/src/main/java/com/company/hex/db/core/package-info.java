/**
 * Core-интерфейсы для работы с базой данных.
 * 
 * <p>Содержит основные абстракции:
 * <ul>
 *   <li>{@link com.company.hex.db.core.Row} — представление строки результата</li>
 *   <li>{@link com.company.hex.db.core.RowMapper} — функция маппинга строки в объект</li>
 *   <li>{@link com.company.hex.db.core.Query} — описание запроса</li>
 *   <li>{@link com.company.hex.db.core.QueryResult} — результат выполнения запроса</li>
 *   <li>{@link com.company.hex.db.core.QueryExecutor} — исполнитель запросов</li>
 *   <li>{@link com.company.hex.db.core.QueryInterceptor} — интерцептор запросов</li>
 * </ul>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
package com.company.hex.db.core;
