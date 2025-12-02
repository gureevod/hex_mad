/**
 * Пакет с аннотациями параметров для SQL запросов.
 *
 * <p>Содержит аннотации для определения параметров методов репозитория:</p>
 *
 * <ul>
 *     <li>{@link com.company.hex.db.annotations.param.Param @Param} — именованный параметр запроса</li>
 *     <li>{@link com.company.hex.db.annotations.param.ParamList @ParamList} — коллекция для IN clause</li>
 *     <li>{@link com.company.hex.db.annotations.param.NullableParam @NullableParam} — nullable параметр</li>
 *     <li>{@link com.company.hex.db.annotations.param.OutParam @OutParam} — выходной параметр для stored procedures</li>
 *     <li>{@link com.company.hex.db.annotations.param.InOutParam @InOutParam} — входной-выходной параметр для stored procedures</li>
 * </ul>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * @Select("SELECT * FROM users WHERE id = :id AND status IN (:statuses)")
 * List<User> findByIdAndStatuses(
 *     @Param("id") Long id,
 *     @ParamList("statuses") List<String> statuses);
 *
 * @Select("SELECT * FROM users WHERE email = :email")
 * Optional<User> findByEmail(@NullableParam("email") String email);
 * }</pre>
 *
 * @since 1.0.0
 */
package com.company.hex.db.annotations.param;
