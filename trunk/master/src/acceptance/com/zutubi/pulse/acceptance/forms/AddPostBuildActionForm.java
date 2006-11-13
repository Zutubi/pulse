package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class AddPostBuildActionForm extends BaseForm
{
    public AddPostBuildActionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "post.build.action.type";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "name", "type", "specIds", "stateNames", "failOnError" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, SELECT, SELECT, SELECT, CHECKBOX };
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
        tester.assertOptionValuesEqual("type", new String[]{ "tag", "exe" });
        tester.assertOptionValuesEqual("stateNames", new String[] { "SUCCESS", "FAILURE", "ERROR" });
    }
}
