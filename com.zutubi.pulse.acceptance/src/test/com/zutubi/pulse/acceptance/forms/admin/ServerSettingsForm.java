package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;

/**
 * The generic server settings at path settings/.
 */
public class ServerSettingsForm extends ConfigurationForm
{
    public ServerSettingsForm(SeleniumBrowser browser)
    {
        super(browser, GlobalConfiguration.class);
    }
}
