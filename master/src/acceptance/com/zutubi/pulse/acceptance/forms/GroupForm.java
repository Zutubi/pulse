package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class GroupForm extends BaseForm
{
    private boolean create;

    public GroupForm(WebTester tester, boolean add)
    {
        super(tester);
        this.create = add;
    }

    public String getFormName()
    {
        if(create)
        {
            return "group.create";
        }
        else
        {
            return "group.edit";
        }
    }

    public String[] getFieldNames()
    {
        if(create)
        {
            return new String[] { "name", "admin", "personal", "adminAllProjects", "projects" };
        }
        else
        {
            return new String[] { "newName", "admin", "personal", "adminAllProjects", "projects" };
        }
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, CHECKBOX, CHECKBOX, CHECKBOX, MULTI_SELECT };
    }
}
