package com.company.hex.db.mapping;

import com.company.hex.db.annotations.mapping.Column;
import com.company.hex.db.annotations.mapping.Enumerated;
import com.company.hex.db.annotations.mapping.Id;
import com.company.hex.db.annotations.mapping.Transient;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Метаданные Entity класса для маппинга ResultSet.
 *
 * <p>Хранит информацию о полях класса, их связи с колонками БД,
 * типах и аннотациях. Используется для автоматического маппинга
 * результатов SQL запросов на Java объекты.</p>
 *
 * <h2>Кэширование</h2>
 * <p>Метаданные кэшируются для каждого класса, так как
 * их создание через reflection — дорогая операция.</p>
 *
 * <h2>Поддерживаемые типы</h2>
 * <ul>
 *     <li>Обычные POJO классы с аннотациями {@code @Column}</li>
 *     <li>Java Records (Java 16+)</li>
 *     <li>Классы без аннотаций (маппинг по имени поля)</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 */
public final class EntityMetadata {

    /**
     * Кэш метаданных для классов.
     */
    private static final ConcurrentHashMap<Class<?>, EntityMetadata> CACHE =
        new ConcurrentHashMap<>();

    /**
     * Целевой класс.
     */
    private final Class<?> entityClass;

    /**
     * Является ли класс Record.
     */
    private final boolean isRecord;

    /**
     * Список метаданных полей (в порядке объявления).
     */
    private final List<FieldMetadata> fields;

    /**
     * Маппинг имя колонки (lower case) -> метаданные поля.
     */
    private final Map<String, FieldMetadata> columnToField;

    /**
     * Конструктор для Record (или default constructor для POJO).
     */
    private final Constructor<?> constructor;

    /**
     * Метаданные поля с @Id аннотацией (может быть null).
     */
    private final FieldMetadata idField;

    private EntityMetadata(Class<?> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.isRecord = entityClass.isRecord();
        this.fields = new ArrayList<>();
        this.columnToField = new LinkedHashMap<>();

        if (isRecord) {
            parseRecordComponents();
            this.constructor = findRecordConstructor();
        } else {
            parseClassFields();
            this.constructor = findDefaultConstructor();
        }

        // Находим ID поле
        this.idField = fields.stream()
            .filter(FieldMetadata::isId)
            .findFirst()
            .orElse(null);
    }

    /**
     * Получает или создаёт метаданные для указанного класса.
     *
     * @param entityClass класс для анализа
     * @return метаданные класса
     */
    public static EntityMetadata forClass(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    /**
     * Очищает кэш метаданных.
     * Полезно для тестов.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Парсит компоненты Record класса.
     */
    private void parseRecordComponents() {
        RecordComponent[] components = entityClass.getRecordComponents();
        for (RecordComponent component : components) {
            FieldMetadata meta = createFieldMetadata(component);
            fields.add(meta);
            registerColumnMapping(meta);
        }
    }

    /**
     * Парсит поля обычного класса.
     */
    private void parseClassFields() {
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                // Пропускаем static, transient и @Transient поля
                int modifiers = field.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(modifiers)) {
                    continue;
                }
                if (java.lang.reflect.Modifier.isTransient(modifiers)) {
                    continue;
                }
                if (field.isAnnotationPresent(Transient.class)) {
                    continue;
                }

                FieldMetadata meta = createFieldMetadata(field);
                fields.add(meta);
                registerColumnMapping(meta);
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * Создаёт метаданные для RecordComponent.
     */
    private FieldMetadata createFieldMetadata(RecordComponent component) {
        String fieldName = component.getName();
        Class<?> fieldType = component.getType();

        // Определяем имя колонки
        String columnName = determineColumnName(component);

        // Проверяем аннотации
        boolean isId = component.isAnnotationPresent(Id.class);
        Enumerated enumerated = component.getAnnotation(Enumerated.class);

        return new FieldMetadata(
            fieldName,
            columnName,
            fieldType,
            null, // Record не имеет Field
            component,
            isId,
            enumerated
        );
    }

    /**
     * Создаёт метаданные для Field.
     */
    private FieldMetadata createFieldMetadata(Field field) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        // Определяем имя колонки
        String columnName = determineColumnName(field);

        // Проверяем аннотации
        boolean isId = field.isAnnotationPresent(Id.class);
        Enumerated enumerated = field.getAnnotation(Enumerated.class);

        return new FieldMetadata(
            fieldName,
            columnName,
            fieldType,
            field,
            null,
            isId,
            enumerated
        );
    }

    /**
     * Определяет имя колонки из аннотации @Column или имени поля.
     */
    private String determineColumnName(RecordComponent component) {
        Column column = component.getAnnotation(Column.class);
        if (column != null) {
            String name = column.value().isEmpty() ? column.name() : column.value();
            if (!name.isEmpty()) {
                return name;
            }
        }
        // Конвертируем camelCase в snake_case
        return camelToSnake(component.getName());
    }

    /**
     * Определяет имя колонки из аннотации @Column или имени поля.
     */
    private String determineColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            String name = column.value().isEmpty() ? column.name() : column.value();
            if (!name.isEmpty()) {
                return name;
            }
        }
        // Конвертируем camelCase в snake_case
        return camelToSnake(field.getName());
    }

    /**
     * Регистрирует маппинг колонка -> поле.
     */
    private void registerColumnMapping(FieldMetadata meta) {
        // Регистрируем по имени колонки (lower case)
        columnToField.put(meta.getColumnName().toLowerCase(), meta);
        // Также регистрируем по имени поля для fallback
        columnToField.put(meta.getFieldName().toLowerCase(), meta);
    }

    /**
     * Находит конструктор Record с параметрами.
     */
    private Constructor<?> findRecordConstructor() {
        try {
            Class<?>[] paramTypes = fields.stream()
                .map(FieldMetadata::getFieldType)
                .toArray(Class<?>[]::new);
            Constructor<?> ctor = entityClass.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                "Record constructor not found for " + entityClass.getName(), e);
        }
    }

    /**
     * Находит конструктор без параметров для POJO.
     */
    private Constructor<?> findDefaultConstructor() {
        try {
            Constructor<?> ctor = entityClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                "Default constructor not found for " + entityClass.getName()
                + ". Add a no-args constructor or use Java Record.", e);
        }
    }

    /**
     * Конвертирует camelCase в snake_case.
     *
     * @param camelCase строка в camelCase
     * @return строка в snake_case
     */
    public static String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Конвертирует snake_case в camelCase.
     *
     * @param snake строка в snake_case
     * @return строка в camelCase
     */
    public static String snakeToCamel(String snake) {
        if (snake == null || !snake.contains("_")) {
            return snake;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (char c : snake.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }

    // ==================== Getters ====================

    /**
     * Возвращает целевой класс.
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * Проверяет, является ли класс Record.
     */
    public boolean isRecord() {
        return isRecord;
    }

    /**
     * Возвращает список метаданных полей.
     */
    public List<FieldMetadata> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Возвращает конструктор для создания экземпляра.
     */
    public Constructor<?> getConstructor() {
        return constructor;
    }

    /**
     * Возвращает метаданные поля по имени колонки.
     *
     * @param columnName имя колонки (case insensitive)
     * @return метаданные поля или Optional.empty()
     */
    public Optional<FieldMetadata> getFieldByColumnName(String columnName) {
        if (columnName == null) {
            return Optional.empty();
        }
        FieldMetadata meta = columnToField.get(columnName.toLowerCase());
        if (meta == null) {
            // Попробуем snake_case -> camelCase
            String camelCase = snakeToCamel(columnName);
            meta = columnToField.get(camelCase.toLowerCase());
        }
        return Optional.ofNullable(meta);
    }

    /**
     * Возвращает метаданные поля @Id.
     */
    public Optional<FieldMetadata> getIdField() {
        return Optional.ofNullable(idField);
    }

    /**
     * Возвращает количество полей.
     */
    public int getFieldCount() {
        return fields.size();
    }

    // ==================== Inner Class ====================

    /**
     * Метаданные одного поля.
     */
    public static final class FieldMetadata {

        private final String fieldName;
        private final String columnName;
        private final Class<?> fieldType;
        private final Field field;          // null для Record
        private final RecordComponent recordComponent; // null для POJO
        private final boolean isId;
        private final Enumerated enumerated;

        FieldMetadata(String fieldName, String columnName, Class<?> fieldType,
                      Field field, RecordComponent recordComponent,
                      boolean isId, Enumerated enumerated) {
            this.fieldName = fieldName;
            this.columnName = columnName;
            this.fieldType = fieldType;
            this.field = field;
            this.recordComponent = recordComponent;
            this.isId = isId;
            this.enumerated = enumerated;

            // Делаем Field доступным
            if (field != null) {
                field.setAccessible(true);
            }
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getColumnName() {
            return columnName;
        }

        public Class<?> getFieldType() {
            return fieldType;
        }

        public Optional<Field> getField() {
            return Optional.ofNullable(field);
        }

        public Optional<RecordComponent> getRecordComponent() {
            return Optional.ofNullable(recordComponent);
        }

        public boolean isId() {
            return isId;
        }

        /**
         * Проверяет, является ли поле Enum.
         */
        public boolean isEnum() {
            return fieldType.isEnum();
        }

        /**
         * Возвращает тип маппинга Enum (STRING или ORDINAL).
         */
        public Optional<Enumerated.EnumType> getEnumType() {
            if (enumerated != null) {
                return Optional.of(enumerated.value());
            }
            // По умолчанию STRING для всех enum
            if (fieldType.isEnum()) {
                return Optional.of(Enumerated.EnumType.STRING);
            }
            return Optional.empty();
        }

        /**
         * Устанавливает значение поля в объект (только для POJO).
         */
        public void setValue(Object instance, Object value) throws ReflectiveOperationException {
            if (field == null) {
                throw new IllegalStateException(
                    "Cannot set value on Record component: " + fieldName);
            }
            field.set(instance, value);
        }

        /**
         * Получает значение поля из объекта.
         */
        public Object getValue(Object instance) throws ReflectiveOperationException {
            if (field != null) {
                return field.get(instance);
            }
            if (recordComponent != null) {
                return recordComponent.getAccessor().invoke(instance);
            }
            throw new IllegalStateException("No accessor for field: " + fieldName);
        }

        @Override
        public String toString() {
            return "FieldMetadata{"
                + "fieldName='" + fieldName + '\''
                + ", columnName='" + columnName + '\''
                + ", fieldType=" + fieldType.getSimpleName()
                + ", isId=" + isId
                + '}';
        }
    }

    @Override
    public String toString() {
        return "EntityMetadata{"
            + "entityClass=" + entityClass.getName()
            + ", isRecord=" + isRecord
            + ", fieldCount=" + fields.size()
            + '}';
    }
}
