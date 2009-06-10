package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.setup.SetupDatabaseTypeConfiguration;

/**
 */
public class SetupDatabaseTypeForm extends ConfigurationForm
{
    public SetupDatabaseTypeForm(SeleniumBrowser browser)
    {
        super(browser, SetupDatabaseTypeConfiguration.class, false);
    }
}