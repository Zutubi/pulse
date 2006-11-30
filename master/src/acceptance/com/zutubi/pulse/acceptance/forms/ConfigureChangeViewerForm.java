package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class ConfigureChangeViewerForm extends BaseForm
{
    public ConfigureChangeViewerForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "SelectType";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "type" };
    }

    public int[] getFieldTypes()
    {
        return new int[] { SELECT };
    }

    public void assertFormPresent()
    {
        super.assertFormPresent();
        tester.assertOptionValuesEqual("type", new String[]{ "custom", "Fisheye", "P4Web", "Trac", "ViewVC" });
    }
}
