package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ConfigureCustomChangeViewerForm extends BaseForm
{
    public ConfigureCustomChangeViewerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "CustomChangeViewerForm";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "changesetURL", "fileViewURL", "fileDownloadURL", "fileDiffURL" };
    }
}
