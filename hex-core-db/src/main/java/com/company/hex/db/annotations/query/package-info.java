/**
 * Пакет с аннотациями для определения SQL запросов.
 *
 * <p>Содержит аннотации для декларативного определения SQL операций:</p>
 *
 * <ul>
 *     <li>{@code @Select} — SELECT запросы</li>
 *     <li>{@code @Insert} — INSERT запросы</li>
 *     <li>{@code @Update} — UPDATE запросы</li>
 *     <li>{@code @Delete} — DELETE запросы</li>
 *     <li>{@code @ReturnGeneratedKeys} — возврат сгенерированных ключей</li>
 *     <li>{@code @Script} — выполнение SQL скриптов из файлов</li>
 * </ul>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * @Select("SELECT * FROM users WHERE id = :id")
 * Optional<User> findById(@Param("id") Long id);
 *
 * @Insert("INSERT INTO users (name, email) VALUES (:name, :email)")
 * @ReturnGeneratedKeys
 * Long create(@Param("name") String name, @Param("email") String email);
 *
 * @Update("UPDATE users SET name = :name WHERE id = :id")
 * int updateName(@Param("id") Long id, @Param("name") String name);
 *
 * @Delete("DELETE FROM users WHERE id = :id")
 * int deleteById(@Param("id") Long id);
 * }</pre>
 *
 * @since 1.0.0
 */
package com.company.hex.db.annotations.query;
