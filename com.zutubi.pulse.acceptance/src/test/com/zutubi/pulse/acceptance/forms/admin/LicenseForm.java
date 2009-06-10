package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.license.config.LicenseConfiguration;

/**
 * Project label form.
 */
public class LicenseForm extends ConfigurationForm
{
    public LicenseForm(SeleniumBrowser browser)
    {
        super(browser, LicenseConfiguration.class);
    }

    public String[] getFieldNames()
    {
        return new String[]{"key"};
    }
}
