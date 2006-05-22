package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class SubscriptionForm extends BaseForm
{
    private String name;

    public SubscriptionForm(WebTester tester, boolean create)
    {
        super(tester);
        if(create)
        {
            name = "subscription.create";
        }
        else
        {
            name = "subscription.edit";
        }
    }

    public String getFormName()
    {
        return name;
    }

    public String[] getFieldNames()
    {
        return new String[] { "projectId", "contactPointId", "condition" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { SELECT, SELECT, SELECT };
    }
}
