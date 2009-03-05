package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;

/**
 * Resource property form (suits wizard too).
 */
public class ResourcePropertyForm extends ConfigurationForm
{
    public ResourcePropertyForm(Selenium selenium)
    {
        super(selenium, ResourcePropertyConfiguration.class, true, false);
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX, CHECKBOX, CHECKBOX};
    }
}
