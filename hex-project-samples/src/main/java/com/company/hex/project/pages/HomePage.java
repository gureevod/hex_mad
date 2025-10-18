package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.TextElement;

/**
 * Page Object для домашней страницы.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Page(url = "/home", title = "Home Page")
public class HomePage extends BasePage {
    
    @Element(name = "Welcome Message", xpath = "//h1[@class='welcome']")
    TextElement welcomeMessage;
}