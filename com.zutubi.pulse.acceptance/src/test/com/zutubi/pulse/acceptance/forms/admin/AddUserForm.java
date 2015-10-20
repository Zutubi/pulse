package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.rest.wizards.UserConfigurationCreator;

/**
 * Form for adding a user details.
 */
public class AddUserForm extends ConfigurationForm
{
    public AddUserForm(SeleniumBrowser browser)
    {
        super(browser, UserConfigurationCreator.class);
    }
}
