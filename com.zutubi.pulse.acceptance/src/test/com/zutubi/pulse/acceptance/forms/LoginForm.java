package com.zutubi.pulse.acceptance.forms;

import com.zutubi.pulse.master.tove.config.misc.LoginConfiguration;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.LoginPage;

public class LoginForm extends SeleniumForm
{
    public LoginForm(SeleniumBrowser browser)
    {
        super(browser, false);
    }

    public String getFormName()
    {
        return LoginConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{LoginPage.FIELD_USERNAME, LoginPage.FIELD_PASSWORD, LoginPage.FIELD_REMEMBERME};
    }

    @Override
    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX};
    }
}
