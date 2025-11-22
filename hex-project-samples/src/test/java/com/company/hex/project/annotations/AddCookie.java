package com.company.hex.project.annotations;


import com.company.hex.project.junit5.CookieExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Аннотация для добавления куки перед тестом.
 * Автоматически открывает браузер, если он закрыт.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(AddCookies.class) // Позволяет вешать несколько аннотаций
@ExtendWith(CookieExtension.class) // Подключает расширение
public @interface AddCookie {

    /** Имя куки */
    String name();

    /** Значение куки */
    String value();

    /**
     * Путь куки. По умолчанию "/"
     */
    String path() default "/";

    /**
     * URL, который нужно открыть перед установкой куки.
     * Если пусто - берется Configuration.baseUrl.
     * Это обязательно, так как Selenium не может ставить куки "в пустоту".
     */
    String url() default "";
}