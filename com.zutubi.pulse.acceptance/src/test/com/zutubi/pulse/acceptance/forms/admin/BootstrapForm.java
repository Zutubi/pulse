package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.BootstrapConfiguration;

/**
 * Form for {@link BootstrapConfiguration}.
 */
public class BootstrapForm extends ConfigurationForm
{
    public BootstrapForm(SeleniumBrowser browser)
    {
        super(browser, BootstrapConfiguration.class);
    }
}
