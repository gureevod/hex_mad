package com.company.hex.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для декларативного определения UI компонента в Page Object.
 * Компонент - это переиспользуемая группа элементов с собственным root локатором.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Component(name = "Main Header", root = "//header[@id='main-header']")
 * HeaderComponent header;
 * 
 * @Component(name = "Sidebar Navigation", root = "//aside[@id='sidebar']")
 * NavigationComponent sidebar;
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Component {
    
    /**
     * Имя компонента для логирования и Allure steps.
     * Обязательный параметр.
     * 
     * @return имя компонента
     */
    String name();
    
    /**
     * Корневой локатор компонента (xpath или css).
     * Все элементы внутри компонента будут искаться относительно этого локатора.
     * Обязательный параметр.
     * 
     * @return корневой локатор
     */
    String root();
}