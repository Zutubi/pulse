package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.group.BuiltinGroupConfiguration;

/**
 * Builtin group edit form.
 */
public class BuiltinGroupForm extends ConfigurationForm
{
    public BuiltinGroupForm(Selenium selenium)
    {
        super(selenium, BuiltinGroupConfiguration.class);
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, MULTI_SELECT };
    }
}
