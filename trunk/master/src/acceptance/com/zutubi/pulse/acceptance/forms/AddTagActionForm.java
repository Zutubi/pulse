package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class AddTagActionForm extends BaseForm
{
    public AddTagActionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "post.build.action.tag";
    }

    public String[] getFieldNames()
    {
        return new String[] { "tagName", "moveExisting" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, CHECKBOX };
    }
}

