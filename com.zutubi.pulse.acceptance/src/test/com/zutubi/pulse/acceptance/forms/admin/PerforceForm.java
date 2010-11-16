package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;

/**
 * The Perforce SCM form.
 */
public class PerforceForm extends ConfigurationForm
{
    public PerforceForm(Selenium selenium)
    {
        super(selenium, PerforceConfiguration.class);
    }
}