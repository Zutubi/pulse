package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.project.hooks.PreBuildHookConfiguration;

/**
 */
public class PreBuildHookForm extends ConfigurationForm
{
    public PreBuildHookForm(SeleniumBrowser browser)
    {
        super(browser, PreBuildHookConfiguration.class);
    }
}
