package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.license.config.LicenseConfiguration;

/**
 * Project label form.
 */
public class LicenseForm extends ConfigurationForm
{
    public LicenseForm(Selenium selenium)
    {
        super(selenium, LicenseConfiguration.class);
    }

    public String[] getFieldNames()
    {
        return new String[]{"key"};
    }
}
