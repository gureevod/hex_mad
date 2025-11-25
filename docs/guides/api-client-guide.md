# Руководство по работе с декларативным REST API клиентом Hex Framework

## Содержание

1. [Введение](#введение)
2. [Быстрый старт](#быстрый-старт)
3. [Архитектура фреймворка](#архитектура-фреймворка)
4. [Аннотации](#аннотации)
   - [HTTP методы](#http-методы)
   - [Параметры запроса](#параметры-запроса)
   - [Конфигурационные аннотации](#конфигурационные-аннотации)
5. [Создание API сервисов](#создание-api-сервисов)
   - [Простой подход](#простой-подход)
   - [Builder API для расширенной конфигурации](#builder-api-для-расширенной-конфигурации)
6. [Интерцепторы](#интерцепторы)
   - [Встроенные интерцепторы](#встроенные-интерцепторы)
   - [Создание кастомных интерцепторов](#создание-кастомных-интерцепторов)
7. [Конвертеры ответов](#конвертеры-ответов)
8. [Исполнители запросов](#исполнители-запросов-requestexecutor)
9. [Конфигурация](#конфигурация)
10. [DTO и модели данных](#dto-и-модели-данных)
11. [Обработка ошибок](#обработка-ошибок)
12. [Продвинутые сценарии](#продвинутые-сценарии)
13. [Escape Hatch: Прямой доступ к RestAssured](#escape-hatch-прямой-доступ-к-restassured)
14. [Legacy подход: BaseApiService](#legacy-подход-baseapiservice)
15. [Лучшие практики](#лучшие-практики)

---

## Введение

Hex Framework предоставляет декларативный подход к API тестированию, вдохновлённый библиотекой Retrofit2. Вместо написания императивного кода с цепочками RestAssured, вы определяете API контракты через аннотированные Java интерфейсы.

### Преимущества декларативного подхода

| Аспект | Императивный подход | Декларативный подход |
|--------|---------------------|----------------------|
| Boilerplate | Много повторяющегося кода | Минимум кода |
| Type Safety | Runtime ошибки | Compile-time проверки |
| Читаемость | Сложно понять контракт | API контракт самодокументирован |
| Поддержка | Трудно менять | Легко расширять |
| Тестируемость | Сложно мокировать | Просто мокируется через интерфейсы |

### Цели фреймворка

- Сокращение boilerplate кода на ≥40%
- Время до первого теста ≤ 60 минут
- Потокобезопасное параллельное выполнение с flake rate ≤ 2%
- Стандартизация паттернов для сервисов и DTO

---

## Быстрый старт

### Шаг 1: Создайте DTO

```java
package com.company.hex.project.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("username")
    private String username;
}
```

### Шаг 2: Создайте API интерфейс

```java
package com.company.hex.project.api.services;

import com.company.hex.api.annotations.config.ApiService;
import com.company.hex.api.annotations.http.*;
import com.company.hex.api.annotations.param.*;
import com.company.hex.project.api.dto.UserDto;

import java.util.List;

@ApiService(baseUrl = "https://jsonplaceholder.typicode.com")
public interface UserApi {
    
    @GET("/users")
    List<UserDto> getAllUsers();
    
    @GET("/users/{id}")
    UserDto getUserById(@Path("id") int userId);
    
    @POST("/users")
    UserDto createUser(@Body CreateUserRequest request);
}
```

### Шаг 3: Используйте в тестах

```java
import com.company.hex.api.service.ApiServiceFactory;
import org.junit.jupiter.api.Test;

class UserApiTest {
    
    @Test
    void shouldGetUserById() {
        // Создаём клиент API
        UserApi userApi = ApiServiceFactory.create(UserApi.class);
        
        // Вызываем метод - всё просто!
        UserDto user = userApi.getUserById(1);
        
        // Проверяем результат
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getName()).isNotEmpty();
    }
}
```

---

## Архитектура фреймворка

Фреймворк построен на модульной архитектуре с чётким разделением ответственности:

```
┌──────────────────────────────────────────────────────────────────┐
│                    Annotated Interface                            │
│                   (UserApi.java)                                  │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                      ProxyHandler                                 │
│  - Перехватывает вызовы методов                                   │
│  - Оркестрирует процесс обработки                                │
└──────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ Annotation      │ │ Interceptor     │ │ Response        │
│ Processor       │ │ Chain           │ │ Converter       │
│                 │ │                 │ │                 │
│ Парсит аннотации│ │ Retry, Logging, │ │ Jackson-based   │
│ → RequestDef    │ │ StatusValidation│ │ конвертация     │
└─────────────────┘ └────────┬────────┘ └─────────────────┘
                             │
                             ▼
                   ┌─────────────────┐
                   │ Request         │
                   │ Executor        │
                   │                 │
                   │ RestAssured     │
                   │ HTTP клиент     │
                   └─────────────────┘
```

### Основные компоненты

| Компонент | Класс | Описание |
|-----------|-------|----------|
| **Фабрика сервисов** | [`ApiServiceFactory`](hex-core-api/src/main/java/com/company/hex/api/service/ApiServiceFactory.java:31) | Создаёт прокси-экземпляры API сервисов |
| **Proxy Handler** | [`ProxyHandler`](hex-core-api/src/main/java/com/company/hex/api/proxy/ProxyHandler.java:21) | Перехватывает вызовы методов интерфейса |
| **Процессор аннотаций** | [`AnnotationProcessor`](hex-core-api/src/main/java/com/company/hex/api/processor/AnnotationProcessor.java:38) | Извлекает метаданные из аннотаций |
| **Цепочка интерцепторов** | [`InterceptorChain`](hex-core-api/src/main/java/com/company/hex/api/interceptor/InterceptorChain.java:20) | Выполняет pre/post обработку запросов |
| **Исполнитель запросов** | [`RestAssuredExecutor`](hex-core-api/src/main/java/com/company/hex/api/executor/RestAssuredExecutor.java:22) | Выполняет HTTP запросы через RestAssured |
| **Конвертер ответов** | [`JacksonResponseConverter`](hex-core-api/src/main/java/com/company/hex/api/converter/JacksonResponseConverter.java:21) | Конвертирует HTTP ответы в Java объекты |

---

## Аннотации

### HTTP методы

Аннотации для указания типа HTTP запроса размещаются на методах интерфейса:

| Аннотация | Пакет | Описание |
|-----------|-------|----------|
| `@GET` | [`com.company.hex.api.annotations.http`](hex-core-api/src/main/java/com/company/hex/api/annotations/http/GET.java) | HTTP GET запрос |
| `@POST` | [`com.company.hex.api.annotations.http`](hex-core-api/src/main/java/com/company/hex/api/annotations/http/POST.java) | HTTP POST запрос |
| `@PUT` | [`com.company.hex.api.annotations.http`](hex-core-api/src/main/java/com/company/hex/api/annotations/http/PUT.java) | HTTP PUT запрос |
| `@DELETE` | [`com.company.hex.api.annotations.http`](hex-core-api/src/main/java/com/company/hex/api/annotations/http/DELETE.java) | HTTP DELETE запрос |
| `@PATCH` | [`com.company.hex.api.annotations.http`](hex-core-api/src/main/java/com/company/hex/api/annotations/http/PATCH.java) | HTTP PATCH запрос |

**Пример использования:**

```java
public interface UserApi {
    
    @GET("/users")                  // GET запрос на /users
    List<UserDto> getAllUsers();
    
    @POST("/users")                 // POST запрос на /users
    UserDto createUser(@Body CreateUserRequest request);
    
    @PUT("/users/{id}")             // PUT запрос на /users/{id}
    UserDto updateUser(@Path("id") int userId, @Body UpdateUserRequest request);
    
    @DELETE("/users/{id}")          // DELETE запрос на /users/{id}
    void deleteUser(@Path("id") int userId);
    
    @PATCH("/users/{id}")           // PATCH запрос на /users/{id}
    UserDto patchUser(@Path("id") int userId, @Body PatchRequest patch);
}
```

> **Важно:** Каждый метод должен иметь ровно одну HTTP аннотацию. Валидатор [`InterfaceValidator`](hex-core-api/src/main/java/com/company/hex/api/validation/InterfaceValidator.java:28) проверяет это при создании сервиса.

### Параметры запроса

Аннотации для указания параметров запроса размещаются на параметрах методов:

| Аннотация | Пакет | Описание | Пример |
|-----------|-------|----------|--------|
| `@Path` | [`com.company.hex.api.annotations.param`](hex-core-api/src/main/java/com/company/hex/api/annotations/param/Path.java) | Path параметр в URL | `/users/{id}` → `@Path("id")` |
| `@Query` | [`com.company.hex.api.annotations.param`](hex-core-api/src/main/java/com/company/hex/api/annotations/param/Query.java) | Query параметр | `?name=John` → `@Query("name")` |
| `@Header` | [`com.company.hex.api.annotations.param`](hex-core-api/src/main/java/com/company/hex/api/annotations/param/Header.java) | HTTP заголовок | `Authorization: Bearer ...` |
| `@Body` | [`com.company.hex.api.annotations.param`](hex-core-api/src/main/java/com/company/hex/api/annotations/param/Body.java) | Тело запроса | JSON payload |
| `@FormParam` | [`com.company.hex.api.annotations.param`](hex-core-api/src/main/java/com/company/hex/api/annotations/param/FormParam.java) | Form параметр | `multipart/form-data` |

**Примеры использования:**

```java
public interface UserApi {
    
    // Path параметры - подставляются в URL
    @GET("/users/{id}")
    UserDto getUserById(@Path("id") int userId);
    
    @GET("/users/{userId}/posts/{postId}")
    PostDto getPost(@Path("userId") int userId, @Path("postId") int postId);
    
    // Query параметры - добавляются после ?
    @GET("/users")
    List<UserDto> searchUsers(@Query("name") String name, 
                              @Query("age") Integer age);
    // Результат: /users?name=John&age=25
    
    // Динамические заголовки
    @GET("/users/{id}")
    UserDto getUserWithAuth(@Path("id") int userId, 
                            @Header("Authorization") String authToken);
    
    // Тело запроса - автоматически сериализуется в JSON
    @POST("/users")
    UserDto createUser(@Body CreateUserRequest request);
    
    // Form параметры - для multipart запросов
    @POST("/users/{id}/avatar")
    Response uploadAvatar(@Path("id") int userId,
                         @FormParam("file") byte[] fileContent,
                         @FormParam("filename") String filename);
}
```

> **Валидация:** Фреймворк проверяет, что все `@Path` параметры имеют соответствующие плейсхолдеры в URL. Также `@Body` не разрешён для `@GET` запросов.

### Конфигурационные аннотации

Аннотации для настройки поведения сервиса и методов:

#### @ApiService

Размещается на интерфейсе, определяет базовые настройки сервиса:

```java
// Вариант 1: Прямой URL
@ApiService(baseUrl = "https://api.example.com")
public interface MyApi { ... }

// Вариант 2: URL из конфигурации (hex.properties)
@ApiService(baseUrl = "${api.base.url}")
public interface MyApi { ... }

// Вариант 3: С базовым путём
@ApiService(baseUrl = "https://api.example.com", basePath = "/api/v1")
public interface MyApi { 
    @GET("/users")  // Итоговый путь: /api/v1/users
    List<UserDto> getUsers();
}
```

#### @Headers

Добавляет статические заголовки к методу:

```java
@POST("/users")
@Headers({"Content-Type: application/json", "X-Api-Version: 2.0"})
UserDto createUser(@Body CreateUserRequest request);
```

#### @Retry

Настраивает автоматические повторные попытки при ошибках:

```java
@PUT("/users/{id}")
@Retry(count = 5, delay = 2000)  // 5 попыток с задержкой 2 секунды
UserDto updateUser(@Path("id") int userId, @Body UpdateRequest request);
```

> **Поведение:** Повторные попытки выполняются при:
> - Сетевых ошибках (исключениях)
> - HTTP статусах 5xx (серверные ошибки)

#### @ExpectedStatus

Валидирует ожидаемый HTTP статус ответа:

```java
@DELETE("/users/{id}")
@ExpectedStatus(204)  // Ожидаем 204 No Content
void deleteUser(@Path("id") int userId);

@POST("/users")
@ExpectedStatus({200, 201})  // Ожидаем 200 или 201
UserDto createUser(@Body CreateUserRequest request);
```

> **При несоответствии:** Бросается `AssertionError` с детальным описанием.

---

## Создание API сервисов

### Простой подход

Для большинства случаев достаточно статического метода [`ApiServiceFactory.create()`](hex-core-api/src/main/java/com/company/hex/api/service/ApiServiceFactory.java:59):

```java
// Создание с конфигурацией по умолчанию
UserApi userApi = ApiServiceFactory.create(UserApi.class);

// Создание с кастомной конфигурацией
ApiConfig customConfig = HexConfigFactory.getConfig(ApiConfig.class);
UserApi userApi = ApiServiceFactory.create(UserApi.class, customConfig);
```

**Что происходит при создании:**

1. Валидируется интерфейс ([`InterfaceValidator`](hex-core-api/src/main/java/com/company/hex/api/validation/InterfaceValidator.java))
2. Создаются компоненты с дефолтными настройками
3. Настраивается цепочка интерцепторов: `RetryInterceptor → LoggingInterceptor → StatusValidationInterceptor`
4. Создаётся Java Proxy для интерфейса

### Builder API для расширенной конфигурации

Для сложных сценариев используйте [`ApiServiceFactory.builder()`](hex-core-api/src/main/java/com/company/hex/api/service/ApiServiceFactoryBuilder.java:44):

```java
UserApi userApi = ApiServiceFactory.builder()
    .withConfig(customConfig)                           // Кастомная конфигурация
    .addInterceptorFirst(new AuthInterceptor(token))    // Интерцептор в начало
    .addInterceptor(new MetricsInterceptor())           // Интерцептор в конец
    .withConverter(customConverter)                      // Кастомный конвертер
    .withExecutor(customExecutor)                        // Кастомный executor
    .create(UserApi.class);
```

**Методы Builder API:**

| Метод | Описание |
|-------|----------|
| `withConfig(ApiConfig)` | Устанавливает кастомную конфигурацию |
| `addInterceptor(Interceptor)` | Добавляет интерцептор в конец цепочки |
| `addInterceptorFirst(Interceptor)` | Добавляет интерцептор в начало цепочки |
| `addInterceptorAfter(Class, Interceptor)` | Добавляет интерцептор после указанного класса |
| `withConverter(ResponseConverter)` | Устанавливает кастомный конвертер ответов |
| `withExecutor(RequestExecutor)` | Устанавливает кастомный executor |

---

## Интерцепторы

Интерцепторы реализуют паттерн Chain of Responsibility и позволяют модифицировать запросы и ответы на любом этапе обработки.

### Встроенные интерцепторы

#### 1. RetryInterceptor

[`RetryInterceptor`](hex-core-api/src/main/java/com/company/hex/api/interceptor/RetryInterceptor.java:18) выполняет повторные попытки при ошибках:

- Обрабатывает аннотацию `@Retry` на методах
- Повторяет запрос при сетевых ошибках и HTTP 5xx
- Логирует каждую попытку

```java
@PUT("/users/{id}")
@Retry(count = 3, delay = 1000)
UserDto updateUser(@Path("id") int userId, @Body UpdateRequest request);
```

#### 2. LoggingInterceptor

[`LoggingInterceptor`](hex-core-api/src/main/java/com/company/hex/api/interceptor/LoggingInterceptor.java:15) логирует запросы и ответы:

```
→ GET /users/1
  Path params: {id=1}
← GET /users/1 - HTTP/1.1 200 OK (125ms)
  Response time: 125ms
  Content-Type: application/json; charset=utf-8
```

#### 3. StatusValidationInterceptor

[`StatusValidationInterceptor`](hex-core-api/src/main/java/com/company/hex/api/interceptor/StatusValidationInterceptor.java:19) валидирует HTTP статусы:

- Обрабатывает аннотацию `@ExpectedStatus`
- Бросает `AssertionError` при несоответствии

### Создание кастомных интерцепторов

Интерцептор должен реализовать интерфейс [`Interceptor`](hex-core-api/src/main/java/com/company/hex/api/interceptor/Interceptor.java:28):

```java
package com.company.hex.project.api.interceptors;

import com.company.hex.api.interceptor.Interceptor;
import com.company.hex.api.model.RequestDefinition;
import io.restassured.response.Response;

/**
 * Интерцептор для добавления JWT токена ко всем запросам.
 */
public class JwtAuthInterceptor implements Interceptor {
    
    private final String jwtToken;
    
    public JwtAuthInterceptor(String jwtToken) {
        this.jwtToken = jwtToken;
    }
    
    @Override
    public Response intercept(Chain chain) {
        RequestDefinition request = chain.request();
        
        // Модифицируем запрос - добавляем заголовок авторизации
        request.getHeaders().put("Authorization", "Bearer " + jwtToken);
        
        // Передаём управление дальше по цепочке
        return chain.proceed(request);
    }
}
```

**Пример: Интерцептор для метрик**

```java
public class MetricsInterceptor implements Interceptor {
    
    private final MetricsCollector metrics;
    
    public MetricsInterceptor(MetricsCollector metrics) {
        this.metrics = metrics;
    }
    
    @Override
    public Response intercept(Chain chain) {
        RequestDefinition request = chain.request();
        long startTime = System.currentTimeMillis();
        
        try {
            Response response = chain.proceed(request);
            
            // Записываем метрики успешного запроса
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordSuccess(request.getHttpMethod(), request.getPath(), duration);
            
            return response;
        } catch (Exception e) {
            // Записываем метрики неуспешного запроса
            metrics.recordFailure(request.getHttpMethod(), request.getPath(), e);
            throw e;
        }
    }
}
```

**Пример: Интерцептор для GraphQL**

```java
public class GraphQLInterceptor implements Interceptor {
    
    @Override
    public Response intercept(Chain chain) {
        RequestDefinition request = chain.request();
        
        // Преобразуем REST вызов в GraphQL
        if (request.getPath().startsWith("/graphql")) {
            return executeGraphQLQuery(request);
        }
        
        // Обычные REST запросы идут дальше по цепочке
        return chain.proceed(request);
    }
    
    private Response executeGraphQLQuery(RequestDefinition request) {
        // Кастомная логика выполнения GraphQL запроса
    }
}
```

**Регистрация кастомных интерцепторов:**

```java
UserApi userApi = ApiServiceFactory.builder()
    .addInterceptorFirst(new JwtAuthInterceptor(token))  // Первым (до логирования)
    .addInterceptor(new MetricsInterceptor(metrics))      // В конец
    .addInterceptorAfter(LoggingInterceptor.class, new CachingInterceptor())  // После логирования
    .create(UserApi.class);
```

---

## Конвертеры ответов

Конвертер ответов отвечает за преобразование HTTP ответа в Java объекты.

### Интерфейс ResponseConverter

```java
public interface ResponseConverter {
    <T> T convert(Response response, Type returnType);
}
```

### Стандартный JacksonResponseConverter

[`JacksonResponseConverter`](hex-core-api/src/main/java/com/company/hex/api/converter/JacksonResponseConverter.java:21) поддерживает:

- Простые типы (`UserDto`, `String`, `Integer`)
- Параметризованные типы (`List<UserDto>`, `Map<String, Object>`)
- Тип `Response` — возвращает сырой RestAssured Response
- Тип `void` — ничего не возвращает

**Примеры возвращаемых типов:**

```java
public interface UserApi {
    
    // Простой объект
    @GET("/users/{id}")
    UserDto getUserById(@Path("id") int id);
    
    // Список объектов
    @GET("/users")
    List<UserDto> getAllUsers();
    
    // Сырой Response (для особых случаев)
    @GET("/users")
    Response getAllUsersRaw();
    
    // void - когда ответ не нужен
    @DELETE("/users/{id}")
    void deleteUser(@Path("id") int id);
}
```

### Создание кастомного конвертера

Для нестандартных форматов (XML, Protobuf и т.д.):

```java
public class XmlResponseConverter implements ResponseConverter {
    
    private final XmlMapper xmlMapper = new XmlMapper();
    
    @Override
    public <T> T convert(Response response, Type returnType) {
        if (returnType.equals(Response.class)) {
            return (T) response;
        }
        
        if (returnType.equals(Void.TYPE)) {
            return null;
        }
        
        String body = response.getBody().asString();
        
        try {
            if (returnType instanceof Class) {
                return xmlMapper.readValue(body, (Class<T>) returnType);
            }
            // Обработка параметризованных типов...
        } catch (Exception e) {
            throw new ApiConversionException("XML parsing failed", e, returnType, body, 
                                            response.getStatusCode());
        }
    }
}
```

**Использование:**

```java
UserApi userApi = ApiServiceFactory.builder()
    .withConverter(new XmlResponseConverter())
    .create(UserApi.class);
```

---

## Исполнители запросов (RequestExecutor)

### Интерфейс RequestExecutor

```java
public interface RequestExecutor {
    Response execute(RequestDefinition request);
}
```

### Стандартный RestAssuredExecutor

[`RestAssuredExecutor`](hex-core-api/src/main/java/com/company/hex/api/executor/RestAssuredExecutor.java:22):

- Использует RestAssured для выполнения HTTP запросов
- Автоматически интегрируется с Allure
- Поддерживает все типы параметров (path, query, headers, body, form)

### Создание кастомного Executor

Для использования другого HTTP клиента:

```java
public class OkHttpExecutor implements RequestExecutor {
    
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public Response execute(RequestDefinition request) {
        // Построение и выполнение запроса через OkHttp
        Request okHttpRequest = buildRequest(request);
        okhttp3.Response okHttpResponse = client.newCall(okHttpRequest).execute();
        
        // Преобразование в RestAssured Response для совместимости
        return convertToRestAssuredResponse(okHttpResponse);
    }
}
```

---

## Конфигурация

### ApiConfig

[`ApiConfig`](hex-core-api/src/main/java/com/company/hex/api/config/ApiConfig.java:22) определяет все настройки API клиента:

| Свойство | Ключ | По умолчанию | Описание |
|----------|------|--------------|----------|
| `baseUrl` | `hex.api.base.url` | `http://localhost:8080` | Базовый URL API |
| `apiTimeout` | `hex.api.timeout` | `30` | Таймаут в секундах |
| `apiRetryCount` | `hex.api.retry.count` | `3` | Количество повторов |
| `apiRetryDelay` | `hex.api.retry.delay` | `1000` | Задержка между повторами (мс) |
| `authType` | `hex.api.auth.type` | `none` | Тип аутентификации |
| `authUsername` | `hex.api.auth.username` | `` | Имя пользователя |
| `authPassword` | `hex.api.auth.password` | `` | Пароль |
| `bearerToken` | `hex.api.auth.bearer.token` | `` | Bearer токен |
| `defaultContentType` | `hex.api.content.type` | `application/json` | Content-Type |
| `defaultAccept` | `hex.api.accept` | `application/json` | Accept header |
| `loggingEnabled` | `hex.api.logging.enabled` | `true` | Логирование запросов |
| `sslValidationEnabled` | `hex.api.ssl.validation.enabled` | `true` | Валидация SSL |

### Файл конфигурации hex.properties

```properties
# Базовый URL для API
hex.api.base.url=https://api.example.com

# Таймауты и повторы
hex.api.timeout=30
hex.api.retry.count=3
hex.api.retry.delay=1000

# Аутентификация (none, basic, bearer, oauth2)
hex.api.auth.type=bearer
hex.api.auth.bearer.token=your-jwt-token-here

# Логирование
hex.api.logging.enabled=true

# SSL
hex.api.ssl.validation.enabled=false
```

### Разрешение плейсхолдеров

[`PropertyResolver`](hex-core-api/src/main/java/com/company/hex/api/config/PropertyResolver.java:24) разрешает плейсхолдеры в аннотациях:

```java
@ApiService(baseUrl = "${api.base.url}")           // Из hex.properties
@ApiService(baseUrl = "${API_BASE_URL}")           // Из переменных окружения
@ApiService(baseUrl = "${api.url:http://localhost}")  // Со значением по умолчанию
```

**Порядок поиска:**
1. System properties (`-Dapi.base.url=...`)
2. HexConfigFactory (hex.properties)
3. Environment variables (`API_BASE_URL=...`)
4. Значение по умолчанию (после `:`)

---

## DTO и модели данных

### Рекомендуемая структура DTO

```java
package com.company.hex.project.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO для пользователя.
 * 
 * Lombok аннотации:
 * - @Data: генерирует getters, setters, toString, equals, hashCode
 * - @Builder: паттерн Builder для создания объектов
 * - @NoArgsConstructor: конструктор без аргументов (для Jackson)
 * - @AllArgsConstructor: конструктор со всеми полями (для Builder)
 * 
 * Jackson аннотации:
 * - @JsonIgnoreProperties(ignoreUnknown = true): игнорировать неизвестные поля
 * - @JsonProperty("name"): маппинг JSON поля на Java поле
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("created_at")  // snake_case → camelCase маппинг
    private String createdAt;
}
```

### Request DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")  
    private String email;
    
    @JsonProperty("password")
    private String password;
}
```

### Генерация DTO из JSON

Фреймворк включает утилиту [`JsonToDtoGenerator`](hex-core-api/src/main/java/com/company/hex/api/tools/dto/JsonToDtoGenerator.java) для генерации DTO из JSON примеров:

```java
JsonToDtoGenerator.builder()
    .addSource(Path.of("src/test/resources/api/json/activity.json"))
    .targetPackage("com.company.hex.project.api.dto")
    .outputDir(Path.of("target/generated-sources/hex-dtos"))
    .build()
    .generate();
```

---

## Обработка ошибок

### Типы исключений

#### ApiConversionException

[`ApiConversionException`](hex-core-api/src/main/java/com/company/hex/api/exception/ApiConversionException.java:12) — ошибка десериализации ответа:

```
Ошибка десериализации ответа API: Не удалось десериализовать ответ
Ожидаемый тип: com.company.hex.project.api.dto.UserDto
HTTP Статус: 200
Тело ответа: {"invalid": "json"}

Возможные решения:
  1. Проверьте соответствие полей DTO и JSON структуры
  2. Убедитесь, что все обязательные поля присутствуют в ответе
  3. Проверьте аннотации Jackson (@JsonProperty, @JsonIgnore и т.д.)
  4. Убедитесь, что типы данных совпадают
```

#### ApiResponseException

[`ApiResponseException`](hex-core-api/src/main/java/com/company/hex/api/exception/ApiResponseException.java:13) — HTTP ошибка:

```
HTTP Запрос: GET /users/999
HTTP Статус: 404
Тело ответа: {"error": "User not found"}
```

#### AssertionError (от StatusValidationInterceptor)

При несоответствии ожидаемого статуса:

```
Status code validation failed for DELETE /users/1. Expected: [204], Actual: 200
```

### Обработка ошибок в тестах

```java
@Test
void shouldHandleNotFound() {
    UserApi userApi = ApiServiceFactory.create(UserApi.class);
    
    assertThatThrownBy(() -> userApi.getUserById(99999))
        .isInstanceOf(ApiResponseException.class)
        .hasMessageContaining("404");
}

@Test
void shouldHandleConversionError() {
    // При некорректном формате ответа
    assertThatThrownBy(() -> badApi.getWithInvalidResponse())
        .isInstanceOf(ApiConversionException.class)
        .hasMessageContaining("Ожидаемый тип");
}
```

---

## Продвинутые сценарии

### Параллельное выполнение

Все компоненты фреймворка потокобезопасны:

```java
@Test
void testParallelRequests() {
    UserApi userApi = ApiServiceFactory.create(UserApi.class);
    
    List<CompletableFuture<UserDto>> futures = IntStream.range(1, 100)
        .mapToObj(id -> CompletableFuture.supplyAsync(() -> userApi.getUserById(id)))
        .collect(Collectors.toList());
    
    List<UserDto> users = futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
    
    assertThat(users).hasSize(99);
}
```

### Динамический baseUrl

Для разных environments:

```java
public interface UserApi {
    // BaseUrl определяется в @ApiService или конфигурации
}

// Тест для разных окружений
@ParameterizedTest
@ValueSource(strings = {"dev", "staging", "prod"})
void testDifferentEnvironments(String env) {
    ApiConfig config = // загрузить конфигурацию для env
    UserApi userApi = ApiServiceFactory.create(UserApi.class, config);
    
    List<UserDto> users = userApi.getAllUsers();
    assertThat(users).isNotEmpty();
}
```

### Условные заголовки

```java
public interface UserApi {
    
    @GET("/users/{id}")
    UserDto getUserById(@Path("id") int id);
    
    @GET("/users/{id}")
    UserDto getUserWithAuth(@Path("id") int id, 
                            @Header("Authorization") String auth,
                            @Header("X-Request-Id") String requestId);
}

// Использование
String requestId = UUID.randomUUID().toString();
UserDto user = userApi.getUserWithAuth(1, "Bearer " + token, requestId);
```

### Цепочка API вызовов

```java
@Test
void testApiChaining() {
    UserApi userApi = ApiServiceFactory.create(UserApi.class);
    PostApi postApi = ApiServiceFactory.create(PostApi.class);
    
    // Получаем пользователя
    UserDto user = userApi.getUserById(1);
    
    // Получаем его посты
    List<PostDto> posts = postApi.getPostsByUserId(user.getId());
    
    // Создаём новый пост
    CreatePostRequest request = CreatePostRequest.builder()
        .userId(user.getId())
        .title("New Post")
        .body("Content here")
        .build();
    
    PostDto newPost = postApi.createPost(request);
    
    assertThat(newPost.getUserId()).isEqualTo(user.getId());
}
```

---

## Escape Hatch: Прямой доступ к RestAssured

Для сложных сценариев, когда декларативного подхода недостаточно:

### Получение RequestSpecification

```java
// Получить настроенную спецификацию с дефолтной конфигурацией
RequestSpecification spec = ApiServiceFactory.getRequestSpecification();

// Или с кастомной конфигурацией
RequestSpecification spec = ApiServiceFactory.getRequestSpecification(customConfig);

// Использование напрямую
Response response = spec
    .header("Custom-Header", "value")
    .queryParam("page", 1)
    .when()
    .get("/custom/endpoint")
    .then()
    .statusCode(200)
    .extract()
    .response();
```

### Получение RequestExecutor

```java
RequestExecutor executor = ApiServiceFactory.getRequestExecutor();

RequestDefinition request = RequestDefinition.builder()
    .httpMethod("POST")
    .path("/custom/endpoint")
    .body(complexPayload)
    .build();

Response response = executor.execute(request);
```

---

## Legacy подход: BaseApiService

Для миграции с императивного кода или особых случаев:

### Создание через наследование

```java
public class UserApiService extends BaseApiService {
    
    public UserApiService() {
        super();
    }
    
    public UserApiService(ApiConfig config) {
        super(config);
    }
    
    public UserDto getUserById(int userId) {
        return executeGet("/users/" + userId, UserDto.class);
    }
    
    public UserDto createUser(CreateUserRequest request) {
        return executePost("/users", request, UserDto.class);
    }
    
    public void deleteUser(int userId) {
        executeDelete("/users/" + userId);
    }
    
    // Сложные сценарии через прямой доступ к RestAssured
    public Response customRequest(String payload) {
        return newRequest()
            .header("X-Custom", "value")
            .body(payload)
            .when()
            .post("/custom/endpoint");
    }
}
```

### Использование через фабрику

```java
UserApiService userService = ApiServiceFactory.createLegacy(UserApiService.class);
UserDto user = userService.getUserById(1);
```

### Доступные helper методы в BaseApiService

| Метод | Описание |
|-------|----------|
| `executeGet(path, responseType)` | GET запрос с типизированным ответом |
| `executePost(path, body, responseType)` | POST запрос с телом |
| `executePut(path, body, responseType)` | PUT запрос |
| `executePatch(path, body, responseType)` | PATCH запрос |
| `executeDelete(path)` | DELETE запрос |
| `executeWithRetry(supplier)` | Выполнение с повторами |
| `newRequest()` | Получить новую RequestSpecification |
| `getRequestSpecification()` | Получить базовую спецификацию |

---

## Лучшие практики

### 1. Структура проекта

```
src/
├── main/java/
│   └── com/company/hex/project/
│       └── api/
│           ├── dto/                 # DTO классы
│           │   ├── UserDto.java
│           │   └── CreateUserRequest.java
│           ├── services/            # API интерфейсы
│           │   ├── UserApi.java
│           │   └── ProductApi.java
│           └── interceptors/        # Кастомные интерцепторы
│               └── AuthInterceptor.java
└── test/java/
    └── com/company/hex/project/
        └── tests/api/
            ├── UserApiTest.java
            └── ProductApiTest.java
```

### 2. Переиспользование API инстансов

```java
// В базовом тесте
public abstract class BaseApiTest {
    
    protected UserApi userApi;
    protected ProductApi productApi;
    
    @BeforeEach
    void setUp() {
        userApi = ApiServiceFactory.create(UserApi.class);
        productApi = ApiServiceFactory.create(ProductApi.class);
    }
}
```

### 3. Кастомная фабрика для проекта

```java
public final class ProjectApiFactory {
    
    private static final ApiConfig CONFIG = HexConfigFactory.getConfig(ApiConfig.class);
    private static final Interceptor AUTH_INTERCEPTOR = new JwtAuthInterceptor(getToken());
    
    public static <T> T create(Class<T> serviceInterface) {
        return ApiServiceFactory.builder()
            .withConfig(CONFIG)
            .addInterceptorFirst(AUTH_INTERCEPTOR)
            .create(serviceInterface);
    }
    
    private static String getToken() {
        // Логика получения токена
    }
}

// Использование
UserApi userApi = ProjectApiFactory.create(UserApi.class);
```

### 4. Именование методов

```java
public interface UserApi {
    
    // GET - используйте get, find, search, list
    List<UserDto> getAllUsers();
    UserDto getUserById(@Path("id") int id);
    List<UserDto> findUsersByStatus(@Query("status") String status);
    List<UserDto> searchUsers(@Query("q") String query);
    
    // POST - используйте create, add
    UserDto createUser(@Body CreateUserRequest request);
    
    // PUT - используйте update
    UserDto updateUser(@Path("id") int id, @Body UpdateRequest request);
    
    // DELETE - используйте delete, remove
    void deleteUser(@Path("id") int id);
    
    // PATCH - используйте patch, update*Field
    UserDto patchUser(@Path("id") int id, @Body PatchRequest patch);
}
```

### 5. Документирование API интерфейсов

```java
/**
 * API для работы с пользователями.
 * 
 * <p>Базовый URL: https://api.example.com
 * <p>Документация: https://api.example.com/docs
 * 
 * @author Team Name
 * @version 1.0
 */
@ApiService(baseUrl = "${api.base.url}")
public interface UserApi {
    
    /**
     * Получить пользователя по ID.
     * 
     * @param userId ID пользователя (positive integer)
     * @return информация о пользователе
     * @throws ApiResponseException если пользователь не найден (404)
     */
    @GET("/users/{id}")
    UserDto getUserById(@Path("id") int userId);
}
```

### 6. Тестирование ошибочных сценариев

```java
@Test
void shouldHandleNotFoundError() {
    assertThatThrownBy(() -> userApi.getUserById(99999))
        .isInstanceOf(ApiResponseException.class);
}

@Test
void shouldValidateRequiredFields() {
    CreateUserRequest invalidRequest = CreateUserRequest.builder()
        .name(null)  // Обязательное поле
        .build();
    
    assertThatThrownBy(() -> userApi.createUser(invalidRequest))
        .isInstanceOf(ApiResponseException.class)
        .hasMessageContaining("400");
}
```

---

## Заключение

Декларативный API клиент Hex Framework предоставляет:

✅ **Минимум boilerplate** — определяйте контракты через аннотированные интерфейсы  
✅ **Type Safety** — compile-time проверка типов  
✅ **Расширяемость** — кастомные интерцепторы, конвертеры, executors  
✅ **Потокобезопасность** — параллельное выполнение из коробки  
✅ **Интеграция с Allure** — автоматическое логирование запросов/ответов  
✅ **Гибкость** — escape hatch для сложных сценариев  
✅ **Backward Compatibility** — поддержка legacy BaseApiService подхода

Для большинства задач API тестирования достаточно:

1. Создать DTO классы с Lombok и Jackson аннотациями
2. Определить API интерфейс с HTTP аннотациями
3. Вызвать `ApiServiceFactory.create(MyApi.class)`
4. Использовать методы интерфейса как обычные Java методы

---

*Документация актуальна для Hex Framework версии 1.0*