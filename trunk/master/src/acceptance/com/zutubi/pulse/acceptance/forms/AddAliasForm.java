package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * The add alias form.
 */
public class AddAliasForm extends BaseForm
{
    public AddAliasForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "alias.create";
    }

    public String[] getFieldNames()
    {
        return new String[]{"alias"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD};
    }
}
