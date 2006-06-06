package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class BuildStageForm extends BaseForm
{
    private boolean create;

    public BuildStageForm(WebTester tester, boolean create)
    {
        super(tester);
        this.create = create;
    }

    public String getFormName()
    {
        if(create)
        {
            return "createBuildStage";
        }
        else
        {
            return "editBuildStage";
        }
    }

    public String[] getFieldNames()
    {
        return new String[]{ "name", "stage.recipe", "buildHost" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, SELECT };
    }
}
