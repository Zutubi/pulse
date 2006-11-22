package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditExeActionForm extends BaseForm
{
    private boolean stage = false;

    public EditExeActionForm(WebTester tester)
    {
        super(tester);
    }

    public EditExeActionForm(WebTester tester, boolean stage)
    {
        super(tester);
        this.stage = stage;
    }

    public String getFormName()
    {
        return "edit.exe.post.build.action";
    }

    public String[] getFieldNames()
    {
        if(stage)
        {
            return new String[]{ "newName", "stateNames", "failOnError", "postBuildAction.command", "postBuildAction.arguments" };
        }
        else
        {
            return new String[]{ "newName", "specIds", "stateNames", "failOnError", "postBuildAction.command", "postBuildAction.arguments" };
        }
    }

    public int[] getFieldTypes()
    {
        if(stage)
        {
            return new int[] { TEXTFIELD, SELECT, CHECKBOX, TEXTFIELD, TEXTFIELD };
        }
        else
        {
            return new int[] { TEXTFIELD, SELECT, SELECT, CHECKBOX, TEXTFIELD, TEXTFIELD };
        }
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
        tester.assertOptionValuesEqual("stateNames", new String[] { "SUCCESS", "FAILURE", "ERROR" });
    }
}
