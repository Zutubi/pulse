package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ConfigureFisheyeChangeViewerForm extends BaseForm
{
    public ConfigureFisheyeChangeViewerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "FisheyeChangeViewerForm";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "baseURL", "projectPath" };
    }
}
