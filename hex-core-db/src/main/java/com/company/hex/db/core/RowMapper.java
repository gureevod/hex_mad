package com.company.hex.db.core;

import java.util.Map;

/**
 * Функция преобразования Row в объект.
 * 
 * <p>Основная точка расширения для кастомного маппинга результатов запросов.
 * Является функциональным интерфейсом, поэтому можно использовать лямбда-выражения.
 *
 * <p>Пример использования:
 * <pre>{@code
 * RowMapper<User> userMapper = row -> new User(
 *     row.getLong("id"),
 *     row.getString("name"),
 *     row.getString("email")
 * );
 * 
 * List<User> users = queryResult.toList(userMapper);
 * }</pre>
 *
 * @param <T> тип результата маппинга
 * @author hex-core-db
 * @since 1.0.0
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Преобразовать строку результата в объект.
     *
     * @param row строка результата запроса
     * @return преобразованный объект
     */
    T map(Row row);

    /**
     * Встроенный маппер, преобразующий Row в Map.
     *
     * @return маппер, возвращающий Map с данными строки
     */
    static RowMapper<Map<String, Object>> toMap() {
        return Row::toMap;
    }
}
