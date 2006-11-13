package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class LicenseEditForm extends BaseForm
{
    public LicenseEditForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "license.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"license"};
    }
}
