package com.zutubi.pulse.acceptance.forms.setup;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 */
public class CreateAdminForm extends SeleniumForm
{
    public CreateAdminForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return "com.zutubi.pulse.prototype.config.setup.AdminUserConfiguration";
    }

    public String[] getFieldNames()
    {
        return new String[]{"login", "name", "password", "confirm"};
    }
}
