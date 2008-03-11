package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

/**
 * Form for editing a user's details.
 */
public class UserForm extends ConfigurationForm
{
    private String user;

    public UserForm(Selenium selenium, String user)
    {
        super(selenium, UserConfiguration.class);
        this.user = user;
    }

    public String getUser()
    {
        return user;
    }
}
