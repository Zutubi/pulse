package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class SlaveForm extends BaseForm
{
    private boolean create;

    public SlaveForm(WebTester tester, boolean create)
    {
        super(tester);
        this.create = create;
    }

    public String getFormName()
    {
        if(create)
        {
            return "addAgent";
        }
        else
        {
            return "editAgent";
        }
    }

    public String[] getFieldNames()
    {
        return new String[] { "slave.name", "slave.host", "slave.port" };
    }
}
