package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditResourceForm extends BaseForm
{
    public EditResourceForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "editResource";
    }

    public String[] getFieldNames()
    {
        return new String[] { "newName", "defaultVersion" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, SELECT };
    }
}
