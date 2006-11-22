package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditTagActionForm extends BaseForm
{
    private boolean stage = false;

    public EditTagActionForm(WebTester tester)
    {
        super(tester);
    }

    public EditTagActionForm(WebTester tester, boolean stage)
    {
        super(tester);
        this.stage = stage;
    }

    public String getFormName()
    {
        return "edit.tag.post.build.action";
    }

    public String[] getFieldNames()
    {
        if(stage)
        {
            return new String[]{ "newName", "stateNames", "failOnError", "postBuildAction.tag", "moveExisting" };
        }
        else
        {
            return new String[]{ "newName", "specIds", "stateNames", "failOnError", "postBuildAction.tag", "moveExisting" };
        }
    }

    public int[] getFieldTypes()
    {
        if(stage)
        {
            return new int[] { TEXTFIELD, SELECT, CHECKBOX, TEXTFIELD, CHECKBOX };
        }
        else
        {
            return new int[] { TEXTFIELD, SELECT, SELECT, CHECKBOX, TEXTFIELD, CHECKBOX };
        }
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
        tester.assertOptionValuesEqual("stateNames", new String[] { "SUCCESS", "FAILURE", "ERROR" });
    }
}
