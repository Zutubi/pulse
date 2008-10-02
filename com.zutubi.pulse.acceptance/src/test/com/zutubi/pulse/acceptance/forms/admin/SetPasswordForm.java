package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration;

/**
 * User set password action form.
 */
public class SetPasswordForm extends ConfigurationForm
{
    public SetPasswordForm(Selenium selenium)
    {
        super(selenium, SetPasswordConfiguration.class);
    }
}
