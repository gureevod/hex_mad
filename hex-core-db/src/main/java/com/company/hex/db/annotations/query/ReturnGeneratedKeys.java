package com.company.hex.db.annotations.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, что метод должен вернуть сгенерированные базой данных ключи.
 *
 * <p>Используется совместно с аннотацией {@link Insert} для получения
 * автоматически сгенерированных значений (обычно первичных ключей)
 * после выполнения INSERT запроса.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Возврат сгенерированного ID
 * @Insert("INSERT INTO users (name, email) VALUES (:name, :email)")
 * @ReturnGeneratedKeys
 * Long create(@Param("name") String name, @Param("email") String email);
 *
 * // Возврат нескольких сгенерированных ключей (PostgreSQL RETURNING)
 * @Insert("INSERT INTO users (name) VALUES (:name)")
 * @ReturnGeneratedKeys(columns = {"id", "created_at"})
 * Map<String, Object> createWithMetadata(@Param("name") String name);
 *
 * // Batch insert с возвратом ключей
 * @Insert("INSERT INTO users (name) VALUES (:name)")
 * @ReturnGeneratedKeys
 * List<Long> createBatch(@ParamList("name") List<String> names);
 * }</pre>
 *
 * <h2>Поддерживаемые возвращаемые типы</h2>
 * <ul>
 *     <li>{@code Long}, {@code Integer} — для одиночного первичного ключа</li>
 *     <li>{@code List<Long>}, {@code List<Integer>} — для batch операций</li>
 *     <li>{@code Map<String, Object>} — для нескольких сгенерированных колонок</li>
 * </ul>
 *
 * <h2>Совместимость с базами данных</h2>
 * <ul>
 *     <li>PostgreSQL — полная поддержка через RETURNING</li>
 *     <li>MySQL — поддержка через LAST_INSERT_ID</li>
 *     <li>H2 — полная поддержка</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Insert
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReturnGeneratedKeys {

    /**
     * Имена колонок для получения сгенерированных значений.
     *
     * <p>Если не указано, возвращается первичный ключ по умолчанию.
     * Указание конкретных колонок полезно для получения нескольких
     * сгенерированных значений (например, id и created_at).</p>
     *
     * @return массив имён колонок для получения
     */
    String[] columns() default {};
}
