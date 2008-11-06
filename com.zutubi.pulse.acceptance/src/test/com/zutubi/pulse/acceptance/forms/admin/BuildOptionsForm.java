package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.BuildOptionsConfiguration;

/**
 * The build options form, mapping to the BuildOptionsConfiguration class.
 */
public class BuildOptionsForm extends ConfigurationForm
{
    public BuildOptionsForm(Selenium selenium)
    {
        super(selenium, BuildOptionsConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { CHECKBOX, CHECKBOX, CHECKBOX, TEXTFIELD };
    }
}
