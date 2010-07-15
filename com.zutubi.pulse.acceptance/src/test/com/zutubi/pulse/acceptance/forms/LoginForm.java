package com.zutubi.pulse.acceptance.forms;

import com.zutubi.pulse.master.tove.config.misc.LoginConfiguration;
import com.zutubi.pulse.acceptance.SeleniumBrowser;

public class LoginForm extends SeleniumForm
{
    public LoginForm(SeleniumBrowser browser)
    {
        super(browser);
    }

    public String getFormName()
    {
        return LoginConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{"j_username", "j_password"};
    }

}
