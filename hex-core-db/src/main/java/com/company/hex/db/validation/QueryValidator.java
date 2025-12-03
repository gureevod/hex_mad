package com.company.hex.db.validation;

import com.company.hex.db.service.QueryDefinition;

/**
 * Интерфейс для валидации SQL запросов перед выполнением.
 *
 * <p>Валидаторы проверяют запросы на корректность и безопасность
 * до отправки в базу данных. Это позволяет обнаружить ошибки
 * на раннем этапе (fail-fast).</p>
 *
 * <h2>Типы валидаций</h2>
 * <ul>
 *     <li>Синтаксические: соответствие параметров</li>
 *     <li>Безопасность: UPDATE/DELETE без WHERE</li>
 *     <li>Типы: nullable без Optional</li>
 * </ul>
 *
 * <h2>Пример реализации</h2>
 * <pre>{@code
 * public class StrictQueryValidator implements QueryValidator {
 *     @Override
 *     public void validate(QueryDefinition query) {
 *         validateParameters(query);
 *         validateDangerousOperations(query);
 *     }
 *
 *     private void validateParameters(QueryDefinition query) {
 *         Set<String> declared = query.getDeclaredParameters();
 *         Set<String> inQuery = extractParametersFromSql(query.getSql());
 *
 *         Set<String> missing = new HashSet<>(inQuery);
 *         missing.removeAll(declared);
 *
 *         if (!missing.isEmpty()) {
 *             throw new QueryValidationException(
 *                 "Missing parameters: " + missing, query);
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryDefinition
 */
public interface QueryValidator {

    /**
     * Валидирует запрос перед выполнением.
     *
     * <p>Метод должен выбросить {@code QueryValidationException}
     * если запрос невалиден.</p>
     *
     * @param query определение запроса для валидации
     * @throws QueryValidationException если запрос невалиден
     */
    void validate(QueryDefinition query);
}
