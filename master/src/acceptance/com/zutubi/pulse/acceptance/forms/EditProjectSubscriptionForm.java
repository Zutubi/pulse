package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditProjectSubscriptionForm extends BaseForm
{
    public EditProjectSubscriptionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "subscription.edit.project";
    }

    public String[] getFieldNames()
    {
        return new String[] { "contactPointId", "projects", "conditionType", "selectedConditions", "repeatedX", "repeatedUnits", "expression", "template" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { SELECT, MULTI_SELECT, RADIOBOX, MULTI_CHECKBOX, TEXTFIELD, SELECT, TEXTFIELD, SELECT };
    }
}
