package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ResourceVersionForm extends BaseForm
{
    public ResourceVersionForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "createResourceVersion";
    }

    public String[] getFieldNames()
    {
        return new String[] { "version.value" };
    }
}
