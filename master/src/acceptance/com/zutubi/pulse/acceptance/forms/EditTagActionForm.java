package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EditTagActionForm extends BaseForm
{
    public EditTagActionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "edit.tag.post.build.action";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "newName", "specIds", "stateNames", "failOnError", "postBuildAction.tag", "moveExisting" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, SELECT, SELECT, CHECKBOX, TEXTFIELD, CHECKBOX };
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
        tester.assertOptionValuesEqual("stateNames", new String[] { "ERROR", "FAILURE", "SUCCESS" });
    }
}
