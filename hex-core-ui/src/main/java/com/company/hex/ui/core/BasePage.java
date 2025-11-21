package com.company.hex.ui.core;

import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.factory.FieldInitializer;
import com.company.hex.ui.config.UiConfig;
import com.company.hex.core.config.HexConfigFactory;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.open;

/**
 * Базовый абстрактный класс для всех Page Objects.
 * Предоставляет:
 * - Автоматическую инициализацию полей с аннотациями @Element, @Elements, @Component
 * - Методы для открытия и проверки страницы
 * - Контекстное логирование
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Page(url = "/login", title = "Login Page")
 * public class LoginPage extends BasePage {
 *     
 *     @Element(name = "Username", xpath = "//input[@id='username']")
 *     Input username;
 *     
 *     @Element(name = "Password", xpath = "//input[@id='password']")
 *     Input password;
 *     
 *     @Step("Login as {user}")
 *     public HomePage login(String user, String pass) {
 *         username.fill(user);
 *         password.fill(pass);
 *         loginButton.click();
 *         return new HomePage();
 *     }
 * }
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("unchecked") // Подавляем варнинги для Fluent API
public abstract class BasePage {
    
    protected final Logger logger;
    protected final String pageName;
    protected final String pageUrl;
    protected final String pageTitle;
    
    /**
     * Конструктор базовой страницы.
     * Автоматически инициализирует все поля с аннотациями.
     */
    protected BasePage() {
        this.logger = HexLoggerFactory.getUiLogger(getClass());

        Page pageAnnotation = getClass().getAnnotation(Page.class);
        if (pageAnnotation != null) {
            this.pageName = pageAnnotation.title();
            this.pageUrl = pageAnnotation.url();
            this.pageTitle = pageAnnotation.title();
        } else {
            this.pageName = getClass().getSimpleName();
            this.pageUrl = "";
            this.pageTitle = "";
            logger.warn("Класс {} не имеет аннотации @Page", getClass().getName());
        }

        // Инициализация полей
        FieldInitializer.initializeFields(this, pageName, null, null);
    }
    
    /**
     * Открыть страницу по URL из аннотации @Page.
     * 
     * @return this для fluent API
     */
    @Step("Открыть страницу '{this.pageName}'")
    public <T extends BasePage> T open() {
        if (pageUrl == null || pageUrl.trim().isEmpty()) {
            throw new IllegalStateException(
                    String.format("Страница '%s' не имеет URL в аннотации @Page", pageName));
        }
        
        logger.info("Открытие страницы '{}' по URL: {}", pageName, pageUrl);
        open(pageUrl);
        logger.info("Страница '{}' успешно открыта", pageName);
        
        return (T) this;
    }
    
    /**
     * Открыть страницу по указанному URL.
     * 
     * @param url URL для открытия
     * @return this для fluent API
     */
    @Step("Открыть страницу '{this.pageName}' по URL: {url}")
    public <T extends BasePage> T open(String url) {
        logger.info("Открытие страницы '{}' по URL: {}", pageName, url);
        Selenide.open(url);
        logger.info("Страница '{}' успешно открыта", pageName);
        
        return (T) this;
    }
    
    /**
     * Проверить, что страница открыта (по URL).
     * 
     * @return true если страница открыта
     */
    public boolean isOpened() {
        if (pageUrl == null || pageUrl.trim().isEmpty()) {
            logger.warn("Невозможно проверить открытие страницы '{}' - URL не указан", pageName);
            return false;
        }
        
        String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
        boolean isOpened = currentUrl.contains(pageUrl);
        
        logger.debug("Проверка открытия страницы '{}': {} (текущий URL: {})",
                pageName, isOpened, currentUrl);
        
        return isOpened;
    }
    
    /**
     * Проверить, что страница открыта (по заголовку).
     * 
     * @return true если заголовок совпадает
     */
    public boolean isTitleCorrect() {
        if (pageTitle == null || pageTitle.trim().isEmpty()) {
            logger.warn("Невозможно проверить заголовок страницы '{}' - title не указан", pageName);
            return false;
        }
        
        String currentTitle = WebDriverRunner.getWebDriver().getTitle();
        boolean isCorrect = currentTitle.contains(pageTitle);
        
        logger.debug("Проверка заголовка страницы '{}': {} (текущий: {})",
                pageName, isCorrect, currentTitle);
        
        return isCorrect;
    }
    
    /**
     * Обновить страницу.
     * 
     * @return this для fluent API
     */
    @Step("Обновить страницу '{this.pageName}'")
    public <T extends BasePage> T refresh() {
        logger.info("Обновление страницы '{}'", pageName);
        Selenide.refresh();
        logger.info("Страница '{}' успешно обновлена", pageName);
        
        return (T) this;
    }
    
    /**
     * Вернуться на предыдущую страницу.
     * 
     * @return this для fluent API
     */
    @Step("Вернуться назад со страницы '{this.pageName}'")
    public <T extends BasePage> T back() {
        logger.info("Возврат назад со страницы '{}'", pageName);
        Selenide.back();
        logger.info("Возврат выполнен");
        
        return (T) this;
    }
    
    /**
     * Перейти вперед.
     * 
     * @return this для fluent API
     */
    @Step("Перейти вперед со страницы '{this.pageName}'")
    public <T extends BasePage> T forward() {
        logger.info("Переход вперед со страницы '{}'", pageName);
        Selenide.forward();
        logger.info("Переход выполнен");
        
        return (T) this;
    }
    
    /**
     * Получить имя страницы.
     * 
     * @return имя страницы
     */
    public String getPageName() {
        return pageName;
    }
    
    /**
     * Получить URL страницы.
     * 
     * @return URL страницы
     */
    public String getPageUrl() {
        return pageUrl;
    }
    
    /**
     * Получить заголовок страницы.
     * 
     * @return заголовок страницы
     */
    public String getPageTitle() {
        return pageTitle;
    }
    
    /**
     * Получить текущий URL браузера.
     * 
     * @return текущий URL
     */
    public String getCurrentUrl() {
        return WebDriverRunner.getWebDriver().getCurrentUrl();
    }
    
    /**
     * Получить текущий заголовок браузера.
     * 
     * @return текущий заголовок
     */
    public String getCurrentTitle() {
        return WebDriverRunner.getWebDriver().getTitle();
    }
    
    @Override
    public String toString() {
        return String.format("%s[name='%s', url='%s']",
                getClass().getSimpleName(), pageName, pageUrl);
    }
}