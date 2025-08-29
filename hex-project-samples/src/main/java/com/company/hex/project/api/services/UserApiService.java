package com.company.hex.project.api.services;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.api.service.BaseApiService;
import io.restassured.response.Response;

/**
 * Пример API сервиса для работы с пользователями.
 * Демонстрирует использование BaseApiService и ApiServiceFactory.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class UserApiService extends BaseApiService {

    /**
     * Конструктор по умолчанию.
     * Использует конфигурацию по умолчанию.
     */
    public UserApiService() {
        super();
    }

    /**
     * Конструктор с пользовательской конфигурацией.
     * 
     * @param config пользовательская конфигурация API
     */
    public UserApiService(ApiConfig config) {
        super(config);
    }

    /**
     * Получить список всех пользователей.
     * 
     * @return ответ API со списком пользователей
     */
    public Response getAllUsers() {
        logger.info("Получение списка всех пользователей");
        
        return executeWithRetry(() -> 
            newRequest()
                .when()
                .get("/users")
                .then()
                .extract()
                .response()
        );
    }

    /**
     * Получить пользователя по ID.
     * 
     * @param userId идентификатор пользователя
     * @return ответ API с данными пользователя
     */
    public Response getUserById(int userId) {
        logger.info("Получение пользователя с ID: {}", userId);
        
        return executeWithRetry(() -> 
            newRequest()
                .pathParam("userId", userId)
                .when()
                .get("/users/{userId}")
                .then()
                .extract()
                .response()
        );
    }

    /**
     * Создать нового пользователя.
     * 
     * @param userJson JSON данные пользователя
     * @return ответ API с созданным пользователем
     */
    public Response createUser(String userJson) {
        logger.info("Создание нового пользователя");
        
        return executeWithRetry(() -> 
            newRequest()
                .body(userJson)
                .when()
                .post("/users")
                .then()
                .extract()
                .response()
        );
    }

    /**
     * Обновить существующего пользователя.
     * 
     * @param userId идентификатор пользователя
     * @param userJson JSON данные для обновления
     * @return ответ API с обновленным пользователем
     */
    public Response updateUser(int userId, String userJson) {
        logger.info("Обновление пользователя с ID: {}", userId);
        
        return executeWithRetry(() -> 
            newRequest()
                .pathParam("userId", userId)
                .body(userJson)
                .when()
                .put("/users/{userId}")
                .then()
                .extract()
                .response()
        );
    }

    /**
     * Удалить пользователя по ID.
     * 
     * @param userId идентификатор пользователя
     * @return ответ API об удалении
     */
    public Response deleteUser(int userId) {
        logger.info("Удаление пользователя с ID: {}", userId);
        
        return executeWithRetry(() -> 
            newRequest()
                .pathParam("userId", userId)
                .when()
                .delete("/users/{userId}")
                .then()
                .extract()
                .response()
        );
    }

    /**
     * Поиск пользователей по имени.
     * 
     * @param name имя для поиска
     * @return ответ API со списком найденных пользователей
     */
    public Response searchUsersByName(String name) {
        logger.info("Поиск пользователей по имени: {}", name);
        
        return executeWithRetry(() -> 
            newRequest()
                .queryParam("name", name)
                .when()
                .get("/users/search")
                .then()
                .extract()
                .response()
        );
    }

    /**
     * Получить профиль текущего пользователя (требует аутентификации).
     * 
     * @return ответ API с профилем пользователя
     */
    public Response getCurrentUserProfile() {
        logger.info("Получение профиля текущего пользователя");
        
        return executeWithRetry(() -> 
            newRequest()
                .when()
                .get("/users/me")
                .then()
                .extract()
                .response()
        );
    }

    /**
     * Проверить доступность API пользователей.
     * 
     * @return ответ API о состоянии сервиса
     */
    public Response checkApiHealth() {
        logger.info("Проверка доступности API пользователей");
        
        return executeWithRetry(() -> 
            newRequest()
                .when()
                .get("/users/health")
                .then()
                .extract()
                .response()
        );
    }
}