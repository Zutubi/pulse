package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.tove.config.setup.SetupLicenseConfiguration;

/**
 */
public class PulseLicenseForm extends SeleniumForm
{
    public PulseLicenseForm(Selenium selenium)
    {
        super(selenium, false);
    }

    public String getFormName()
    {
        return SetupLicenseConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{"license"};
    }

}
