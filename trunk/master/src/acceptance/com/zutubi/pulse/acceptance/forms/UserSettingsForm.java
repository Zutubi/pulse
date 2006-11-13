package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * The user edit form.
 */
public class UserSettingsForm extends BaseForm
{
    public UserSettingsForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "user.settings.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"defaultAction", "myBuildsCount", "refreshEnabled", "refreshInterval", "tailLines", "tailInterval"};
    }

    public int[] getFieldTypes()
    {
        return new int[]{SELECT, TEXTFIELD, CHECKBOX, TEXTFIELD, TEXTFIELD, TEXTFIELD};
    }

    public String[] getSelectOptions(String name)
    {
        if (name.equals("defaultAction"))
        {
            return new String[]{"dashboard", "welcome"};
        }
        else
        {
            return super.getSelectOptions(name);
        }
    }
}
