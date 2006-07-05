package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class AddBuildSpecForm extends BaseForm
{
    public AddBuildSpecForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
            return "spec.add";
    }

    public String[] getFieldNames()
    {
        return new String[]{"spec.name", "spec.isolateChangelists", "timeoutEnabled", "timeout", "name", "stage.recipe", "buildHost"};
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, CHECKBOX, CHECKBOX, TEXTFIELD, TEXTFIELD, TEXTFIELD, SELECT };
    }
}
