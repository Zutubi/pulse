package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class CreateUserForm extends BaseForm
{
    public CreateUserForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "newUser.create";
    }

    public String[] getFieldNames()
    {
        return new String[]{"newUser.login", "newUser.name", "ldapAuthentication", "newUser.password", "confirm", "admin"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD, CHECKBOX};
    }
}
