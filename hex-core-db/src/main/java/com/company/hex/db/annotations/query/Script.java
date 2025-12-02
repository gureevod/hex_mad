package com.company.hex.db.annotations.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для выполнения SQL скрипта из файла ресурсов.
 *
 * <p>Используется для выполнения SQL скриптов, содержащих несколько SQL
 * операторов, из файлов в classpath. Полезно для инициализации тестовых
 * данных, миграций или сложных операций очистки.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Выполнение скрипта из classpath
 * @Script(resource = "db/cleanup.sql")
 * void cleanup();
 *
 * // Скрипт с параметрами
 * @Script(resource = "db/init-user.sql")
 * void initUser(@Param("userId") Long userId);
 *
 * // Скрипт для создания тестовых данных
 * @Script(resource = "db/test-data/users.sql")
 * int insertTestUsers();
 *
 * // Скрипт с кастомным разделителем
 * @Script(resource = "db/procedures.sql", separator = ";;")
 * void createProcedures();
 * }</pre>
 *
 * <h2>Формат файла скрипта</h2>
 * <ul>
 *     <li>SQL операторы разделяются точкой с запятой (по умолчанию)</li>
 *     <li>Комментарии поддерживаются: {@code --} и {@code /* ... * /}</li>
 *     <li>Параметры указываются в формате {@code :paramName}</li>
 * </ul>
 *
 * <h2>Возвращаемые типы</h2>
 * <ul>
 *     <li>{@code void} — результат не возвращается</li>
 *     <li>{@code int} — общее количество затронутых строк</li>
 *     <li>{@code int[]} — количество затронутых строк для каждого оператора</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Param
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Script {

    /**
     * Путь к файлу SQL скрипта в classpath.
     *
     * <p>Путь относительно корня classpath. Например:
     * {@code "db/cleanup.sql"} соответствует файлу
     * {@code src/main/resources/db/cleanup.sql}.</p>
     *
     * @return путь к файлу скрипта
     */
    String resource();

    /**
     * Разделитель между SQL операторами в скрипте.
     *
     * <p>По умолчанию используется точка с запятой ({@code ;}).
     * Можно изменить для скриптов с хранимыми процедурами,
     * которые содержат точки с запятой внутри тела процедуры.</p>
     *
     * @return разделитель операторов
     */
    String separator() default ";";

    /**
     * Продолжать выполнение при ошибке.
     *
     * <p>Если {@code true}, выполнение продолжится даже при ошибке
     * в одном из операторов. Ошибки будут залогированы.
     * Если {@code false} (по умолчанию), выполнение прервётся
     * при первой ошибке.</p>
     *
     * @return продолжать ли при ошибке
     */
    boolean continueOnError() default false;
}
