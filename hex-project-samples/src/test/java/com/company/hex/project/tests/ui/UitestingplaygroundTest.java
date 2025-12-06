package com.company.hex.project.tests.ui;

import com.company.hex.project.annotations.UiTest;
import com.company.hex.project.pages.SamplePage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
public class UitestingplaygroundTest {

    @Test
    @Story("Login Flow")
    @DisplayName("Успешный вход в систему")
    @Description("Демонстрирует простой flow авторизации на UI")
    @Severity(SeverityLevel.CRITICAL)
    public void testSuccessfulLogin() {
        SamplePage page = new SamplePage();
        page.open();
        page.username.fill("user");
        page.password.fill("pwd");
        page.loginButton.click();
        page.successMessage.shouldHaveExactText("Welcome, user!");
        page.successMessage.click();
        page.loginButton.click();
        System.out.println();
    }
}
