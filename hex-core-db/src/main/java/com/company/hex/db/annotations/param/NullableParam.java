package com.company.hex.db.annotations.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, что параметр может принимать значение null.
 *
 * <p>По умолчанию, передача {@code null} в обычный {@link Param} вызывает
 * предупреждение валидатора. Аннотация {@code @NullableParam} явно указывает,
 * что {@code null} является допустимым значением для данного параметра.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Опциональный фильтр по имени
 * @Select("""
 *     SELECT * FROM users
 *     WHERE (:name IS NULL OR name = :name)
 *       AND (:email IS NULL OR email = :email)
 *     """)
 * List<User> findByFilters(@NullableParam("name") String name,
 *                          @NullableParam("email") String email);
 *
 * // Обновление с опциональным полем
 * @Update("""
 *     UPDATE users SET
 *         name = COALESCE(:name, name),
 *         email = COALESCE(:email, email)
 *     WHERE id = :id
 *     """)
 * int updateUser(@Param("id") Long id,
 *                @NullableParam("name") String name,
 *                @NullableParam("email") String email);
 *
 * // Вставка с nullable полем
 * @Insert("INSERT INTO users (name, nickname) VALUES (:name, :nickname)")
 * @ReturnGeneratedKeys
 * Long create(@Param("name") String name, @NullableParam("nickname") String nickname);
 * }</pre>
 *
 * <h2>Поведение при null</h2>
 * <ul>
 *     <li>Значение {@code null} передаётся в PreparedStatement как {@code setNull()}</li>
 *     <li>Используется {@link java.sql.Types#NULL} или указанный {@link #sqlType()}</li>
 *     <li>Валидатор не выдаёт предупреждение о nullable значении</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Param
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NullableParam {

    /**
     * Имя параметра в SQL запросе.
     *
     * <p>Должно соответствовать плейсхолдеру {@code :paramName} в SQL.</p>
     *
     * @return имя параметра
     */
    String value();

    /**
     * SQL тип для null значения.
     *
     * <p>Указывает тип из {@link java.sql.Types} для использования
     * при вызове {@code setNull()}. По умолчанию используется
     * {@link java.sql.Types#NULL}, что подходит для большинства случаев.</p>
     *
     * <p>Пример указания типа:</p>
     * <pre>{@code
     * @NullableParam(value = "date", sqlType = Types.DATE)
     * LocalDate date
     * }</pre>
     *
     * @return SQL тип из {@link java.sql.Types}
     */
    int sqlType() default java.sql.Types.NULL;
}
