/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class FileArtifactForm extends BaseForm
{
    public FileArtifactForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "artifact.file";
    }

    public String[] getFieldNames()
    {
        return new String[]{"capture.file", "capture.mimeType"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD};
    }
}
