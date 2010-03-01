package com.zutubi.pulse.acceptance.forms.dashboard;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.user.ChangePasswordConfiguration;

/**
 * User change password action form.
 */
public class ChangePasswordForm extends ConfigurationForm
{
    public ChangePasswordForm(SeleniumBrowser browser)
    {
        super(browser, ChangePasswordConfiguration.class);
    }
}