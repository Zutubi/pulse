package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class DirectoryArtifactForm extends BaseForm
{
    public DirectoryArtifactForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "artifact.directory";
    }

    public String[] getFieldNames()
    {
        return new String[]{"capture.base", "capture.includes", "capture.excludes", "capture.mimeType"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD};
    }
}
