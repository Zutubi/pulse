package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CreatePropertyForm extends BaseForm
{
    private boolean spec;

    public CreatePropertyForm(WebTester tester, boolean spec)
    {
        super(tester);
        this.spec = spec;
    }

    public String getFormName()
    {
        if(spec)
        {
            return "createBuildSpecificationProperty";
        }
        else
        {
            return "createProperty";
        }
    }

    public String[] getFieldNames()
    {
        return new String[] { "property.name", "value", "property.addToEnvironment", "property.addToPath", "property.resolveVariables" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX, CHECKBOX, CHECKBOX };
    }
}
