package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * Form for editing a user's details.
 */
public class UserForm extends ConfigurationForm
{
    private String user;

    public UserForm(SeleniumBrowser browser, String user)
    {
        super(browser, UserConfiguration.class);
        this.user = user;
    }

    public String getUser()
    {
        return user;
    }
}
