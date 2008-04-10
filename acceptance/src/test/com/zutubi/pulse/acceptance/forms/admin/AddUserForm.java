package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.prototype.config.user.UserConfigurationCreator;

/**
 * Form for adding a user details.
 */
public class AddUserForm extends ConfigurationForm
{
    public AddUserForm(Selenium selenium)
    {
        super(selenium, UserConfigurationCreator.class);
    }
}
