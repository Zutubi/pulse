package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class AddGroupMembersForm extends BaseForm
{
    public AddGroupMembersForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "addGroupMembers";
    }

    public String[] getFieldNames()
    {
        return new String[] { "members" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { MULTI_SELECT };
    }

}
