package com.company.hex.db.service;

import com.company.hex.db.annotations.config.DbService;
import com.company.hex.db.exception.DbException;
import com.company.hex.db.interceptor.DbInterceptor;
import com.company.hex.db.validation.QueryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Фабрика для создания экземпляров DB сервисов.
 *
 * <p>Создаёт proxy-реализации аннотированных интерфейсов репозиториев.
 * Поддерживает как простое создание с настройками по умолчанию,
 * так и гибкую конфигурацию через Builder API.</p>
 *
 * <h2>Простое использование</h2>
 * <pre>{@code
 * // Создание с настройками по умолчанию
 * UserRepository userRepo = DbServiceFactory.create(UserRepository.class);
 *
 * // Использование
 * Optional<User> user = userRepo.findById(42L);
 * }</pre>
 *
 * <h2>Builder API</h2>
 * <pre>{@code
 * UserRepository userRepo = DbServiceFactory.builder()
 *     .withDataSource("analytics")
 *     .withConfig(customConfig)
 *     .addInterceptor(new LoggingInterceptor())
 *     .addInterceptor(new SlowQueryInterceptor())
 *     .withValidator(new StrictQueryValidator())
 *     .create(UserRepository.class);
 * }</pre>
 *
 * <h2>Потокобезопасность</h2>
 * <p>Фабрика и создаваемые прокси потокобезопасны. Кэш репозиториев
 * использует ConcurrentHashMap для безопасного доступа из нескольких потоков.</p>
 *
 * <h2>Escape Hatch</h2>
 * <pre>{@code
 * // Прямой доступ к соединению
 * Connection conn = DbServiceFactory.getConnection("primary");
 *
 * // Прямой доступ к DataSource
 * DataSource ds = DbServiceFactory.getDataSource("primary");
 *
 * // Прямой QueryExecutor
 * QueryExecutor executor = DbServiceFactory.getQueryExecutor();
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DbServiceFactoryBuilder
 * @see DbProxyHandler
 */
public final class DbServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DbServiceFactory.class);

    /**
     * Кэш созданных репозиториев для повторного использования.
     */
    private static final ConcurrentHashMap<Class<?>, Object> repositoryCache = new ConcurrentHashMap<>();

    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private DbServiceFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Создаёт новый builder для настройки DB сервиса.
     *
     * @return новый экземпляр DbServiceFactoryBuilder
     */
    public static DbServiceFactoryBuilder builder() {
        return new DbServiceFactoryBuilder();
    }

    /**
     * Создаёт DB сервис с настройками по умолчанию.
     *
     * <p>Настройки по умолчанию:</p>
     * <ul>
     *     <li>DataSource: из аннотации @DbService или "primary"</li>
     *     <li>Query timeout: из аннотации @DbService или 30 секунд</li>
     *     <li>Валидация запросов: включена</li>
     *     <li>Интерцепторы: без дополнительных</li>
     * </ul>
     *
     * @param <T> тип интерфейса сервиса
     * @param serviceInterface класс интерфейса сервиса
     * @return proxy-экземпляр сервиса
     * @throws DbException если интерфейс некорректен или не может быть создан
     */
    public static <T> T create(Class<T> serviceInterface) {
        validateInterface(serviceInterface);

        logger.info("Создание DB сервиса: {}", serviceInterface.getSimpleName());

        // Извлекаем конфигурацию из @DbService
        DbService dbServiceAnnotation = serviceInterface.getAnnotation(DbService.class);
        String dataSource = dbServiceAnnotation != null
            ? dbServiceAnnotation.dataSource()
            : "primary";
        boolean validateQueries = dbServiceAnnotation == null
            || dbServiceAnnotation.validateQueries();

        return createProxy(
            serviceInterface,
            new QueryProcessor(),
            List.of(),
            null,
            null,
            dataSource,
            validateQueries
        );
    }

    /**
     * Создаёт DB сервис и кэширует его.
     *
     * <p>При повторном вызове с тем же интерфейсом возвращает
     * закэшированный экземпляр.</p>
     *
     * @param <T> тип интерфейса сервиса
     * @param serviceInterface класс интерфейса сервиса
     * @return proxy-экземпляр сервиса (может быть кэшированным)
     */
    @SuppressWarnings("unchecked")
    public static <T> T createCached(Class<T> serviceInterface) {
        return (T) repositoryCache.computeIfAbsent(serviceInterface, DbServiceFactory::create);
    }

    /**
     * Очищает кэш репозиториев.
     *
     * <p>Полезно для тестов, когда нужно пересоздать репозитории
     * с новой конфигурацией.</p>
     */
    public static void clearCache() {
        repositoryCache.clear();
        logger.debug("Repository cache cleared");
    }

    /**
     * Создаёт proxy для интерфейса.
     */
    @SuppressWarnings("unchecked")
    static <T> T createProxy(Class<T> serviceInterface,
                             QueryProcessor queryProcessor,
                             List<DbInterceptor> interceptors,
                             QueryValidator queryValidator,
                             QueryExecutor queryExecutor,
                             String dataSource,
                             boolean validateQueries) {

        // Создаём proxy handler
        DbProxyHandler handler = new DbProxyHandler(
            serviceInterface,
            queryProcessor,
            interceptors,
            queryValidator,
            queryExecutor,
            dataSource,
            validateQueries
        );

        // Создаём и возвращаем proxy
        T proxy = (T) Proxy.newProxyInstance(
            serviceInterface.getClassLoader(),
            new Class<?>[]{serviceInterface},
            handler
        );

        logger.info("Успешно создан DB сервис: {}", serviceInterface.getSimpleName());
        return proxy;
    }

    /**
     * Валидирует интерфейс сервиса.
     */
    private static void validateInterface(Class<?> serviceInterface) {
        if (serviceInterface == null) {
            throw new DbException("Service interface cannot be null");
        }

        if (!serviceInterface.isInterface()) {
            throw new DbException("Service class must be an interface: " + serviceInterface.getName());
        }
    }

    /**
     * Возвращает QueryExecutor для прямого использования.
     *
     * <p>Это "escape hatch" для сценариев, требующих прямого
     * выполнения запросов без использования репозитория.</p>
     *
     * @return QueryExecutor или null если не сконфигурирован
     */
    public static QueryExecutor getQueryExecutor() {
        // TODO: Implement when ConnectionProvider is ready
        logger.debug("Getting QueryExecutor");
        return null;
    }

    /**
     * Возвращает QueryProcessor для анализа методов.
     *
     * <p>Полезно для тестов и инструментов анализа репозиториев.</p>
     *
     * @return новый экземпляр QueryProcessor
     */
    public static QueryProcessor getQueryProcessor() {
        return new QueryProcessor();
    }
}
