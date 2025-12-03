package com.company.hex.db.service;

import com.company.hex.db.exception.DbException;
import com.company.hex.db.interceptor.DbInterceptor;
import com.company.hex.db.interceptor.DbInterceptorChain;
import com.company.hex.db.interceptor.ExecutionContext;
import com.company.hex.db.validation.QueryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic proxy handler для перехвата вызовов методов DB сервисов.
 *
 * <p>Оркестрирует процесс обработки вызова метода репозитория:</p>
 * <ol>
 *     <li>Парсинг аннотаций через {@link QueryProcessor}</li>
 *     <li>Валидация запроса через {@link QueryValidator} (опционально)</li>
 *     <li>Выполнение через цепочку интерцепторов</li>
 *     <li>Маппинг результата</li>
 * </ol>
 *
 * <h2>Потокобезопасность</h2>
 * <p>Handler потокобезопасен и может использоваться несколькими потоками
 * одновременно. Метаданные методов кэшируются для повторного использования.</p>
 *
 * <h2>Обработка Object методов</h2>
 * <p>Методы {@code toString()}, {@code equals()}, {@code hashCode()}
 * обрабатываются напрямую без вызова БД.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryProcessor
 * @see DbInterceptor
 */
public class DbProxyHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(DbProxyHandler.class);

    private final Class<?> serviceInterface;
    private final QueryProcessor queryProcessor;
    private final List<DbInterceptor> interceptors;
    private final QueryValidator queryValidator;
    private final QueryExecutor queryExecutor;
    private final String dataSource;
    private final boolean validateQueries;

    /**
     * Кэш валидации методов (чтобы не валидировать повторно).
     */
    private final ConcurrentHashMap<Method, Boolean> validatedMethods = new ConcurrentHashMap<>();

    /**
     * Создаёт новый proxy handler.
     *
     * @param serviceInterface интерфейс репозитория
     * @param queryProcessor процессор запросов
     * @param interceptors список интерцепторов
     * @param queryValidator валидатор запросов (может быть null)
     * @param queryExecutor исполнитель запросов
     * @param dataSource имя DataSource по умолчанию
     * @param validateQueries включена ли валидация запросов
     */
    public DbProxyHandler(Class<?> serviceInterface,
                          QueryProcessor queryProcessor,
                          List<DbInterceptor> interceptors,
                          QueryValidator queryValidator,
                          QueryExecutor queryExecutor,
                          String dataSource,
                          boolean validateQueries) {
        this.serviceInterface = serviceInterface;
        this.queryProcessor = queryProcessor;
        this.interceptors = sortInterceptors(interceptors);
        this.queryValidator = queryValidator;
        this.queryExecutor = queryExecutor;
        this.dataSource = dataSource;
        this.validateQueries = validateQueries;
    }

    /**
     * Сортирует интерцепторы по порядку выполнения.
     */
    private List<DbInterceptor> sortInterceptors(List<DbInterceptor> interceptors) {
        if (interceptors == null || interceptors.isEmpty()) {
            return List.of();
        }

        List<DbInterceptor> sorted = new ArrayList<>(interceptors);
        sorted.sort(Comparator.comparingInt(DbInterceptor::getOrder));
        return List.copyOf(sorted);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Обработка стандартных методов Object
        if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(proxy, method, args);
        }

        // Обработка default методов интерфейса
        if (method.isDefault()) {
            return handleDefaultMethod(proxy, method, args);
        }

        logger.debug("Intercepted method call: {}.{}",
            serviceInterface.getSimpleName(), method.getName());

        try {
            // Шаг 1: Парсим аннотации и создаём QueryDefinition
            QueryDefinition queryDefinition = queryProcessor.process(method, args, serviceInterface);

            // Шаг 2: Валидируем запрос (один раз при первом вызове)
            validateQueryIfNeeded(method, queryDefinition);

            // Шаг 3: Создаём контекст выполнения
            ExecutionContext context = createExecutionContext(queryDefinition);

            // Шаг 4: Выполняем через цепочку интерцепторов
            Object result = executeWithInterceptors(queryDefinition, context);

            logger.debug("Method invocation completed successfully: {}.{}",
                serviceInterface.getSimpleName(), method.getName());

            return result;

        } catch (DbException e) {
            logger.error("Database error in {}.{}: {}",
                serviceInterface.getSimpleName(), method.getName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error invoking method {}.{}: {}",
                serviceInterface.getSimpleName(), method.getName(), e.getMessage(), e);
            throw new DbException("Error executing " + method.getName(), e);
        } finally {
            // Очищаем контекст потока
            ExecutionContext.clear();
        }
    }

    /**
     * Обрабатывает стандартные методы Object.
     */
    private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();

        switch (methodName) {
            case "toString":
                return "DbServiceProxy[" + serviceInterface.getSimpleName() + "]";
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return System.identityHashCode(proxy);
            default:
                throw new UnsupportedOperationException(
                    "Method " + methodName + " is not supported");
        }
    }

    /**
     * Обрабатывает default методы интерфейса.
     */
    private Object handleDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        // Для Java 16+ используем MethodHandle
        return java.lang.invoke.MethodHandles.lookup()
            .findSpecial(
                serviceInterface,
                method.getName(),
                java.lang.invoke.MethodType.methodType(
                    method.getReturnType(), method.getParameterTypes()),
                serviceInterface)
            .bindTo(proxy)
            .invokeWithArguments(args);
    }

    /**
     * Валидирует запрос при первом вызове метода.
     */
    private void validateQueryIfNeeded(Method method, QueryDefinition queryDefinition) {
        if (!validateQueries || queryValidator == null) {
            return;
        }

        // Проверяем, был ли метод уже валидирован
        validatedMethods.computeIfAbsent(method, m -> {
            logger.debug("Validating query for method: {}", method.getName());
            queryValidator.validate(queryDefinition);
            return true;
        });
    }

    /**
     * Создаёт контекст выполнения для запроса.
     */
    private ExecutionContext createExecutionContext(QueryDefinition queryDefinition) {
        ExecutionContext context = new ExecutionContext();
        context.setDataSourceName(queryDefinition.getDataSource());
        context.setReadOnly(queryDefinition.isReadOnly());
        queryDefinition.getTimeout().ifPresent(context::setQueryTimeout);
        ExecutionContext.setCurrent(context);
        return context;
    }

    /**
     * Выполняет запрос через цепочку интерцепторов.
     */
    private Object executeWithInterceptors(QueryDefinition queryDefinition, ExecutionContext context) {
        // Создаём цепочку интерцепторов
        DbInterceptorChain chain = new DefaultInterceptorChain(
            queryDefinition, context, interceptors, 0, queryExecutor);

        // Запускаем выполнение
        return chain.proceed();
    }

    /**
     * Реализация цепочки интерцепторов.
     */
    private static class DefaultInterceptorChain implements DbInterceptorChain {

        private final QueryDefinition query;
        private final ExecutionContext context;
        private final List<DbInterceptor> interceptors;
        private final int currentIndex;
        private final QueryExecutor queryExecutor;

        DefaultInterceptorChain(QueryDefinition query,
                                ExecutionContext context,
                                List<DbInterceptor> interceptors,
                                int currentIndex,
                                QueryExecutor queryExecutor) {
            this.query = query;
            this.context = context;
            this.interceptors = interceptors;
            this.currentIndex = currentIndex;
            this.queryExecutor = queryExecutor;
        }

        @Override
        public QueryDefinition query() {
            return query;
        }

        @Override
        public ExecutionContext context() {
            return context;
        }

        @Override
        public Object proceed() {
            return proceed(query);
        }

        @Override
        public Object proceed(QueryDefinition query) {
            if (currentIndex < interceptors.size()) {
                // Вызываем следующий интерцептор
                DbInterceptor interceptor = interceptors.get(currentIndex);
                DbInterceptorChain nextChain = new DefaultInterceptorChain(
                    query, context, interceptors, currentIndex + 1, queryExecutor);
                return interceptor.intercept(nextChain);
            } else {
                // Все интерцепторы пройдены — выполняем запрос
                return executeQuery(query);
            }
        }

        /**
         * Выполняет запрос через QueryExecutor.
         */
        private Object executeQuery(QueryDefinition query) {
            if (queryExecutor != null) {
                return queryExecutor.execute(query, context);
            }

            // Если executor не задан, возвращаем заглушку для тестов
            throw new DbException(
                "QueryExecutor not configured. Cannot execute query: " + query.getMethodName());
        }
    }

    /**
     * Возвращает интерфейс сервиса.
     *
     * @return интерфейс репозитория
     */
    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    /**
     * Возвращает имя DataSource.
     *
     * @return имя DataSource
     */
    public String getDataSource() {
        return dataSource;
    }
}
