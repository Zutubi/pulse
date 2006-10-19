package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CreateProjectSubscriptionForm extends BaseForm
{
    public CreateProjectSubscriptionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "subscription.create.project";
    }

    public String[] getFieldNames()
    {
        return new String[] { "projects", "condition", "template" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { MULTI_SELECT, TEXTFIELD, SELECT };
    }
}
