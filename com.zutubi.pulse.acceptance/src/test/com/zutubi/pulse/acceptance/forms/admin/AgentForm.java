package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

/**
 * Used for both the wizard and editing an agent.
 */
public class AgentForm extends ConfigurationForm
{
    public AgentForm(SeleniumBrowser browser)
    {
        super(browser, AgentConfiguration.class);
    }
}
