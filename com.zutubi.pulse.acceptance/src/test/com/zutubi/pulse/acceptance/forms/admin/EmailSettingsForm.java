package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;

/**
 */
public class EmailSettingsForm extends ConfigurationForm
{
    public EmailSettingsForm(SeleniumBrowser browser)
    {
        super(browser, EmailConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[]{ TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD };
    }
}
