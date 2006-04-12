package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class BuildSpecForm extends BaseForm
{
    private boolean create;

    public BuildSpecForm(WebTester tester, boolean create)
    {
        super(tester);
        this.create = create;
    }

    public String getFormName()
    {
        if(create)
        {
            return "createBuildSpecification";
        }
        else
        {
            return "spec.edit"; 
        }
    }

    public String[] getFieldNames()
    {
        return new String[]{"spec.name", "recipe", "timeoutEnabled", "timeout"};
    }
}
