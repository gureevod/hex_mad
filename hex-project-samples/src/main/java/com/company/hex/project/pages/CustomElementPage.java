package com.company.hex.project.pages;

import com.company.hex.project.elements.CustomButton;
import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;

@Page(url = "/custom", title = "Custom Element Page")
public class CustomElementPage extends BasePage {

    @Element(name = "My Custom Button", xpath = "//button[@id='custom']")
    public CustomButton customButton;

}