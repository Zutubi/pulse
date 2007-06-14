package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 */
public class ServerSettingsForm extends SeleniumForm
{
    public ServerSettingsForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return "com.zutubi.pulse.prototype.config.setup.ServerSettingsConfiguration";
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
