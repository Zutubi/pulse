package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CreatePersonalSubscriptionForm extends BaseForm
{
    public CreatePersonalSubscriptionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "subscription.create.personal";
    }

    public String[] getFieldNames()
    {
        return new String[] { "template" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { SELECT };
    }
}
