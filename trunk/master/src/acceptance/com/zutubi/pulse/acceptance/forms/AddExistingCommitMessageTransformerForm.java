package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class comment/>
 */
public class AddExistingCommitMessageTransformerForm extends BaseForm
{
    public AddExistingCommitMessageTransformerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "transformer.existing.add";
    }

    public String[] getFieldNames()
    {
        return new String[]{"existing"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{SELECT};
    }
}
