package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;

/**
 * <class comment/>
 */
public class SignupForm extends SeleniumForm
{
    public SignupForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return "signup";
    }

    public String[] getFieldNames()
    {
        return new String[]{"newUser.login", "newUser.name", "newUser.password", "confirm"};
    }
}
