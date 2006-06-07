package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CreateResourcePropertyForm extends BaseForm
{
    public CreateResourcePropertyForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "createProperty";
    }

    public String[] getFieldNames()
    {
        return new String[] { "property.name", "value" };
    }
}
