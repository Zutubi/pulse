package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.tove.config.setup.ServerSettingsConfiguration;

/**
 */
public class ServerSettingsForm extends SeleniumForm
{
    public ServerSettingsForm(Selenium selenium)
    {
        super(selenium, false);
    }

    public String getFormName()
    {
        return ServerSettingsConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{"baseUrl", "host", "ssl", "from", "username", "password", "subjectPrefix", "customPort", "port"};
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX, TEXTFIELD };
    }
}
