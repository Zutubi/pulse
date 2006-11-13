package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * The user edit form.
 * 
 */
public class EditUserForm extends BaseForm
{
    public EditUserForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "user.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"user.name"};
    }
}
