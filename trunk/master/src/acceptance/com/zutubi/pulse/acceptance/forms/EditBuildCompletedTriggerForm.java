package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditBuildCompletedTriggerForm extends BaseForm
{
    public EditBuildCompletedTriggerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "editBuildCompletedTrigger";
    }

    public String[] getFieldNames()
    {
        return new String[]{"trigger.name", "specification", "filterProject", "filterSpecification", "filterStateNames"};
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, SELECT, SELECT, SELECT, SELECT};
    }
}
