package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditPersonalSubscriptionForm extends BaseForm
{
    public EditPersonalSubscriptionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "subscription.edit.personal";
    }

    public String[] getFieldNames()
    {
        return new String[] { "contactPointId", "template" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { SELECT, SELECT };
    }
}
