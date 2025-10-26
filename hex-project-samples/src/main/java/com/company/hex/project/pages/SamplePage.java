package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.Input;
import com.company.hex.ui.elements.TextElement;

@Page(url = "/sampleapp", title = "Sample Page")
public class SamplePage extends BasePage {

    @Element(name = "Юзернейм", xpath = "//input[@name='UserName']")
    public Input username;

    @Element(name = "Пароль", xpath = "//input[@name='Password']")
    public Input password;

    @Element(name = "Кнопка Логин", xpath = "//button[@id='login']")
    public Button loginButton;

    @Element(name = "Сообщение", xpath = "//label[@id='loginstatus']")
    public TextElement successMessage;
}
