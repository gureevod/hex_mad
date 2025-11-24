# Основные возможности

В этом разделе описаны основные аннотации и способы их использования для построения API запросов.

## HTTP Методы

Поддерживаются все стандартные HTTP методы. Каждый метод должен быть помечен одной из следующих аннотаций:

*   `@GET("path")`
*   `@POST("path")`
*   `@PUT("path")`
*   `@DELETE("path")`
*   `@PATCH("path")`

Значение аннотации — это относительный путь к ресурсу.

```java
@GET("/users")
List<UserDto> listUsers();

@POST("/users")
UserDto createUser(@Body UserDto user);
```

## Параметры запроса

### Path Parameters (Параметры пути)

Используйте `{name}` в URL и аннотацию `@Path("name")` для подстановки значений в путь.

```java
@GET("/users/{id}/posts/{postId}")
PostDto getPost(@Path("id") Long userId, @Path("postId") Long postId);
```

### Query Parameters (Параметры строки запроса)

Используйте `@Query("name")` для добавления параметров в строку запроса (после `?`).

```java
// Генерирует запрос: /users/search?name=John&age=25
@GET("/users/search")
List<UserDto> search(@Query("name") String name, @Query("age") Integer age);
```

### Headers (Заголовки)

Используйте `@Header("name")` для динамического добавления заголовков.

```java
@GET("/user")
UserDto getUser(@Header("Authorization") String token);
```

### Request Body (Тело запроса)

Используйте `@Body` для передачи объекта в теле запроса. Объект будет автоматически сериализован в JSON.

```java
@POST("/users")
UserDto create(@Body CreateUserRequest request);
```

### Form Parameters (Параметры формы)

Для отправки данных в формате `application/x-www-form-urlencoded` используйте `@FormParam`.

```java
@POST("/login")
TokenDto login(@FormParam("username") String user, @FormParam("password") String pass);
```

## Обработка ответов

### Типизированные ответы

Метод может возвращать любой Java объект (DTO). Фреймворк автоматически десериализует JSON ответ в этот объект.

```java
@GET("/users/{id}")
UserDto getUser(@Path("id") Long id);
```

### Списки

Поддерживается возвращение коллекций объектов.

```java
@GET("/users")
List<UserDto> getAllUsers();
```

### Raw Response (Сырой ответ)

Если вам нужен доступ к заголовкам ответа, кукам или статус-коду, возвращайте объект `Response` (из RestAssured).

```java
import io.restassured.response.Response;

@GET("/users/{id}")
Response getUserRaw(@Path("id") Long id);
```

### Void (Без возвращаемого значения)

Если тело ответа не важно (например, при DELETE), используйте `void`.

```java
@DELETE("/users/{id}")
void deleteUser(@Path("id") Long id);
```

## Валидация статусов

Используйте `@ExpectedStatus` для автоматической проверки HTTP статус-кода. Если сервер вернет другой код, тест упадет с ошибкой.

```java
@DELETE("/users/{id}")
@ExpectedStatus(204) // Ожидаем 204 No Content
void deleteUser(@Path("id") Long id);

@POST("/users")
@ExpectedStatus({200, 201}) // Ожидаем 200 или 201
UserDto create(@Body UserDto user);
```

## Повторные попытки (Retry)

Для нестабильных методов можно настроить автоматические повторные попытки с помощью `@Retry`.

```java
@GET("/flaky-endpoint")
@Retry(count = 5, delay = 2000) // 5 попыток с задержкой 2 секунды
DataDto getData();