package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.tove.config.setup.SetupDatabaseTypeConfiguration;

/**
 */
public class SetupDatabaseTypeForm extends ConfigurationForm
{
    public SetupDatabaseTypeForm(Selenium selenium)
    {
        super(selenium, SetupDatabaseTypeConfiguration.class, false);
    }
}