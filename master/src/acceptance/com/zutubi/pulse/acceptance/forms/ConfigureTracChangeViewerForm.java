package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ConfigureTracChangeViewerForm extends BaseForm
{
    public ConfigureTracChangeViewerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "TracChangeViewerForm";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "baseURL" };
    }
}
