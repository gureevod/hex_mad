package com.company.hex.ui.interceptor;

import com.codeborne.selenide.Selenide;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Interceptor для автоматического создания скриншота при ошибке.
 * Скриншот автоматически прикрепляется к Allure отчету.
 * 
 * <p>Использование:</p>
 * <pre>
 * {@code
 * @BeforeAll
 * static void setUp() {
 *     ElementInterceptorRegistry.register(new ScreenshotOnErrorInterceptor());
 * }
 * 
 * @AfterAll
 * static void tearDown() {
 *     ElementInterceptorRegistry.clear();
 * }
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ScreenshotOnErrorInterceptor implements ElementInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotOnErrorInterceptor.class);
    
    @Override
    public void onError(BaseElement element, String actionName, Exception exception) {
        try {
            byte[] screenshot = Selenide.screenshot(OutputType.BYTES);
            if (screenshot != null) {
                String name = String.format("Ошибка_%s_%s", 
                    element.getName().replaceAll("\\s+", "_"), 
                    actionName);
                
                Allure.addAttachment(name, "image/png", 
                    new ByteArrayInputStream(screenshot), ".png");
                
                logger.debug("Скриншот создан для ошибки {} на элементе '{}'", 
                    actionName, element.getName());
            }
        } catch (Exception e) {
            logger.warn("Не удалось создать скриншот: {}", e.getMessage());
        }
    }
    
    @Override
    public int getOrder() {
        return 10; // Выполняется рано для захвата состояния до других interceptors
    }
}