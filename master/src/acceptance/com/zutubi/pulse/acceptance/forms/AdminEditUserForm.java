package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * The user edit form.
 *
 */
public class AdminEditUserForm extends BaseForm
{
    public AdminEditUserForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "user.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "newLogin", "newName", "ldapAuthentication" };
    }


    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX };
    }
}
