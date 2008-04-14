package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.prototype.config.project.types.CustomTypeConfiguration;

/**
 * A text area to edit the pulse file.
 */
public class CustomTypeForm extends ConfigurationForm
{
    public CustomTypeForm(Selenium selenium)
    {
        super(selenium, CustomTypeConfiguration.class);
    }

    public String[] getFieldNames()
    {
        return new String[]{"pulseFileString"};
    }
}
