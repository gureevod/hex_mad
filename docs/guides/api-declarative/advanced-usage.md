# Продвинутое использование

В этом разделе описаны возможности кастомизации и расширения декларативного API клиента.

## Конфигурация

### Базовая конфигурация

По умолчанию `ApiServiceFactory` использует настройки из `hex.properties` (или `hex-test.properties`). Вы можете переопределить их для конкретного сервиса, передав объект `ApiConfig`.

```java
ApiConfig customConfig = new ApiConfig() {
    @Override
    public String baseUrl() {
        return "https://staging-api.example.com";
    }
    // ... другие методы
};

UserApi userApi = ApiServiceFactory.create(UserApi.class, customConfig);
```

### Использование плейсхолдеров

В аннотации `@ApiService` можно использовать плейсхолдеры для подстановки значений из системных свойств (`System.getProperty`).

```java
@ApiService(baseUrl = "${api.base.url}")
public interface UserApi { ... }
```

## Интерцепторы (Interceptors)

Интерцепторы позволяют перехватывать и модифицировать запросы и ответы. Это мощный механизм для логирования, аутентификации, обработки ошибок и модификации заголовков.

### Создание своего интерцептора

Реализуйте интерфейс `Interceptor`.

```java
import com.company.hex.api.interceptor.Interceptor;
import io.restassured.response.Response;

public class AuthInterceptor implements Interceptor {
    private final String token;

    public AuthInterceptor(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) {
        RequestDefinition request = chain.request();
        
        // Модифицируем запрос: добавляем заголовок авторизации
        request.getHeaders().put("Authorization", "Bearer " + token);
        
        // Продолжаем выполнение цепочки
        return chain.proceed(request);
    }
}
```

### Регистрация интерцептора

*Примечание: В текущей версии (v1.0) добавление кастомных интерцепторов требует использования `ApiServiceFactory.Builder` (который планируется к реализации) или модификации фабрики. В качестве временного решения можно использовать наследование от `BaseApiService` для императивного стиля, если требуется сложная логика перехвата, недоступная через аннотации.*

## "Escape Hatch" (Аварийный люк)

Если декларативного подхода недостаточно (например, для очень сложной валидации схемы или специфичных проверок RestAssured), вы всегда можете получить доступ к низкоуровневым инструментам.

### Получение RequestSpecification

```java
import com.company.hex.api.service.ApiServiceFactory;
import io.restassured.specification.RequestSpecification;

// Получаем преднастроенный спецификацию (с URL, логгером и т.д.)
RequestSpecification spec = ApiServiceFactory.getRequestSpecification();

spec.given()
    .queryParam("complex", "value")
    .when()
    .get("/endpoint")
    .then()
    .statusCode(200);
```

### Получение RequestExecutor

```java
import com.company.hex.api.executor.RequestExecutor;
import com.company.hex.api.model.RequestDefinition;

RequestExecutor executor = ApiServiceFactory.getRequestExecutor();

// Ручное создание определения запроса (обычно не требуется)
RequestDefinition def = RequestDefinition.builder()
    .httpMethod("GET")
    .path("/users")
    .build();

executor.execute(def);
```

## Расширение функциональности

### Кастомные конвертеры

По умолчанию используется `JacksonResponseConverter`. Если вам нужно поддерживать XML или другой формат, вы можете реализовать свой `ResponseConverter`.

*В текущей версии замена конвертера требует доработки `ApiServiceFactory`.*

### Кастомные аннотации

Вы можете создавать свои аннотации и обрабатывать их, расширив `AnnotationProcessor`. Это требует глубокого понимания внутренней архитектуры фреймворка.