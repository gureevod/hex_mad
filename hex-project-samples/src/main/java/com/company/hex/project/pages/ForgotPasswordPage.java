package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.Input;

/**
 * Page Object для страницы восстановления пароля.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Page(url = "/forgot-password", title = "Forgot Password Page")
public class ForgotPasswordPage extends BasePage {
    
    @Element(name = "Email", xpath = "//input[@id='email']")
    Input email;
    
    @Element(name = "Reset Button", xpath = "//button[@type='submit']")
    Button resetButton;
}