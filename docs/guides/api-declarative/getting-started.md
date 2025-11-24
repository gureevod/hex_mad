# Быстрый старт: Декларативный API клиент

## Введение

Декларативный API клиент в Hex Framework позволяет описывать взаимодействие с REST API, используя простые Java интерфейсы и аннотации. Этот подход вдохновлен библиотекой Retrofit и призван сократить количество шаблонного кода (boilerplate), повысить читаемость тестов и обеспечить строгую типизацию.

Вместо того чтобы писать длинные цепочки вызовов RestAssured в каждом тесте, вы один раз описываете контракт API в интерфейсе, и фреймворк автоматически создает реализацию для выполнения запросов.

## Зачем это использовать?

*   **Меньше кода:** Описание запроса занимает 1-2 строки аннотаций вместо 5-10 строк кода.
*   **Читаемость:** Интерфейс служит документацией к вашему API.
*   **Типизация:** Автоматическая конвертация JSON ответов в Java объекты (DTO).
*   **Надежность:** Встроенные механизмы повторных попыток (Retry) и валидации статусов.

## Пример использования

### 1. Создайте DTO (Data Transfer Object)

Сначала опишите структуру данных, которую вы ожидаете получить от API.

```java
public class UserDto {
    private Long id;
    private String name;
    private String email;
    
    // Геттеры, сеттеры, конструкторы
}
```

### 2. Объявите интерфейс сервиса

Создайте интерфейс и опишите методы API с помощью аннотаций.

```java
package com.company.hex.project.api.services;

import com.company.hex.api.annotations.config.ApiService;
import com.company.hex.api.annotations.http.GET;
import com.company.hex.api.annotations.param.Path;
import java.util.List;

// Базовый URL и путь для всех методов интерфейса
@ApiService(baseUrl = "${api.base.url}", basePath = "/api/v1")
public interface UserApi {

    @GET("/users")
    List<UserDto> getAllUsers();

    @GET("/users/{id}")
    UserDto getUserById(@Path("id") Long id);
}
```

### 3. Используйте сервис в тесте

Используйте `ApiServiceFactory` для создания экземпляра сервиса и вызова методов.

```java
import com.company.hex.api.service.ApiServiceFactory;
import org.junit.jupiter.api.Test;

public class UserApiTest {

    @Test
    public void testGetUsers() {
        // Создаем прокси-реализацию интерфейса
        UserApi userApi = ApiServiceFactory.create(UserApi.class);

        // Выполняем запрос и получаем типизированный ответ
        List<UserDto> users = userApi.getAllUsers();

        // Проверяем результат
        assert !users.isEmpty();
    }
}
```

## Что дальше?

*   Изучите [Основные возможности](basic-usage.md) для работы с различными типами запросов и параметров.
*   Посмотрите [Продвинутое использование](advanced-usage.md) для настройки конфигурации и обработки ошибок.
*   Ознакомьтесь с [Лучшими практиками](best-practices.md) для написания чистого и поддерживаемого кода.