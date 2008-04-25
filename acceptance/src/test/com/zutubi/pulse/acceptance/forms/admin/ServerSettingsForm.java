package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;

/**
 * The generic server settings at path settings/.
 */
public class ServerSettingsForm extends ConfigurationForm
{
    public ServerSettingsForm(Selenium selenium)
    {
        super(selenium, GlobalConfiguration.class);
    }
}
