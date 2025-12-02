/**
 * Пакет с аннотациями для определения SQL запросов.
 *
 * <p>Содержит аннотации для декларативного определения SQL операций:</p>
 *
 * <ul>
 *     <li>{@link com.company.hex.db.annotations.query.Select @Select} — SELECT запросы</li>
 *     <li>{@link com.company.hex.db.annotations.query.Insert @Insert} — INSERT запросы</li>
 *     <li>{@link com.company.hex.db.annotations.query.Update @Update} — UPDATE запросы</li>
 *     <li>{@link com.company.hex.db.annotations.query.Delete @Delete} — DELETE запросы</li>
 *     <li>{@link com.company.hex.db.annotations.query.ReturnGeneratedKeys @ReturnGeneratedKeys} — возврат сгенерированных ключей</li>
 *     <li>{@link com.company.hex.db.annotations.query.Script @Script} — выполнение SQL скриптов из файлов</li>
 *     <li>{@link com.company.hex.db.annotations.query.DangerousQuery @DangerousQuery} — подтверждение опасных операций</li>
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
