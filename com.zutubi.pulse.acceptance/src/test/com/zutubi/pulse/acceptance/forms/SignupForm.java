package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.tove.config.user.SignupUserConfiguration;
import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Anonymous self-signup form.
 */
public class SignupForm extends SeleniumForm
{
    public SignupForm(SeleniumBrowser browser)
    {
        super(browser, false);
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
