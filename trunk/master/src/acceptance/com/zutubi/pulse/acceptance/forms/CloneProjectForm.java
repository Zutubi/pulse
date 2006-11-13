package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CloneProjectForm extends BaseForm
{
    public CloneProjectForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "project.clone";
    }

    public String[] getFieldNames()
    {
        return new String[] { "name", "description" };
    }
}
