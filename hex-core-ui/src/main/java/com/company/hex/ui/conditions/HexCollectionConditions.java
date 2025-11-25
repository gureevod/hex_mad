package com.company.hex.ui.conditions;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementsCondition;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

/**
 * Условия для проверки коллекций элементов.
 * Расширяет стандартные Selenide условия для ElementsCollection.
 * 
 * <p>Примеры использования:</p>
 * <pre>
 * {@code
 * import static com.company.hex.ui.conditions.HexCollectionConditions.*;
 * 
 * // Все элементы имеют класс
 * elements.shouldHave(allHaveClass("active"));
 * 
 * // Все тексты содержат подстроку
 * items.shouldHave(allContainText("Product"));
 * 
 * // Размер в диапазоне
 * list.shouldHave(sizeInRange(5, 10));
 * 
 * // Кастомный предикат
 * buttons.shouldHave(allMatch(
 *     el -> el.isEnabled(), 
 *     "все кнопки активны"
 * ));
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public final class HexCollectionConditions {
    
    private HexCollectionConditions() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Все элементы соответствуют предикату.
     * 
     * @param predicate предикат для проверки
     * @param description описание проверки
     * @return условие
     */
    public static WebElementsCondition allMatch(Predicate<WebElement> predicate, String description) {
        return new WebElementsCondition() {
            @Override
            @Nonnull
            public CheckResult check(Driver driver, List<WebElement> elements) {
                for (int i = 0; i < elements.size(); i++) {
                    if (!predicate.test(elements.get(i))) {
                        return CheckResult.rejected(
                            String.format("Элемент [%d] не соответствует: %s", i, description),
                            elements);
                    }
                }
                return CheckResult.accepted();
            }
            
            @Override
            public String toString() {
                return "все соответствуют: " + description;
            }
        };
    }
    
    /**
     * Хотя бы один элемент соответствует предикату.
     * 
     * @param predicate предикат для проверки
     * @param description описание проверки
     * @return условие
     */
    public static WebElementsCondition anyMatch(Predicate<WebElement> predicate, String description) {
        return new WebElementsCondition() {
            @Override
            @Nonnull
            public CheckResult check(Driver driver, List<WebElement> elements) {
                for (WebElement element : elements) {
                    if (predicate.test(element)) {
                        return CheckResult.accepted();
                    }
                }
                return CheckResult.rejected("Ни один элемент не соответствует: " + description, elements);
            }
            
            @Override
            public String toString() {
                return "хотя бы один соответствует: " + description;
            }
        };
    }
    
    /**
     * Ни один элемент не соответствует предикату.
     * 
     * @param predicate предикат для проверки
     * @param description описание проверки
     * @return условие
     */
    public static WebElementsCondition noneMatch(Predicate<WebElement> predicate, String description) {
        return new WebElementsCondition() {
            @Override
            @Nonnull
            public CheckResult check(Driver driver, List<WebElement> elements) {
                for (int i = 0; i < elements.size(); i++) {
                    if (predicate.test(elements.get(i))) {
                        return CheckResult.rejected(
                            String.format("Элемент [%d] неожиданно соответствует: %s", i, description),
                            elements);
                    }
                }
                return CheckResult.accepted();
            }
            
            @Override
            public String toString() {
                return "ни один не соответствует: " + description;
            }
        };
    }
    
    /**
     * Все элементы имеют указанный класс.
     * 
     * @param className имя CSS класса
     * @return условие
     */
    public static WebElementsCondition allHaveClass(String className) {
        return allMatch(
            el -> {
                String classes = el.getAttribute("class");
                if (classes == null) return false;
                for (String cls : classes.split("\\s+")) {
                    if (cls.equals(className)) {
                        return true;
                    }
                }
                return false;
            },
            "имеют класс '" + className + "'"
        );
    }
    
    /**
     * Все элементы НЕ имеют указанный класс.
     * 
     * @param className имя CSS класса
     * @return условие
     */
    public static WebElementsCondition noneHaveClass(String className) {
        return noneMatch(
            el -> {
                String classes = el.getAttribute("class");
                if (classes == null) return false;
                for (String cls : classes.split("\\s+")) {
                    if (cls.equals(className)) {
                        return true;
                    }
                }
                return false;
            },
            "имеют класс '" + className + "'"
        );
    }
    
    /**
     * Все тексты элементов содержат подстроку.
     * 
     * @param text текст для поиска
     * @return условие
     */
    public static WebElementsCondition allContainText(String text) {
        return allMatch(
            el -> el.getText().contains(text),
            "содержат текст '" + text + "'"
        );
    }
    
    /**
     * Хотя бы один элемент содержит текст.
     * 
     * @param text текст для поиска
     * @return условие
     */
    public static WebElementsCondition anyContainsText(String text) {
        return anyMatch(
            el -> el.getText().contains(text),
            "содержит текст '" + text + "'"
        );
    }
    
    /**
     * Все элементы имеют точный текст.
     * 
     * @param text ожидаемый текст
     * @return условие
     */
    public static WebElementsCondition allHaveExactText(String text) {
        return allMatch(
            el -> text.equals(el.getText()),
            "имеют текст '" + text + "'"
        );
    }
    
    /**
     * Размер коллекции в диапазоне.
     * 
     * @param min минимальный размер (включительно)
     * @param max максимальный размер (включительно)
     * @return условие
     */
    public static WebElementsCondition sizeInRange(int min, int max) {
        return new WebElementsCondition() {
            @Override
            @Nonnull
            public CheckResult check(Driver driver, List<WebElement> elements) {
                int size = elements.size();
                if (size >= min && size <= max) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected(
                    String.format("Ожидался размер в диапазоне [%d, %d], но получен %d", min, max, size),
                    elements);
            }
            
            @Override
            public String toString() {
                return "размер в диапазоне [" + min + ", " + max + "]";
            }
        };
    }
    
    /**
     * Размер коллекции больше указанного.
     * 
     * @param minSize минимальный размер (не включительно)
     * @return условие
     */
    public static WebElementsCondition sizeGreaterThan(int minSize) {
        return new WebElementsCondition() {
            @Override
            @Nonnull
            public CheckResult check(Driver driver, List<WebElement> elements) {
                int size = elements.size();
                if (size > minSize) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected(
                    String.format("Ожидался размер > %d, но получен %d", minSize, size),
                    elements);
            }
            
            @Override
            public String toString() {
                return "размер > " + minSize;
            }
        };
    }
    
    /**
     * Размер коллекции меньше указанного.
     * 
     * @param maxSize максимальный размер (не включительно)
     * @return условие
     */
    public static WebElementsCondition sizeLessThan(int maxSize) {
        return new WebElementsCondition() {
            @Override
            @Nonnull
            public CheckResult check(Driver driver, List<WebElement> elements) {
                int size = elements.size();
                if (size < maxSize) {
                    return CheckResult.accepted();
                }
                return CheckResult.rejected(
                    String.format("Ожидался размер < %d, но получен %d", maxSize, size),
                    elements);
            }
            
            @Override
            public String toString() {
                return "размер < " + maxSize;
            }
        };
    }
    
    /**
     * Все элементы видимы.
     * 
     * @return условие
     */
    public static WebElementsCondition allVisible() {
        return allMatch(
            WebElement::isDisplayed,
            "видимы"
        );
    }
    
    /**
     * Все элементы скрыты.
     * 
     * @return условие
     */
    public static WebElementsCondition allHidden() {
        return allMatch(
            el -> !el.isDisplayed(),
            "скрыты"
        );
    }
    
    /**
     * Все элементы активны (enabled).
     * 
     * @return условие
     */
    public static WebElementsCondition allEnabled() {
        return allMatch(
            WebElement::isEnabled,
            "активны"
        );
    }
    
    /**
     * Все элементы отключены (disabled).
     * 
     * @return условие
     */
    public static WebElementsCondition allDisabled() {
        return allMatch(
            el -> !el.isEnabled(),
            "отключены"
        );
    }
    
    /**
     * Все элементы имеют атрибут с указанным значением.
     * 
     * @param attributeName имя атрибута
     * @param value ожидаемое значение
     * @return условие
     */
    public static WebElementsCondition allHaveAttribute(String attributeName, String value) {
        return allMatch(
            el -> value.equals(el.getAttribute(attributeName)),
            "имеют атрибут " + attributeName + " = '" + value + "'"
        );
    }
    
    /**
     * Все элементы имеют атрибут (любое значение).
     * 
     * @param attributeName имя атрибута
     * @return условие
     */
    public static WebElementsCondition allHaveAttribute(String attributeName) {
        return allMatch(
            el -> el.getAttribute(attributeName) != null,
            "имеют атрибут " + attributeName
        );
    }
}