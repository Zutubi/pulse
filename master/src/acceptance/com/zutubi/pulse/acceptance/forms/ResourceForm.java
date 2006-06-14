package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ResourceForm extends BaseForm
{
    public ResourceForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "createResource";
    }

    public String[] getFieldNames()
    {
        return new String[] { "resource.name" };
    }
}
