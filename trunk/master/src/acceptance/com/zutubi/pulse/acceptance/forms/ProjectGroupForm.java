package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ProjectGroupForm extends BaseForm
{
    private boolean create;

    public ProjectGroupForm(WebTester tester, boolean add)
    {
        super(tester);
        this.create = add;
    }

    public String getFormName()
    {
        if(create)
        {
            return "add.project.group";
        }
        else
        {
            return "edit.project.group";
        }
    }

    public String[] getFieldNames()
    {
        return new String[] { "name", "projects" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, MULTI_SELECT };
    }
}
