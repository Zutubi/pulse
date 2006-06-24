package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class AddExeActionForm extends BaseForm
{
    public AddExeActionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "post.build.action.exe";
    }

    public String[] getFieldNames()
    {
        return new String[] { "command", "arguments" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD };
    }
}
