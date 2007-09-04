package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.prototype.config.admin.EmailConfiguration;

/**
 */
public class EmailSettingsForm extends SeleniumForm
{
    public EmailSettingsForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return EmailConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{ "host", "ssl", "from", "username", "password", "subjectPrefix", "customPort", "port", "localhost" };
    }

    public int[] getFieldTypes()
    {
        return new int[]{ TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD };
    }
}
