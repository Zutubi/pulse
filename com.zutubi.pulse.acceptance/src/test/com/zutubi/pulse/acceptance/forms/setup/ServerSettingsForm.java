package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.setup.ServerSettingsConfiguration;

/**
 */
public class ServerSettingsForm extends SeleniumForm
{
    public ServerSettingsForm(SeleniumBrowser browser)
    {
        super(browser, false);
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
