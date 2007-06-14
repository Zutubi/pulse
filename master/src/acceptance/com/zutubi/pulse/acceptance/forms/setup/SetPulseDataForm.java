package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 */
public class SetPulseDataForm extends SeleniumForm
{
    public SetPulseDataForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return "com.zutubi.pulse.prototype.config.setup.SetupDataConfiguration";
    }

    public String[] getFieldNames()
    {
        return new String[]{"data"};
    }
}
