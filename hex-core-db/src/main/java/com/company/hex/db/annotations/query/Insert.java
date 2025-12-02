package com.company.hex.db.annotations.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для определения INSERT запроса к базе данных.
 *
 * <p>Используется для декларативного определения SQL INSERT запросов
 * в методах репозитория. Поддерживает именованные параметры в формате {@code :paramName}.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Простой INSERT
 * @Insert("INSERT INTO users (name, email) VALUES (:name, :email)")
 * int create(@Param("name") String name, @Param("email") String email);
 *
 * // INSERT с возвратом сгенерированного ключа
 * @Insert("INSERT INTO users (name, email) VALUES (:name, :email)")
 * @ReturnGeneratedKeys
 * Long createAndGetId(@Param("name") String name, @Param("email") String email);
 *
 * // INSERT с использованием entity
 * @Insert("INSERT INTO users (name, email, created_at) VALUES (:user.name, :user.email, :user.createdAt)")
 * @ReturnGeneratedKeys
 * Long create(@Param("user") User user);
 *
 * // INSERT с ON CONFLICT (PostgreSQL)
 * @Insert("""
 *     INSERT INTO users (id, name, email)
 *     VALUES (:id, :name, :email)
 *     ON CONFLICT (id) DO UPDATE SET name = :name, email = :email
 *     """)
 * int upsert(@Param("id") Long id, @Param("name") String name, @Param("email") String email);
 * }</pre>
 *
 * <h2>Возвращаемые типы</h2>
 * <ul>
 *     <li>{@code void} — результат не возвращается</li>
 *     <li>{@code int} — количество затронутых строк</li>
 *     <li>{@code Long}, {@code Integer} — с {@link ReturnGeneratedKeys}, возвращает сгенерированный ключ</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ReturnGeneratedKeys
 * @see Param
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Insert {

    /**
     * SQL INSERT запрос для выполнения.
     *
     * <p>Поддерживает именованные параметры в формате {@code :paramName},
     * которые будут заменены на соответствующие значения из аргументов метода,
     * аннотированных {@link Param}.</p>
     *
     * @return SQL INSERT запрос
     */
    String value();
}
