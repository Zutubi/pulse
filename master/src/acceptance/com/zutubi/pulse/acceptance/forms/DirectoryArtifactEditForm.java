/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class DirectoryArtifactEditForm extends BaseForm
{
    public DirectoryArtifactEditForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "edit.directory";
    }

    public String[] getFieldNames()
    {
        return new String[]{"name", "capture.base", "capture.includes", "capture.excludes", "capture.mimeType", "processors"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, MULTI_CHECKBOX};
    }
}
