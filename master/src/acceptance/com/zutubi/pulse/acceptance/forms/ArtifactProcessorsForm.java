/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class ArtifactProcessorsForm extends BaseForm
{
    String formName;

    public ArtifactProcessorsForm(WebTester tester, boolean output)
    {
        super(tester);
        if(output)
        {
            formName = "edit.output";
        }
        else
        {
            formName = "artifact.processors";
        }
    }

    public String getFormName()
    {
        return formName;
    }

    public String[] getFieldNames()
    {
        return new String[]{"processors"};
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
    }

    public int[] getFieldTypes()
    {
        return new int[]{MULTI_CHECKBOX};
    }
}
