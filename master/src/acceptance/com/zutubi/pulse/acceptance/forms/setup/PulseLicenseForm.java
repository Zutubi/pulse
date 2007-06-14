package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 */
public class PulseLicenseForm extends SeleniumForm
{
    public PulseLicenseForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return "com.zutubi.pulse.prototype.config.setup.SetupLicenseConfiguration";
    }

    public String[] getFieldNames()
    {
        return new String[]{"license"};
    }

}
