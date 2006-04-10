package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CreateBuildSpecForm extends BaseForm
{
    public CreateBuildSpecForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "createBuildSpecification";
    }

    public String[] getFieldNames()
    {
        return new String[]{"spec.name", "recipe", "timeoutEnabled", "timeout"};
    }
}
