package com.company.hex.db.service;

import com.company.hex.db.config.DbConfig;
import com.company.hex.db.exception.DbException;
import com.company.hex.db.interceptor.DbInterceptor;
import com.company.hex.db.mapping.RowMapper;
import com.company.hex.db.validation.QueryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder для создания DB сервисов с расширенной конфигурацией.
 *
 * <p>Позволяет гибко настраивать интерцепторы, валидаторы, маппинг
 * и другие параметры создаваемого сервиса.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Базовая конфигурация
 * UserRepository repo = DbServiceFactory.builder()
 *     .withDataSource("analytics")
 *     .create(UserRepository.class);
 *
 * // Полная конфигурация
 * UserRepository repo = DbServiceFactory.builder()
 *     .withDataSource("primary")
 *     .withConfig(customConfig)
 *     .addInterceptor(new QueryLoggingInterceptor())
 *     .addInterceptor(new SlowQueryInterceptor(Duration.ofSeconds(5)))
 *     .addInterceptorFirst(new AuthInterceptor())
 *     .withValidator(new StrictQueryValidator())
 *     .enableQueryValidation(true)
 *     .create(UserRepository.class);
 * }</pre>
 *
 * <h2>Порядок интерцепторов</h2>
 * <p>Интерцепторы выполняются в порядке их добавления, за исключением:</p>
 * <ul>
 *     <li>{@code addInterceptorFirst()} — добавляет в начало</li>
 *     <li>{@code addInterceptorAfter()} — добавляет после указанного класса</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DbServiceFactory
 */
public final class DbServiceFactoryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DbServiceFactoryBuilder.class);

    private String dataSource = "primary";
    private DbConfig config;
    private final List<InterceptorEntry> interceptors = new ArrayList<>();
    private QueryValidator queryValidator;
    private QueryExecutor queryExecutor;
    private boolean enableQueryValidation = true;
    private RowMapperFactory rowMapperFactory;

    /**
     * Внутренний класс для хранения интерцептора и его позиции.
     */
    private static class InterceptorEntry {
        final DbInterceptor interceptor;
        final Position position;
        final Class<?> afterClass;

        enum Position {
            FIRST, NORMAL, AFTER
        }

        InterceptorEntry(DbInterceptor interceptor, Position position, Class<?> afterClass) {
            this.interceptor = interceptor;
            this.position = position;
            this.afterClass = afterClass;
        }
    }

    /**
     * Package-private конструктор. Используйте DbServiceFactory.builder()
     */
    DbServiceFactoryBuilder() {
    }

    /**
     * Устанавливает имя DataSource.
     *
     * @param dataSource имя DataSource из конфигурации
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder withDataSource(String dataSource) {
        if (dataSource == null || dataSource.isBlank()) {
            throw new DbException("DataSource name cannot be null or blank");
        }
        this.dataSource = dataSource;
        logger.debug("Set dataSource: {}", dataSource);
        return this;
    }

    /**
     * Устанавливает конфигурацию DB.
     *
     * @param config конфигурация DB
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder withConfig(DbConfig config) {
        if (config == null) {
            throw new DbException("DbConfig cannot be null");
        }
        this.config = config;
        logger.debug("Set custom DbConfig");
        return this;
    }

    /**
     * Добавляет интерцептор в конец списка.
     *
     * @param interceptor интерцептор для добавления
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder addInterceptor(DbInterceptor interceptor) {
        if (interceptor == null) {
            throw new DbException("Interceptor cannot be null");
        }
        this.interceptors.add(new InterceptorEntry(
            interceptor, InterceptorEntry.Position.NORMAL, null));
        logger.debug("Added interceptor: {}", interceptor.getClass().getSimpleName());
        return this;
    }

    /**
     * Добавляет интерцептор в начало списка.
     *
     * <p>Полезно для интерцепторов, которые должны выполняться первыми
     * (например, аутентификация, создание контекста).</p>
     *
     * @param interceptor интерцептор для добавления
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder addInterceptorFirst(DbInterceptor interceptor) {
        if (interceptor == null) {
            throw new DbException("Interceptor cannot be null");
        }
        this.interceptors.add(new InterceptorEntry(
            interceptor, InterceptorEntry.Position.FIRST, null));
        logger.debug("Added interceptor first: {}", interceptor.getClass().getSimpleName());
        return this;
    }

    /**
     * Добавляет интерцептор после указанного класса.
     *
     * @param afterClass класс интерцептора, после которого нужно вставить
     * @param interceptor интерцептор для добавления
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder addInterceptorAfter(Class<?> afterClass, DbInterceptor interceptor) {
        if (afterClass == null) {
            throw new DbException("afterClass cannot be null");
        }
        if (interceptor == null) {
            throw new DbException("Interceptor cannot be null");
        }
        this.interceptors.add(new InterceptorEntry(
            interceptor, InterceptorEntry.Position.AFTER, afterClass));
        logger.debug("Added interceptor after {}: {}",
            afterClass.getSimpleName(), interceptor.getClass().getSimpleName());
        return this;
    }

    /**
     * Добавляет несколько интерцепторов.
     *
     * @param interceptors список интерцепторов
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder addInterceptors(List<DbInterceptor> interceptors) {
        if (interceptors != null) {
            interceptors.forEach(this::addInterceptor);
        }
        return this;
    }

    /**
     * Устанавливает валидатор запросов.
     *
     * @param validator валидатор запросов
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder withValidator(QueryValidator validator) {
        if (validator == null) {
            throw new DbException("QueryValidator cannot be null");
        }
        this.queryValidator = validator;
        logger.debug("Set validator: {}", validator.getClass().getSimpleName());
        return this;
    }

    /**
     * Устанавливает исполнитель запросов.
     *
     * @param executor исполнитель запросов
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder withExecutor(QueryExecutor executor) {
        if (executor == null) {
            throw new DbException("QueryExecutor cannot be null");
        }
        this.queryExecutor = executor;
        logger.debug("Set executor: {}", executor.getClass().getSimpleName());
        return this;
    }

    /**
     * Включает или выключает валидацию запросов.
     *
     * @param enable true для включения валидации
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder enableQueryValidation(boolean enable) {
        this.enableQueryValidation = enable;
        logger.debug("Query validation: {}", enable ? "enabled" : "disabled");
        return this;
    }

    /**
     * Устанавливает фабрику RowMapper.
     *
     * @param factory фабрика RowMapper
     * @return этот builder для fluent API
     */
    public DbServiceFactoryBuilder withRowMapperFactory(RowMapperFactory factory) {
        this.rowMapperFactory = factory;
        logger.debug("Set custom RowMapperFactory");
        return this;
    }

    /**
     * Создаёт экземпляр DB сервиса с настроенной конфигурацией.
     *
     * @param <T> тип интерфейса сервиса
     * @param serviceInterface класс интерфейса сервиса
     * @return proxy-экземпляр сервиса
     * @throws DbException если интерфейс некорректен
     */
    public <T> T create(Class<T> serviceInterface) {
        validateInterface(serviceInterface);

        logger.info("Создание DB сервиса через builder: {}", serviceInterface.getSimpleName());

        // Создаём компоненты
        QueryProcessor queryProcessor = new QueryProcessor();

        // Упорядочиваем интерцепторы
        List<DbInterceptor> orderedInterceptors = orderInterceptors();

        return DbServiceFactory.createProxy(
            serviceInterface,
            queryProcessor,
            orderedInterceptors,
            queryValidator,
            queryExecutor,
            dataSource,
            enableQueryValidation
        );
    }

    /**
     * Валидирует интерфейс сервиса.
     */
    private void validateInterface(Class<?> serviceInterface) {
        if (serviceInterface == null) {
            throw new DbException("Service interface cannot be null");
        }

        if (!serviceInterface.isInterface()) {
            throw new DbException("Service class must be an interface: " + serviceInterface.getName());
        }
    }

    /**
     * Упорядочивает интерцепторы согласно указанным позициям.
     */
    private List<DbInterceptor> orderInterceptors() {
        List<DbInterceptor> result = new ArrayList<>();
        Map<Class<?>, Integer> classPositions = new LinkedHashMap<>();

        // Сначала добавляем все FIRST интерцепторы
        for (InterceptorEntry entry : interceptors) {
            if (entry.position == InterceptorEntry.Position.FIRST) {
                result.add(entry.interceptor);
                classPositions.put(entry.interceptor.getClass(), result.size() - 1);
            }
        }

        // Затем добавляем NORMAL интерцепторы
        for (InterceptorEntry entry : interceptors) {
            if (entry.position == InterceptorEntry.Position.NORMAL) {
                result.add(entry.interceptor);
                classPositions.put(entry.interceptor.getClass(), result.size() - 1);
            }
        }

        // Наконец, обрабатываем AFTER интерцепторы
        for (InterceptorEntry entry : interceptors) {
            if (entry.position == InterceptorEntry.Position.AFTER) {
                Integer afterIndex = classPositions.get(entry.afterClass);
                if (afterIndex != null) {
                    result.add(afterIndex + 1, entry.interceptor);
                    // Обновляем позиции для последующих интерцепторов
                    updatePositions(classPositions, afterIndex + 1);
                    classPositions.put(entry.interceptor.getClass(), afterIndex + 1);
                } else {
                    logger.warn("Class {} not found for addInterceptorAfter, adding at end",
                        entry.afterClass.getSimpleName());
                    result.add(entry.interceptor);
                    classPositions.put(entry.interceptor.getClass(), result.size() - 1);
                }
            }
        }

        return result;
    }

    /**
     * Обновляет позиции в map после вставки интерцептора.
     */
    private void updatePositions(Map<Class<?>, Integer> positions, int fromIndex) {
        for (Map.Entry<Class<?>, Integer> entry : positions.entrySet()) {
            if (entry.getValue() >= fromIndex) {
                positions.put(entry.getKey(), entry.getValue() + 1);
            }
        }
    }

    /**
     * Функциональный интерфейс для фабрики RowMapper.
     */
    @FunctionalInterface
    public interface RowMapperFactory {
        /**
         * Создаёт RowMapper для указанного типа.
         *
         * @param <T> тип результата
         * @param type класс результата
         * @return RowMapper для типа или null если не поддерживается
         */
        <T> RowMapper<T> createMapper(Class<T> type);
    }
}
