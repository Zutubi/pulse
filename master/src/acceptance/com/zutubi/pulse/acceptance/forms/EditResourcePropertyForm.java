package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditResourcePropertyForm extends BaseForm
{
    public EditResourcePropertyForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "editProperty";
    }

    public String[] getFieldNames()
    {
        return new String[] { "newName", "newValue", "newAddToEnvironment", "newAddToPath" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX, CHECKBOX };
    }
}
