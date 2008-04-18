package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.prototype.config.setup.SetupDataConfiguration;
import com.zutubi.pulse.prototype.config.setup.SetupDatabaseTypeConfiguration;
import com.thoughtworks.selenium.Selenium;

/**
 */
public class SetupDatabaseTypeForm extends ConfigurationForm
{
    public SetupDatabaseTypeForm(Selenium selenium)
    {
        super(selenium, SetupDatabaseTypeConfiguration.class, false);
    }
}