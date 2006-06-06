package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class RequiredResourceForm extends BaseForm
{
    private boolean create;

    public RequiredResourceForm(WebTester tester, boolean create)
    {
        super(tester);
        this.create = create;
    }

    public String getFormName()
    {
        if(create)
        {
            return "addBuildStageResource";
        }
        else
        {
            return "editBuildStageResource";
        }
    }

    public String[] getFieldNames()
    {
        return new String[] { "resourceRequirement.resource", "resourceRequirement.version" };
    }
}
