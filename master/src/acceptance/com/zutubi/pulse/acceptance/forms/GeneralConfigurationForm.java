/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class GeneralConfigurationForm extends BaseForm
{
    public GeneralConfigurationForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "general.config";
    }

    public String[] getFieldNames()
    {
        return new String[]{"hostName", "helpUrl", "rssEnabled"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, CHECKBOX};
    }
}
