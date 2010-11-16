package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;

/**
 * The Perforce SCM form.
 */
public class PerforceForm extends ConfigurationForm
{
    public PerforceForm(SeleniumBrowser browser)
    {
        super(browser, PerforceConfiguration.class);
    }
}