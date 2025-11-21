package com.company.hex.project.annotations;


import com.company.hex.project.junit5.UiExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(UiExtension.class) // Подключаем наше расширение
public @interface UiTest {
}
