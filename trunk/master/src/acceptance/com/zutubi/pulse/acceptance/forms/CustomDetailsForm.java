package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CustomDetailsForm extends BaseForm
{
    public CustomDetailsForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "custom.setup";
    }

    public String[] getFieldNames()
    {
        return new String[]{"details.pulseFile"};
    }
}
