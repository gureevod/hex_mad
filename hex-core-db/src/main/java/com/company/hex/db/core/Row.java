package com.company.hex.db.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Представляет одну строку результата запроса.
 * 
 * <p>Иммутабельная обёртка над данными из ResultSet.
 * Все данные читаются в память при создании — никаких открытых ресурсов.
 * 
 * <p>Поддерживает автоматическую конвертацию типов:
 * <ul>
 *   <li>Number → Long, Integer, BigDecimal</li>
 *   <li>Number (0/1) и String ("true"/"false") → Boolean</li>
 *   <li>java.sql.Timestamp → LocalDateTime</li>
 *   <li>java.sql.Date → LocalDate</li>
 * </ul>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public interface Row {

    /**
     * Получить значение колонки с приведением типа.
     *
     * @param column имя колонки (регистронезависимо)
     * @param type   целевой тип значения
     * @param <T>    тип возвращаемого значения
     * @return значение колонки или null, если значение отсутствует
     */
    <T> T get(String column, Class<T> type);

    /**
     * Получить строковое значение колонки.
     * Эквивалент {@code get(column, String.class)}.
     *
     * @param column имя колонки (регистронезависимо)
     * @return строковое значение или null
     */
    String getString(String column);

    /**
     * Получить значение типа Long.
     * 
     * <p>Автоконвертация из Number.
     *
     * @param column имя колонки (регистронезависимо)
     * @return значение Long или null
     */
    Long getLong(String column);

    /**
     * Получить значение типа Integer.
     * 
     * <p>Автоконвертация из Number.
     *
     * @param column имя колонки (регистронезависимо)
     * @return значение Integer или null
     */
    Integer getInt(String column);

    /**
     * Получить значение типа Boolean.
     * 
     * <p>Автоконвертация из:
     * <ul>
     *   <li>Number: 0 = false, остальное = true</li>
     *   <li>String: "true"/"false" (регистронезависимо)</li>
     * </ul>
     *
     * @param column имя колонки (регистронезависимо)
     * @return значение Boolean или null
     */
    Boolean getBoolean(String column);

    /**
     * Получить значение типа BigDecimal.
     * 
     * <p>Рекомендуется для денежных значений.
     *
     * @param column имя колонки (регистронезависимо)
     * @return значение BigDecimal или null
     */
    BigDecimal getBigDecimal(String column);

    /**
     * Получить значение типа LocalDateTime.
     * 
     * <p>Автоконвертация из java.sql.Timestamp.
     *
     * @param column имя колонки (регистронезависимо)
     * @return значение LocalDateTime или null
     */
    LocalDateTime getLocalDateTime(String column);

    /**
     * Получить значение типа LocalDate.
     * 
     * <p>Автоконвертация из java.sql.Date.
     *
     * @param column имя колонки (регистронезависимо)
     * @return значение LocalDate или null
     */
    LocalDate getLocalDate(String column);

    /**
     * Конвертировать всю строку в Map.
     * 
     * <p>Имена колонок приводятся к нижнему регистру.
     *
     * @return неизменяемая карта с данными строки
     */
    Map<String, Object> toMap();
}
