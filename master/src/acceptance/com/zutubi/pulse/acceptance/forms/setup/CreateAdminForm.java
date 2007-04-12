package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.BaseForm;
import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class CreateAdminForm extends BaseForm
{
    public CreateAdminForm(WebTester tester)
    {
        super(tester);
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
