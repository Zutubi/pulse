package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.prototype.config.user.SignupUserConfiguration;

/**
 * Anonymous self-signup form.
 */
public class SignupForm extends SeleniumForm
{
    public SignupForm(Selenium selenium)
    {
        super(selenium, false);
    }

    public String getFormName()
    {
        return SignupUserConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{"login", "name", "password", "confirmPassword"};
    }
}
