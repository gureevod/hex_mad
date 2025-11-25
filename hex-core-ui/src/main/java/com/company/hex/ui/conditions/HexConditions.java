package com.company.hex.ui.conditions;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

/**
 * Дополнительные условия для проверки UI элементов.
 * Расширяет стандартные Selenide Condition дополнительными проверками.
 * 
 * <p>Примеры использования:</p>
 * <pre>
 * {@code
 * import static com.company.hex.ui.conditions.HexConditions.*;
 * 
 * button.shouldBe(hasClass("active"));
 * button.shouldBe(beDisabled());
 * input.shouldBe(hasPlaceholder("Введите email"));
 * input.shouldBe(attributeMatches("data-id", "user-\\d+"));
 * element.shouldBe(inViewport());
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public final class HexConditions {
    
    private HexConditions() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Проверить, что элемент имеет указанный CSS класс.
     * Проверяется точное совпадение класса, а не подстрока.
     * 
     * @param className имя CSS класса
     * @return условие
     */
    public static WebElementCondition hasClass(String className) {
        return new WebElementCondition("имеет класс '" + className + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String classes = element.getAttribute("class");
                if (classes == null) {
                    return CheckResult.rejected("Нет атрибута class", null);
                }
                
                // Проверяем точное совпадение класса, не подстроки
                for (String cls : classes.split("\\s+")) {
                    if (cls.equals(className)) {
                        return CheckResult.accepted();
                    }
                }
                return CheckResult.rejected("Класс '" + className + "' не найден в: " + classes, classes);
            }
        };
    }
    
    /**
     * Проверить, что элемент НЕ имеет указанный CSS класс.
     * 
     * @param className имя CSS класса
     * @return условие
     */
    public static WebElementCondition notHasClass(String className) {
        return new WebElementCondition("не имеет класс '" + className + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String classes = element.getAttribute("class");
                if (classes == null) {
                    return CheckResult.accepted();
                }
                
                for (String cls : classes.split("\\s+")) {
                    if (cls.equals(className)) {
                        return CheckResult.rejected("Класс '" + className + "' найден", classes);
                    }
                }
                return CheckResult.accepted();
            }
        };
    }
    
    /**
     * Проверить, что элемент имеет data-атрибут с указанным значением.
     * 
     * @param dataAttr имя data-атрибута (без префикса 'data-')
     * @param value ожидаемое значение
     * @return условие
     */
    public static WebElementCondition hasDataAttribute(String dataAttr, String value) {
        String fullAttr = "data-" + dataAttr;
        return new WebElementCondition("имеет " + fullAttr + " = '" + value + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String attrValue = element.getAttribute(fullAttr);
                if (value.equals(attrValue)) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Атрибут " + fullAttr + " = '" + attrValue + "'", attrValue);
            }
        };
    }
    
    /**
     * Проверить, что элемент имеет data-атрибут (любое значение).
     * 
     * @param dataAttr имя data-атрибута (без префикса 'data-')
     * @return условие
     */
    public static WebElementCondition hasDataAttribute(String dataAttr) {
        String fullAttr = "data-" + dataAttr;
        return new WebElementCondition("имеет " + fullAttr) {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String attrValue = element.getAttribute(fullAttr);
                if (attrValue != null) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Атрибут " + fullAttr + " отсутствует", null);
            }
        };
    }
    
    /**
     * Проверить, что атрибут элемента соответствует регулярному выражению.
     * 
     * @param attr имя атрибута
     * @param regex регулярное выражение
     * @return условие
     */
    public static WebElementCondition attributeMatches(String attr, String regex) {
        return new WebElementCondition("атрибут '" + attr + "' соответствует '" + regex + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String value = element.getAttribute(attr);
                if (value != null && value.matches(regex)) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Атрибут " + attr + " = '" + value + "' не соответствует regex", value);
            }
        };
    }
    
    /**
     * Проверить, что элемент имеет указанное CSS свойство со значением.
     * 
     * @param property имя CSS свойства
     * @param value ожидаемое значение
     * @return условие
     */
    public static WebElementCondition hasCssValue(String property, String value) {
        return new WebElementCondition("имеет CSS " + property + " = '" + value + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String cssValue = element.getCssValue(property);
                if (value.equals(cssValue)) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("CSS " + property + " = '" + cssValue + "'", cssValue);
            }
        };
    }
    
    /**
     * Проверить, что CSS свойство соответствует регулярному выражению.
     * 
     * @param property имя CSS свойства
     * @param regex регулярное выражение
     * @return условие
     */
    public static WebElementCondition cssValueMatches(String property, String regex) {
        return new WebElementCondition("CSS " + property + " соответствует '" + regex + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String cssValue = element.getCssValue(property);
                if (cssValue != null && cssValue.matches(regex)) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("CSS " + property + " = '" + cssValue + "' не соответствует regex", cssValue);
            }
        };
    }
    
    /**
     * Проверить, что элемент находится в viewport (видим пользователю).
     * Использует JavaScript для проверки позиции элемента.
     * 
     * @return условие
     */
    public static WebElementCondition inViewport() {
        return new WebElementCondition("в viewport") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                try {
                    Rectangle rect = element.getRect();
                    JavascriptExecutor js = (JavascriptExecutor) driver.getWebDriver();
                    
                    Long windowHeight = (Long) js.executeScript("return window.innerHeight;");
                    Long windowWidth = (Long) js.executeScript("return window.innerWidth;");
                    
                    boolean inViewport = rect.getX() >= 0 && rect.getY() >= 0 
                        && rect.getX() + rect.getWidth() <= windowWidth
                        && rect.getY() + rect.getHeight() <= windowHeight;
                    
                    if (inViewport) {
                        return CheckResult.accepted();
                    }
                    return CheckResult.rejected("Элемент вне viewport", String.format(
                        "position: (%d, %d), size: %dx%d, viewport: %dx%d", 
                        rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), 
                        windowWidth, windowHeight));
                } catch (Exception e) {
                    return CheckResult.rejected("Ошибка проверки viewport: " + e.getMessage(), null);
                }
            }
        };
    }
    
    /**
     * Проверить, что элемент disabled (для кнопок, input и т.д.).
     * 
     * @return условие
     */
    public static WebElementCondition beDisabled() {
        return new WebElementCondition("отключен") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                boolean disabled = !element.isEnabled() 
                    || "true".equals(element.getAttribute("disabled"))
                    || element.getAttribute("disabled") != null;
                
                if (disabled) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент активен", "enabled");
            }
        };
    }
    
    /**
     * Проверить, что элемент enabled (активен).
     * 
     * @return условие
     */
    public static WebElementCondition beEnabled() {
        return new WebElementCondition("активен") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                boolean enabled = element.isEnabled() 
                    && !"true".equals(element.getAttribute("disabled"))
                    && element.getAttribute("disabled") == null;
                
                if (enabled) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент отключен", "disabled");
            }
        };
    }
    
    /**
     * Проверить, что элемент readonly.
     * 
     * @return условие
     */
    public static WebElementCondition beReadonly() {
        return new WebElementCondition("только для чтения") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                boolean readonly = "true".equals(element.getAttribute("readonly"))
                    || element.getAttribute("readonly") != null;
                
                if (readonly) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент не readonly", "editable");
            }
        };
    }
    
    /**
     * Проверить, что элемент не readonly.
     * 
     * @return условие
     */
    public static WebElementCondition notBeReadonly() {
        return new WebElementCondition("не только для чтения") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                boolean notReadonly = !"true".equals(element.getAttribute("readonly"))
                    && element.getAttribute("readonly") == null;
                
                if (notReadonly) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент readonly", "readonly");
            }
        };
    }
    
    /**
     * Проверить, что элемент пустой (нет текста и value).
     * 
     * @return условие
     */
    public static WebElementCondition beEmpty() {
        return new WebElementCondition("пустой") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String text = element.getText();
                String value = element.getAttribute("value");
                boolean empty = (text == null || text.trim().isEmpty()) 
                    && (value == null || value.trim().isEmpty());
                
                if (empty) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент не пустой", 
                    "text='" + text + "', value='" + value + "'");
            }
        };
    }
    
    /**
     * Проверить, что элемент не пустой (имеет текст или value).
     * 
     * @return условие
     */
    public static WebElementCondition notBeEmpty() {
        return new WebElementCondition("не пустой") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String text = element.getText();
                String value = element.getAttribute("value");
                boolean notEmpty = (text != null && !text.trim().isEmpty()) 
                    || (value != null && !value.trim().isEmpty());
                
                if (notEmpty) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент пустой", "empty");
            }
        };
    }
    
    /**
     * Проверить placeholder текстового поля.
     * 
     * @param placeholder ожидаемый placeholder
     * @return условие
     */
    public static WebElementCondition hasPlaceholder(String placeholder) {
        return new WebElementCondition("имеет placeholder '" + placeholder + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String actualPlaceholder = element.getAttribute("placeholder");
                if (placeholder.equals(actualPlaceholder)) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("placeholder = '" + actualPlaceholder + "'", actualPlaceholder);
            }
        };
    }
    
    /**
     * Проверить, что placeholder содержит текст.
     * 
     * @param text текст который должен содержаться в placeholder
     * @return условие
     */
    public static WebElementCondition placeholderContains(String text) {
        return new WebElementCondition("placeholder содержит '" + text + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String placeholder = element.getAttribute("placeholder");
                if (placeholder != null && placeholder.contains(text)) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("placeholder = '" + placeholder + "'", placeholder);
            }
        };
    }
    
    /**
     * Проверить, что элемент имеет указанный тип (для input).
     * 
     * @param type тип input
     * @return условие
     */
    public static WebElementCondition hasType(String type) {
        return new WebElementCondition("имеет тип '" + type + "'") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                String actualType = element.getAttribute("type");
                if (type.equals(actualType)) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("type = '" + actualType + "'", actualType);
            }
        };
    }
    
    /**
     * Проверить, что элемент required (обязательный для заполнения).
     * 
     * @return условие
     */
    public static WebElementCondition beRequired() {
        return new WebElementCondition("обязателен") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                boolean required = "true".equals(element.getAttribute("required"))
                    || element.getAttribute("required") != null;
                
                if (required) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент не required", "optional");
            }
        };
    }
    
    /**
     * Проверить, что элемент checked (для checkbox и radio).
     * 
     * @return условие
     */
    public static WebElementCondition beChecked() {
        return new WebElementCondition("отмечен") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                if (element.isSelected()) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент не отмечен", "unchecked");
            }
        };
    }
    
    /**
     * Проверить, что элемент unchecked (для checkbox и radio).
     * 
     * @return условие
     */
    public static WebElementCondition beUnchecked() {
        return new WebElementCondition("не отмечен") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                if (!element.isSelected()) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected("Элемент отмечен", "checked");
            }
        };
    }
}