package com.cinnamonbob.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class CustomProjectEditForm extends BaseForm
{
    public static String FORM_NAME = "custom.edit";
    public static String FIELD_FILE = "details.bobFileName";

    public CustomProjectEditForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return FORM_NAME;
    }

    public String[] getFieldNames()
    {
        return new String[]{FIELD_FILE};
    }
}
