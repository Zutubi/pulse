package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CreateBuildCompletedTriggerForm extends BaseForm
{
    public CreateBuildCompletedTriggerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "trigger.build.completed";
    }

    public String[] getFieldNames()
    {
        return new String[]{"filterProject", "filterSpecification", "filterStateNames"};
    }

    public int[] getFieldTypes()
    {
        return new int[] { SELECT, SELECT, SELECT};
    }

}
