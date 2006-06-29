package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CommitMessageLinkForm extends BaseForm
{
    private boolean create;

    public CommitMessageLinkForm(WebTester tester, boolean create)
    {
        super(tester);
        this.create = create;
    }

    public String getFormName()
    {
        if(create)
        {
            return "add.commit.message.link";
        }
        else
        {
            return "edit.commit.message.link";
        }
    }

    public String[] getFieldNames()
    {
        return new String[]{"transformer.name", "transformer.expression", "transformer.replacement", "selectedProjects"};
    }

    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, TEXTFIELD, SELECT};
    }
}
