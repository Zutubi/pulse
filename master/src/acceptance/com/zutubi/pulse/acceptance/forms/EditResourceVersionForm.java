package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditResourceVersionForm extends BaseForm
{
    public EditResourceVersionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "editResourceVersion";
    }

    public String[] getFieldNames()
    {
        return new String[] { "newValue" };
    }
}
