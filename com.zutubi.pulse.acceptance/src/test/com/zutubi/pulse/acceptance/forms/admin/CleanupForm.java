package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;

/**
 * The form object representing the Cleanup Configuration form.
 */
public class CleanupForm extends ConfigurationForm
{
    public CleanupForm(SeleniumBrowser browser)
    {
        super(browser, CleanupConfiguration.class);
    }
}
