package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.SeleniumForm;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.tove.config.setup.AdminUserConfiguration;

/**
 */
public class CreateAdminForm extends SeleniumForm
{
    public CreateAdminForm(SeleniumBrowser browser)
    {
        super(browser, false);
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
