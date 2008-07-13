package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.tove.config.setup.AdminUserConfiguration;

/**
 */
public class CreateAdminForm extends SeleniumForm
{
    public CreateAdminForm(Selenium selenium)
    {
        super(selenium, false);
    }

    public String getFormName()
    {
        return AdminUserConfiguration.class.getName();
    }

    public String[] getFieldNames()
    {
        return new String[]{"login", "name", "password", "confirm"};
    }
}
