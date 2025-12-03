package com.company.hex.db.mapping;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Интерфейс для маппинга ResultSet на Java объекты.
 *
 * <p>ResultMapper отвечает за преобразование результатов SQL запросов
 * в типизированные Java объекты. Поддерживает автоматический маппинг
 * на основе аннотаций {@code @Column} и кастомный маппинг через
 * {@link RowMapper}.</p>
 *
 * <h2>Поддерживаемые типы возврата</h2>
 * <ul>
 *     <li>Скалярные типы: {@code Long}, {@code Integer}, {@code String} и т.д.</li>
 *     <li>Entity классы с аннотацией {@code @Table}</li>
 *     <li>Java Records (Java 16+)</li>
 *     <li>{@code Optional<T>} для nullable результатов</li>
 *     <li>{@code List<T>} для множественных результатов</li>
 *     <li>{@code Set<T>} для уникальных результатов</li>
 *     <li>{@code Stream<T>} для ленивой обработки</li>
 * </ul>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Получение маппера
 * ResultMapper mapper = ResultMapperFactory.create();
 *
 * // Маппинг одного объекта
 * UserEntity user = mapper.mapSingle(resultSet, UserEntity.class);
 *
 * // Маппинг списка
 * List<UserEntity> users = mapper.mapList(resultSet, UserEntity.class);
 *
 * // Маппинг с Optional
 * Optional<UserEntity> user = mapper.mapOptional(resultSet, UserEntity.class);
 *
 * // Скалярный маппинг
 * Long count = mapper.mapScalar(resultSet, Long.class);
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see RowMapper
 * @see DefaultResultMapper
 */
public interface ResultMapper {

    /**
     * Маппит первую строку ResultSet на объект указанного типа.
     *
     * <p>Если ResultSet пуст, возвращает {@code null}.
     * Если ResultSet содержит несколько строк, маппится только первая.</p>
     *
     * @param <T> тип результата
     * @param rs ResultSet для маппинга
     * @param targetClass класс целевого объекта
     * @return объект указанного типа или {@code null}
     * @throws SQLException при ошибке чтения из ResultSet
     * @throws com.company.hex.db.exception.MappingException при ошибке маппинга
     */
    <T> T mapSingle(ResultSet rs, Class<T> targetClass) throws SQLException;

    /**
     * Маппит первую строку ResultSet на объект с учётом generic типа.
     *
     * @param <T> тип результата
     * @param rs ResultSet для маппинга
     * @param targetClass класс целевого объекта
     * @param genericType полный generic тип (для параметризованных типов)
     * @return объект указанного типа или {@code null}
     * @throws SQLException при ошибке чтения из ResultSet
     * @throws com.company.hex.db.exception.MappingException при ошибке маппинга
     */
    <T> T mapSingle(ResultSet rs, Class<T> targetClass, Type genericType) throws SQLException;

    /**
     * Маппит первую строку ResultSet на Optional.
     *
     * <p>Если ResultSet пуст, возвращает {@code Optional.empty()}.</p>
     *
     * @param <T> тип результата
     * @param rs ResultSet для маппинга
     * @param targetClass класс целевого объекта
     * @return Optional с результатом
     * @throws SQLException при ошибке чтения из ResultSet
     * @throws com.company.hex.db.exception.MappingException при ошибке маппинга
     */
    <T> Optional<T> mapOptional(ResultSet rs, Class<T> targetClass) throws SQLException;

    /**
     * Маппит все строки ResultSet на список объектов.
     *
     * <p>Если ResultSet пуст, возвращает пустой список.</p>
     *
     * @param <T> тип элементов
     * @param rs ResultSet для маппинга
     * @param targetClass класс целевого объекта
     * @return список объектов (может быть пустым)
     * @throws SQLException при ошибке чтения из ResultSet
     * @throws com.company.hex.db.exception.MappingException при ошибке маппинга
     */
    <T> List<T> mapList(ResultSet rs, Class<T> targetClass) throws SQLException;

    /**
     * Маппит все строки ResultSet на Set объектов.
     *
     * <p>Дубликаты будут удалены (зависит от equals/hashCode целевого класса).
     * Если ResultSet пуст, возвращает пустой Set.</p>
     *
     * @param <T> тип элементов
     * @param rs ResultSet для маппинга
     * @param targetClass класс целевого объекта
     * @return Set объектов (может быть пустым)
     * @throws SQLException при ошибке чтения из ResultSet
     * @throws com.company.hex.db.exception.MappingException при ошибке маппинга
     */
    <T> Set<T> mapSet(ResultSet rs, Class<T> targetClass) throws SQLException;

    /**
     * Маппит все строки ResultSet на Stream объектов.
     *
     * <p><b>Важно:</b> Stream лениво читает ResultSet.
     * Не закрывайте ResultSet до завершения работы со Stream.</p>
     *
     * @param <T> тип элементов
     * @param rs ResultSet для маппинга
     * @param targetClass класс целевого объекта
     * @return Stream объектов
     * @throws SQLException при ошибке чтения из ResultSet
     * @throws com.company.hex.db.exception.MappingException при ошибке маппинга
     */
    <T> Stream<T> mapStream(ResultSet rs, Class<T> targetClass) throws SQLException;

    /**
     * Маппит первую строку ResultSet на скалярное значение.
     *
     * <p>Используется для запросов, возвращающих одно значение:
     * COUNT(*), MAX(), MIN() и т.д.</p>
     *
     * @param <T> тип результата
     * @param rs ResultSet для маппинга
     * @param targetClass класс результата (Long, Integer, String, etc.)
     * @return скалярное значение или {@code null}
     * @throws SQLException при ошибке чтения из ResultSet
     * @throws com.company.hex.db.exception.MappingException при ошибке маппинга
     */
    <T> T mapScalar(ResultSet rs, Class<T> targetClass) throws SQLException;

    /**
     * Маппит все строки ResultSet на список скалярных значений.
     *
     * <p>Используется для запросов, возвращающих один столбец:
     * SELECT id FROM ..., SELECT DISTINCT email FROM ... и т.д.</p>
     *
     * @param <T> тип результата
     * @param rs ResultSet для маппинга
     * @param targetClass класс результата (Long, Integer, String, etc.)
     * @return список скалярных значений (может быть пустым)
     * @throws SQLException при ошибке чтения из ResultSet
     * @throws com.company.hex.db.exception.MappingException при ошибке маппинга
     */
    <T> List<T> mapScalarList(ResultSet rs, Class<T> targetClass) throws SQLException;

    /**
     * Маппит ResultSet с использованием кастомного RowMapper.
     *
     * @param <T> тип результата
     * @param rs ResultSet для маппинга
     * @param rowMapper кастомный маппер
     * @return список объектов
     * @throws SQLException при ошибке чтения из ResultSet
     */
    <T> List<T> mapWithRowMapper(ResultSet rs, RowMapper<T> rowMapper) throws SQLException;

    /**
     * Маппит первую строку ResultSet с использованием кастомного RowMapper.
     *
     * @param <T> тип результата
     * @param rs ResultSet для маппинга
     * @param rowMapper кастомный маппер
     * @return объект или {@code null}
     * @throws SQLException при ошибке чтения из ResultSet
     */
    <T> T mapSingleWithRowMapper(ResultSet rs, RowMapper<T> rowMapper) throws SQLException;

    /**
     * Проверяет, является ли тип скалярным (примитивным или wrapper).
     *
     * @param type класс для проверки
     * @return {@code true} если тип скалярный
     */
    boolean isScalarType(Class<?> type);
}
