package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class AddPostBuildActionForm extends BaseForm
{
    private boolean stage = false;

    public AddPostBuildActionForm(WebTester tester)
    {
        super(tester);
    }

    public AddPostBuildActionForm(WebTester tester, boolean stage)
    {
        super(tester);
        this.stage = stage;
    }

    public String getFormName()
    {
        return "post.build.action.type";
    }

    public String[] getFieldNames()
    {
        if(stage)
        {
            return new String[]{ "name", "type", "stateNames", "failOnError" };
        }
        else
        {
            return new String[]{ "name", "type", "specIds", "stateNames", "failOnError" };
        }
    }

    public int[] getFieldTypes()
    {
        if(stage)
        {
            return new int[] { TEXTFIELD, SELECT, SELECT, CHECKBOX };
        }
        else
        {
            return new int[] { TEXTFIELD, SELECT, SELECT, SELECT, CHECKBOX };
        }
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
        tester.assertOptionValuesEqual("type", new String[]{ "tag", "email", "exe" });
        tester.assertOptionValuesEqual("stateNames", new String[] { "SUCCESS", "FAILURE", "ERROR" });
    }
}
