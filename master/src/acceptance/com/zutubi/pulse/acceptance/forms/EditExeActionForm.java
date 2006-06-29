package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditExeActionForm extends BaseForm
{
    public EditExeActionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "edit.exe.post.build.action";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "newName", "specIds", "stateNames", "failOnError", "postBuildAction.command", "postBuildAction.arguments" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, SELECT, SELECT, CHECKBOX, TEXTFIELD, TEXTFIELD };
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
        tester.assertOptionValuesEqual("stateNames", new String[] { "SUCCESS", "FAILURE", "ERROR" });
    }
}
