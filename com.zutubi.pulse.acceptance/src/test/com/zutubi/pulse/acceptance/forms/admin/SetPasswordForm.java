package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration;

/**
 * User set password action form.
 */
public class SetPasswordForm extends ConfigurationForm
{
    public SetPasswordForm(SeleniumBrowser browser)
    {
        super(browser, SetPasswordConfiguration.class);
    }
}
