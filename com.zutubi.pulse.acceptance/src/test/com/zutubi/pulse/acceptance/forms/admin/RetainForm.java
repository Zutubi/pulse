package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.cleanup.config.RetainConfiguration;

/**
 * Form for configuring retain rules.
 */
public class RetainForm extends ConfigurationForm
{
    public RetainForm(SeleniumBrowser browser)
    {
        super(browser, RetainConfiguration.class);
    }
}
