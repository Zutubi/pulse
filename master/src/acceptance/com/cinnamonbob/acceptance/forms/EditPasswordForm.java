package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * The users edit password form.
 */
public class EditPasswordForm extends BaseForm
{
    public EditPasswordForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "password.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"current", "password", "confirm"};
    }
}
