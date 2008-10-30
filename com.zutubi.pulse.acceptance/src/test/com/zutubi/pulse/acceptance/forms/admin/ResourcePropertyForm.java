package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.ResourcePropertyConfiguration;

/**
 * Resource property form (suits wizard too).
 */
public class ResourcePropertyForm extends ConfigurationForm
{
    public ResourcePropertyForm(Selenium selenium, boolean inherited)
    {
        super(selenium, ResourcePropertyConfiguration.class, true, inherited);
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, CHECKBOX, CHECKBOX, CHECKBOX};
    }
}
