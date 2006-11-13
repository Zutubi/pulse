package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class MasterAgentForm extends BaseForm
{
    public MasterAgentForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "editMasterAgent";
    }

    public String[] getFieldNames()
    {
        return new String[] { "agentHost" };
    }
}
