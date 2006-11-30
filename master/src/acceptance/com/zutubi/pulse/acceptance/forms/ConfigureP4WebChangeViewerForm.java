package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ConfigureP4WebChangeViewerForm extends BaseForm
{
    public ConfigureP4WebChangeViewerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "P4WebChangeViewerForm";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "baseURL" };
    }
}
