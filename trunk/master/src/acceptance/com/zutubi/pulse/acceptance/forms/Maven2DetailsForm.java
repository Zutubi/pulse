package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class Maven2DetailsForm extends BaseForm
{
    private boolean setup;

    public Maven2DetailsForm(WebTester tester, boolean setup)
    {
        super(tester);
        this.setup = setup;
    }

    public String getFormName()
    {
        if(setup)
        {
            return "maven2.setup";
        }
        else
        {
            return "maven2.edit";
        }
    }

    public String[] getFieldNames()
    {
        return new String[]{"details.workingDir", "details.goals", "details.arguments"};
    }
}
