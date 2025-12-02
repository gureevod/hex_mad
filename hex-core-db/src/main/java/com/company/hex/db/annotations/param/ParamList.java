package com.company.hex.db.annotations.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Связывает коллекцию параметров с IN clause в SQL запросе.
 *
 * <p>Используется для передачи списка значений в SQL конструкцию {@code IN (...)}.
 * Фреймворк автоматически разворачивает коллекцию в нужное количество
 * плейсхолдеров.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Простой IN clause с List
 * @Select("SELECT * FROM users WHERE id IN (:ids)")
 * List<User> findByIds(@ParamList("ids") List<Long> ids);
 *
 * // IN clause с Set
 * @Select("SELECT * FROM users WHERE status IN (:statuses)")
 * List<User> findByStatuses(@ParamList("statuses") Set<String> statuses);
 *
 * // Комбинация с обычными параметрами
 * @Select("SELECT * FROM users WHERE active = :active AND role IN (:roles)")
 * List<User> findActiveByRoles(@Param("active") boolean active,
 *                               @ParamList("roles") List<String> roles);
 *
 * // DELETE с IN clause
 * @Delete("DELETE FROM users WHERE id IN (:ids)")
 * int deleteByIds(@ParamList("ids") List<Long> ids);
 * }</pre>
 *
 * <h2>Как это работает</h2>
 * <p>SQL запрос {@code "SELECT * FROM users WHERE id IN (:ids)"}
 * с параметром {@code List.of(1L, 2L, 3L)} будет преобразован в:
 * {@code "SELECT * FROM users WHERE id IN (?, ?, ?)"}
 * с подстановкой значений {@code [1, 2, 3]}.</p>
 *
 * <h2>Поддерживаемые типы коллекций</h2>
 * <ul>
 *     <li>{@code List<T>}</li>
 *     <li>{@code Set<T>}</li>
 *     <li>{@code Collection<T>}</li>
 *     <li>Массивы: {@code Long[]}, {@code String[]}, etc.</li>
 * </ul>
 *
 * <h2>Ограничения</h2>
 * <ul>
 *     <li>Пустая коллекция вызовет {@code QueryExecutionException}</li>
 *     <li>Максимальный размер ограничен возможностями СУБД (обычно ~1000 элементов)</li>
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
public @interface ParamList {

    /**
     * Имя параметра в SQL запросе.
     *
     * <p>Должно соответствовать плейсхолдеру {@code :paramName}
     * внутри конструкции {@code IN (:paramName)} в SQL.</p>
     *
     * @return имя параметра
     */
    String value();
}
