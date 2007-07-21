package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.prototype.config.setup.SetupDataConfiguration;

/**
 */
public class SetPulseDataForm extends SeleniumForm
{
    public SetPulseDataForm(Selenium selenium)
    {
        super(selenium, false);
    }

    public String getFormName()
    {
        return SetupDataConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{"data"};
    }
}
