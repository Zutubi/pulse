package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CreateSubscriptionForm extends BaseForm
{
    public CreateSubscriptionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "subscription.create";
    }

    public String[] getFieldNames()
    {
        return new String[] { "contactId", "type" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { SELECT, SELECT };
    }
}
