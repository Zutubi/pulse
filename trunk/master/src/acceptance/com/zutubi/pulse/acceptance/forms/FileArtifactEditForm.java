package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class FileArtifactEditForm extends BaseForm
{
    public FileArtifactEditForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "edit.file";
    }

    public String[] getFieldNames()
    {
        return new String[]{"name", "capture.file", "capture.mimeType", "processors"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, MULTI_CHECKBOX};
    }
}
