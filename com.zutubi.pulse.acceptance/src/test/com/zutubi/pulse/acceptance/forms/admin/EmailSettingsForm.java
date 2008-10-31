package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;

/**
 */
public class EmailSettingsForm extends ConfigurationForm
{
    public EmailSettingsForm(Selenium selenium)
    {
        super(selenium, EmailConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[]{ TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD };
    }
}
