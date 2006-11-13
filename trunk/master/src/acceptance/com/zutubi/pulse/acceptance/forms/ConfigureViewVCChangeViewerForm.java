package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ConfigureViewVCChangeViewerForm extends BaseForm
{
    public ConfigureViewVCChangeViewerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "ViewVCChangeViewerForm";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "baseURL", "projectPath" };
    }
}
