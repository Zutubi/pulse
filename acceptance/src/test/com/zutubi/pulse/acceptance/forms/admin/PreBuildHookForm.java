package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.tove.config.project.hooks.PreBuildHookConfiguration;

/**
 */
public class PreBuildHookForm extends ConfigurationForm
{
    public PreBuildHookForm(Selenium selenium)
    {
        super(selenium, PreBuildHookConfiguration.class);
    }
}
