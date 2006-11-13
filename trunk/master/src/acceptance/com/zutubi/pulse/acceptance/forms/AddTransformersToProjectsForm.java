package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class comment/>
 */
public class AddTransformersToProjectsForm extends BaseForm
{
    public AddTransformersToProjectsForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "edit.commit.message.projects";
    }

    public String[] getFieldNames()
    {
        return new String[]{"selectedProjects"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{SELECT};
    }
}
