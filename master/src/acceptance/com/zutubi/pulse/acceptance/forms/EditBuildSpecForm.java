package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditBuildSpecForm extends BaseForm
{
    public EditBuildSpecForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
            return "spec.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"spec.name", "isolateChangelists", "retainWorkingCopy", "checkoutSchemeName", "timeoutEnabled", "timeout", "prompt"};
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, CHECKBOX, CHECKBOX, SELECT, CHECKBOX, TEXTFIELD, CHECKBOX };
    }
}
