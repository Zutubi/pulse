package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.setup.SetupLicenseConfiguration;

/**
 */
public class PulseLicenseForm extends SeleniumForm
{
    public PulseLicenseForm(SeleniumBrowser browser)
    {
        super(browser, false);
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
