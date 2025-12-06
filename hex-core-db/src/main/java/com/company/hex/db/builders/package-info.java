/**
 * SQL-билдеры для построения типобезопасных запросов.
 * 
 * <p>Этот пакет предоставляет fluent API для построения SQL-запросов
 * без написания сырых SQL-строк. Главная точка входа — {@link com.company.hex.db.builders.SQL}.
 *
 * <p><b>Потокобезопасность:</b> Билдеры НЕ потокобезопасны. Создавайте новый экземпляр
 * билдера в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * Query query = SQL.select("id", "name")
 *     .from("users")
 *     .where("active", true)
 *     .orderBy("name")
 *     .limit(10)
 *     .build();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
package com.company.hex.db.builders;
