package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditPropertyForm extends BaseForm
{
    private boolean spec;

    public EditPropertyForm(WebTester tester, boolean spec)
    {
        super(tester);
        this.spec = spec;
    }

    public String getFormName()
    {
        if(spec)
        {
            return "editBuildSpecificationProperty";
        }
        else
        {
            return "editProperty";
        }
    }

    public String[] getFieldNames()
    {
        return new String[] { "newName", "newValue", "newAddToEnvironment", "newAddToPath", "newResolveVariables" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX, CHECKBOX, CHECKBOX };
    }
}
