package com.company.hex.db.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Процессор для обработки именованных параметров в SQL запросах.
 *
 * <p>Преобразует SQL с именованными параметрами (формат {@code :paramName})
 * в SQL с позиционными параметрами ({@code ?}) и создаёт маппинг
 * для подстановки значений.</p>
 *
 * <h2>Пример преобразования</h2>
 * <pre>{@code
 * // Входной SQL
 * String sql = "SELECT * FROM users WHERE id = :id AND name = :name";
 * Map<String, Object> params = Map.of("id", 42L, "name", "John");
 *
 * // Обработка
 * NamedParameterProcessor processor = new NamedParameterProcessor();
 * ProcessedQuery result = processor.process(sql, params);
 *
 * // Результат
 * // result.getSql() → "SELECT * FROM users WHERE id = ? AND name = ?"
 * // result.getPositionalParameters() → [42L, "John"]
 * }</pre>
 *
 * <h2>Поддерживаемые форматы</h2>
 * <ul>
 *     <li>{@code :paramName} — стандартный формат именованного параметра</li>
 *     <li>{@code :param_name} — с подчёркиванием</li>
 *     <li>{@code :param123} — с цифрами</li>
 * </ul>
 *
 * <h2>Особые случаи</h2>
 * <ul>
 *     <li>Строки в кавычках игнорируются (не обрабатываются как параметры)</li>
 *     <li>PostgreSQL cast ({@code ::type}) не путается с параметрами</li>
 *     <li>Один параметр может использоваться несколько раз в запросе</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ProcessedQuery
 * @see QueryExecutor
 */
public final class NamedParameterProcessor {

    /**
     * Регулярное выражение для поиска именованных параметров.
     * Игнорирует двойное двоеточие (PostgreSQL cast) и параметры в строках.
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile(
        "(?<![:\\w]):(\\w+)(?!\\w)"
    );

    /**
     * Паттерн для определения строковых литералов.
     */
    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile(
        "'(?:[^'\\\\]|\\\\.)*'|\"(?:[^\"\\\\]|\\\\.)*\""
    );

    /**
     * Создаёт новый экземпляр процессора.
     */
    public NamedParameterProcessor() {
    }

    /**
     * Обрабатывает SQL запрос с именованными параметрами.
     *
     * <p>Преобразует именованные параметры в позиционные и создаёт
     * массив значений для PreparedStatement.</p>
     *
     * @param sql SQL запрос с именованными параметрами
     * @param namedParameters карта имя → значение параметров
     * @return обработанный запрос с позиционными параметрами
     * @throws IllegalArgumentException если параметр не найден в карте
     */
    public ProcessedQuery process(String sql, Map<String, Object> namedParameters) {
        Objects.requireNonNull(sql, "SQL cannot be null");

        if (namedParameters == null) {
            namedParameters = Collections.emptyMap();
        }

        // Находим все строковые литералы для исключения
        Set<int[]> stringRanges = findStringLiteralRanges(sql);

        // Находим все именованные параметры
        List<ParameterInfo> parameterInfos = findParameters(sql, stringRanges);

        if (parameterInfos.isEmpty()) {
            // Нет именованных параметров
            return new ProcessedQuery(sql, Collections.emptyList(), 
                Collections.emptySet(), Collections.emptyMap());
        }

        // Преобразуем SQL и собираем позиционные параметры
        return buildProcessedQuery(sql, parameterInfos, namedParameters);
    }

    /**
     * Извлекает имена всех параметров из SQL запроса.
     *
     * @param sql SQL запрос
     * @return набор имён параметров
     */
    public Set<String> extractParameterNames(String sql) {
        Objects.requireNonNull(sql, "SQL cannot be null");

        Set<int[]> stringRanges = findStringLiteralRanges(sql);
        List<ParameterInfo> parameterInfos = findParameters(sql, stringRanges);

        Set<String> names = new HashSet<>();
        for (ParameterInfo info : parameterInfos) {
            names.add(info.name);
        }
        return Collections.unmodifiableSet(names);
    }

    /**
     * Находит диапазоны строковых литералов в SQL.
     */
    private Set<int[]> findStringLiteralRanges(String sql) {
        Set<int[]> ranges = new HashSet<>();
        Matcher matcher = STRING_LITERAL_PATTERN.matcher(sql);
        while (matcher.find()) {
            ranges.add(new int[]{matcher.start(), matcher.end()});
        }
        return ranges;
    }

    /**
     * Проверяет, находится ли позиция внутри строкового литерала.
     */
    private boolean isInsideStringLiteral(int position, Set<int[]> stringRanges) {
        for (int[] range : stringRanges) {
            if (position >= range[0] && position < range[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Находит все именованные параметры в SQL.
     */
    private List<ParameterInfo> findParameters(String sql, Set<int[]> stringRanges) {
        List<ParameterInfo> parameters = new ArrayList<>();
        Matcher matcher = PARAM_PATTERN.matcher(sql);

        while (matcher.find()) {
            int startPos = matcher.start();
            
            // Пропускаем параметры внутри строковых литералов
            if (isInsideStringLiteral(startPos, stringRanges)) {
                continue;
            }

            // Проверяем на PostgreSQL cast (::)
            if (startPos > 0 && sql.charAt(startPos - 1) == ':') {
                continue;
            }

            String paramName = matcher.group(1);
            parameters.add(new ParameterInfo(paramName, startPos, matcher.end()));
        }

        return parameters;
    }

    /**
     * Строит обработанный запрос с позиционными параметрами.
     */
    private ProcessedQuery buildProcessedQuery(String sql, List<ParameterInfo> parameterInfos,
                                                Map<String, Object> namedParameters) {
        StringBuilder processedSql = new StringBuilder();
        List<Object> positionalParameters = new ArrayList<>();
        Map<String, List<Integer>> parameterPositions = new LinkedHashMap<>();
        Set<String> foundParameters = new HashSet<>();

        int lastEnd = 0;

        for (int i = 0; i < parameterInfos.size(); i++) {
            ParameterInfo info = parameterInfos.get(i);

            // Добавляем SQL до текущего параметра
            processedSql.append(sql, lastEnd, info.start);

            // Заменяем параметр на ?
            processedSql.append('?');

            // Получаем значение параметра
            if (!namedParameters.containsKey(info.name)) {
                throw new IllegalArgumentException(
                    "Parameter '" + info.name + "' not found in parameters map. "
                    + "Available parameters: " + namedParameters.keySet());
            }

            Object value = namedParameters.get(info.name);
            positionalParameters.add(value);
            foundParameters.add(info.name);

            // Отслеживаем позиции параметра (1-based)
            parameterPositions
                .computeIfAbsent(info.name, k -> new ArrayList<>())
                .add(positionalParameters.size());

            lastEnd = info.end;
        }

        // Добавляем оставшуюся часть SQL
        processedSql.append(sql.substring(lastEnd));

        return new ProcessedQuery(
            processedSql.toString(),
            positionalParameters,
            foundParameters,
            parameterPositions
        );
    }

    /**
     * Информация о найденном параметре.
     */
    private static class ParameterInfo {
        final String name;
        final int start;
        final int end;

        ParameterInfo(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Результат обработки SQL запроса с именованными параметрами.
     *
     * <p>Содержит преобразованный SQL с позиционными параметрами ({@code ?})
     * и упорядоченный список значений для подстановки.</p>
     */
    public static final class ProcessedQuery {

        private final String sql;
        private final List<Object> positionalParameters;
        private final Set<String> parameterNames;
        private final Map<String, List<Integer>> parameterPositions;

        /**
         * Создаёт результат обработки.
         *
         * @param sql SQL с позиционными параметрами
         * @param positionalParameters упорядоченный список значений
         * @param parameterNames набор имён найденных параметров
         * @param parameterPositions карта имя → позиции в списке
         */
        public ProcessedQuery(String sql, List<Object> positionalParameters,
                              Set<String> parameterNames,
                              Map<String, List<Integer>> parameterPositions) {
            this.sql = sql;
            this.positionalParameters = Collections.unmodifiableList(
                new ArrayList<>(positionalParameters));
            this.parameterNames = Collections.unmodifiableSet(
                new HashSet<>(parameterNames));
            this.parameterPositions = Collections.unmodifiableMap(parameterPositions);
        }

        /**
         * SQL запрос с позиционными параметрами ({@code ?}).
         *
         * @return преобразованный SQL
         */
        public String getSql() {
            return sql;
        }

        /**
         * Упорядоченный список значений параметров.
         *
         * <p>Порядок соответствует порядку {@code ?} в SQL.</p>
         *
         * @return неизменяемый список значений
         */
        public List<Object> getPositionalParameters() {
            return positionalParameters;
        }

        /**
         * Набор имён найденных параметров.
         *
         * @return набор имён
         */
        public Set<String> getParameterNames() {
            return parameterNames;
        }

        /**
         * Карта имя параметра → список позиций (1-based).
         *
         * <p>Полезно, если один параметр используется несколько раз.</p>
         *
         * @return карта позиций
         */
        public Map<String, List<Integer>> getParameterPositions() {
            return parameterPositions;
        }

        /**
         * Количество позиционных параметров.
         *
         * @return количество параметров
         */
        public int getParameterCount() {
            return positionalParameters.size();
        }

        /**
         * Проверяет, есть ли параметры.
         *
         * @return true если есть хотя бы один параметр
         */
        public boolean hasParameters() {
            return !positionalParameters.isEmpty();
        }

        /**
         * Возвращает массив значений для PreparedStatement.
         *
         * @return массив значений
         */
        public Object[] getParametersArray() {
            return positionalParameters.toArray();
        }

        @Override
        public String toString() {
            return "ProcessedQuery{"
                + "sql='" + sql + '\''
                + ", parameters=" + positionalParameters
                + '}';
        }
    }
}
