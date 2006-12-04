package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ExecutableDetailsForm extends BaseForm
{
    private boolean setup;

    public ExecutableDetailsForm(WebTester tester, boolean setup)
    {
        super(tester);
        this.setup = setup;
    }

    public String getFormName()
    {
        if(setup)
        {
            return "executable.setup";
        }
        else
        {
            return "editExecutableDetails";
        }
    }

    public String[] getFieldNames()
    {
        return new String[]{"details.executable", "details.arguments", "details.workingDir"};
    }
}
