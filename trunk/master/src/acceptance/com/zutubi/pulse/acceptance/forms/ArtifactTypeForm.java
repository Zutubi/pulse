package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ArtifactTypeForm extends BaseForm
{
    public ArtifactTypeForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "artifact.type";
    }

    public String[] getFieldNames()
    {
        return new String[]{"name", "type"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, SELECT};
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
        tester.assertOptionsEqual("type", new String[]{"directory artifact", "file artifact"});
    }
}
