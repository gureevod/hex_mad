/**
 * Пакет с основными сервисными классами модуля.
 *
 * <p>Содержит ключевые компоненты для создания и выполнения репозиториев:</p>
 *
 * <ul>
 *     <li>{@code DbServiceFactory} — фабрика для создания прокси репозиториев</li>
 *     <li>{@code QueryExecutor} — выполнение SQL через JDBC</li>
 *     <li>{@code QueryProcessor} — парсинг аннотаций и подготовка запросов</li>
 *     <li>{@code DbProxyHandler} — InvocationHandler для прокси</li>
 *     <li>{@code QueryDefinition} — DTO с метаданными запроса</li>
 * </ul>
 *
 * <h2>Пример использования DbServiceFactory</h2>
 * <pre>{@code
 * // Простое создание
 * UserRepository repo = DbServiceFactory.create(UserRepository.class);
 *
 * // С Builder API
 * UserRepository repo = DbServiceFactory.builder(UserRepository.class)
 *     .withDataSource("primary")
 *     .withConfig(customConfig)
 *     .addInterceptor(new AuditInterceptor())
 *     .withValidator(new StrictQueryValidator())
 *     .build();
 * }</pre>
 *
 * <h2>Архитектура компонентов</h2>
 * <pre>
 * DbServiceFactory.create(MyRepo.class)
 *         │
 *         ▼
 * ┌──────────────────┐
 * │  DbProxyHandler  │ ◄── InvocationHandler
 * └────────┬─────────┘
 *          │
 *     ┌────┴────┐
 *     ▼         ▼
 * QueryProcessor  InterceptorChain
 *     │              │
 *     ▼              ▼
 * QueryDefinition  QueryExecutor
 *                     │
 *                     ▼
 *                 ResultMapper
 * </pre>
 *
 * @see com.company.hex.db.annotations.config.DbService
 * @since 1.0.0
 */
package com.company.hex.db.service;
