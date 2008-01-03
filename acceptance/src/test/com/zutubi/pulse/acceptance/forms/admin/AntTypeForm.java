package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.prototype.config.project.types.AntTypeConfiguration;

/**
 */
public class AntTypeForm extends ConfigurationForm
{
    public AntTypeForm(Selenium selenium)
    {
        super(selenium, AntTypeConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, MULTI_SELECT };
    }
}
