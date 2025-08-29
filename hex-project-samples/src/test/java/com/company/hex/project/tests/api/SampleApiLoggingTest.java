package com.company.hex.project.tests.api;

import com.company.hex.api.logging.ApiStepHelper;
import com.company.hex.api.service.BaseApiService;
import com.company.hex.core.logging.CorrelationIdManager;
import com.company.hex.testing.allure.AllureLifecycleListener;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Пример API теста с демонстрацией логирования и Allure интеграции.
 * Показывает использование всех возможностей фреймворка для API тестирования.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(AllureLifecycleListener.class)
@Epic("Sample Tests")
@Feature("API Logging Demo")
@DisplayName("Демонстрация API логирования и отчетности")
class SampleApiLoggingTest {
    
    private TestApiService apiService;
    
    @BeforeEach
    void setUp() {
        // Инициализируем тестовый API сервис
        apiService = new TestApiService();
        
        // Устанавливаем контекст теста
        CorrelationIdManager.initializeTestContext(
            this.getClass().getSimpleName(), 
            "API Logging Demo Test"
        );
    }
    
    @Test
    @Story("GET Request Logging")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Демонстрация логирования GET запроса")
    @Description("Тест демонстрирует автоматическое логирование GET запроса с прикреплением к Allure отчету")
    void demonstrateGetRequestLogging() {
        // Выполняем GET запрос с автоматическим логированием
        Response response = ApiStepHelper.get("/posts/1", apiService.getRequestSpec());
        
        // Проверяем статус код с логированием
        ApiStepHelper.verifyStatusCode(response, 200);
        
        // Проверяем время ответа
        ApiStepHelper.verifyResponseTime(response, 5000);
        
        // Извлекаем данные из ответа
        String title = ApiStepHelper.extractFromResponse(response, "title");
        Integer userId = ApiStepHelper.extractFromResponse(response, "userId");
        
        // Выполняем проверки
        assertThat(title).isNotNull().isNotEmpty();
        assertThat(userId).isNotNull().isPositive();
        
        // Добавляем дополнительную информацию в отчет
        Allure.addAttachment("Response Title", title);
        Allure.addAttachment("User ID", String.valueOf(userId));
    }
    
    @Test
    @Story("POST Request Logging")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Демонстрация логирования POST запроса с JSON телом")
    @Description("Тест показывает логирование POST запроса с JSON данными и автоматическое прикрепление к отчету")
    void demonstratePostRequestLogging() {
        // Подготавливаем тестовые данные
        TestPostData postData = new TestPostData();
        postData.setTitle("Sample Post Title");
        postData.setBody("This is a sample post body for testing purposes");
        postData.setUserId(1);
        
        // Выполняем POST запрос с JSON телом
        Response response = ApiStepHelper.requestWithJsonBody(
            "POST", "/posts", apiService.getRequestSpec(), postData
        );
        
        // Проверяем успешное создание
        ApiStepHelper.verifyStatusCode(response, 201);
        
        // Извлекаем ID созданного поста
        Integer createdId = ApiStepHelper.extractFromResponse(response, "id");
        
        // Проверяем, что ID был присвоен
        assertThat(createdId).isNotNull().isPositive();
        
        // Добавляем информацию о созданном ресурсе
        Allure.parameter("Created Post ID", createdId);
        Allure.addAttachment("Created Post Data", 
                           String.format("ID: %d\nTitle: %s\nBody: %s", 
                                       createdId, postData.getTitle(), postData.getBody()));
    }
    
    @Test
    @Story("Error Handling Logging")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Демонстрация логирования ошибок API")
    @Description("Тест показывает как фреймворк логирует и обрабатывает ошибки API")
    void demonstrateErrorLogging() {
        // Выполняем запрос к несуществующему ресурсу
        Response response = ApiStepHelper.get("/posts/99999", apiService.getRequestSpec());
        
        // Проверяем код ошибки
        ApiStepHelper.verifyStatusCode(response, 404);
        
        // Добавляем информацию об ошибке
        Allure.addAttachment("Error Response", response.getBody().asString());
        Allure.addAttachment("Response Headers", response.getHeaders().toString());
    }
    
    @Test
    @Story("Query Parameters Logging")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Демонстрация логирования запросов с параметрами")
    @Description("Тест показывает логирование GET запроса с query параметрами")
    void demonstrateQueryParametersLogging() {
        // Подготавливаем параметры запроса
        java.util.Map<String, Object> queryParams = new java.util.HashMap<>();
        queryParams.put("userId", 1);
        queryParams.put("_limit", 5);
        
        // Выполняем запрос с параметрами
        Response response = ApiStepHelper.requestWithQueryParams(
            "GET", "/posts", apiService.getRequestSpec(), queryParams
        );
        
        // Проверяем успешный ответ
        ApiStepHelper.verifyStatusCode(response, 200);
        
        // Проверяем, что вернулось не более 5 элементов
        java.util.List<?> posts = response.jsonPath().getList("$");
        assertThat(posts).hasSizeLessThanOrEqualTo(5);
        
        // Добавляем информацию о результатах
        Allure.addAttachment("Query Parameters", queryParams.toString());
        Allure.addAttachment("Results Count", String.valueOf(posts.size()));
    }
    
    /**
     * Тестовый API сервис для демонстрации.
     */
    private static class TestApiService extends BaseApiService {
        // Используем публичный API для демонстрации
        // В реальных тестах здесь будет ваш API
    }
    
    /**
     * Тестовые данные для POST запроса.
     */
    private static class TestPostData {
        private String title;
        private String body;
        private Integer userId;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
    }
}